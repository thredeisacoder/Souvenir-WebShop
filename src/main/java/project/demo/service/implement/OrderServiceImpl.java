package project.demo.service.implement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.demo.enums.CartStatus;
import project.demo.enums.OrderStatus;
import project.demo.enums.PaymentStatus;
import project.demo.enums.ShipmentStatus;
import project.demo.exception.OrderException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Address;
import project.demo.model.Cart;
import project.demo.model.CartItem;
import project.demo.model.Order;
import project.demo.model.OrderDetail;
import project.demo.model.OrderTimelineEvent;
import project.demo.model.Payment;
import project.demo.model.PaymentMethod;
import project.demo.model.Shipment;
import project.demo.repository.AddressRepository;
import project.demo.repository.CartItemRepository;
import project.demo.repository.CartRepository;
import project.demo.repository.OrderDetailRepository;
import project.demo.repository.OrderRepository;
import project.demo.repository.OrderTimelineEventRepository;
import project.demo.repository.PaymentMethodRepository;
import project.demo.repository.PaymentRepository;
import project.demo.repository.ProductRepository;
import project.demo.repository.ShipmentRepository;
import project.demo.service.IOrderService;
import project.demo.service.IProductDetailService;

/**
 * Implementation of the IOrderService interface for managing Order entities
 */
@Service
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderTimelineEventRepository orderTimelineEventRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;
    private final IProductDetailService productDetailService;

    public OrderServiceImpl(OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            OrderTimelineEventRepository orderTimelineEventRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            AddressRepository addressRepository,
            PaymentMethodRepository paymentMethodRepository,
            PaymentRepository paymentRepository,
            ShipmentRepository shipmentRepository,
            IProductDetailService productDetailService) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.orderTimelineEventRepository = orderTimelineEventRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentRepository = paymentRepository;
        this.shipmentRepository = shipmentRepository;
        this.productDetailService = productDetailService;
    }

    @Override
    public Optional<Order> findById(Integer orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> findByCustomerId(Integer customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional
    public Order createFromCart(Integer cartId, Integer addressId, Integer paymentMethodId, String shippingMethod, String note) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found"));
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        if (!cart.getStatus().equals(CartStatus.ACTIVE.getValue())) {
            throw new OrderException("Cart is not active");
        }

        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        if (cartItems.isEmpty()) {
            throw new OrderException("Cart is empty");
        }

        Order order = new Order();
        order.setCustomerId(cart.getCustomerId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.setShippingAddress(address);
        order.setPaymentMethodId(paymentMethod.getPaymentMethodId());
        order.setShippingMethod(shippingMethod);
        order.setShippingFee(calculateShippingFee(shippingMethod));
        order.setEstimatedDeliveryDate(calculateEstimatedDeliveryDate(shippingMethod));
        order.setOrderStatus(OrderStatus.NEW.getValue());
        order.setNote(note);
        
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>();
        Order savedOrder = orderRepository.save(order);
        
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(savedOrder.getOrderId());
            detail.setProductId(item.getProductId());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getUnitPrice()); // Sử dụng unitPrice thay vì product.getPrice()
            detail.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))); // Sử dụng unitPrice cho subtotal
            
            // Nếu có note, thêm note vào OrderDetail
            if (note != null && !note.trim().isEmpty()) {
                detail.setNote(note);
            }
            
            orderDetails.add(detail);
            subtotal = subtotal.add(detail.getSubtotal());
            
            // Giảm số lượng trong kho sau khi thanh toán
            try {
                productDetailService.decreaseStockQuantity(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                throw new OrderException("Could not decrease stock quantity for product ID " + 
                    item.getProductId() + ": " + e.getMessage());
            }
        }
        
        savedOrder.setSubtotal(subtotal);
        savedOrder.setTotal(subtotal.add(savedOrder.getShippingFee()));
        savedOrder = orderRepository.save(savedOrder);
        
        orderDetailRepository.saveAll(orderDetails);
        
        // Create initial timeline event
        OrderTimelineEvent timelineEvent = new OrderTimelineEvent();
        timelineEvent.setOrderId(savedOrder.getOrderId());
        timelineEvent.setStatus(OrderStatus.ORDER_PLACED);
        timelineEvent.setTimestamp(LocalDateTime.now());
        timelineEvent.setIcon("fa-shopping-cart");
        timelineEvent.setIconBackgroundColor("bg-info");
        timelineEvent.setDescription("Đơn hàng của bạn đã được đặt thành công.");
        orderTimelineEventRepository.save(timelineEvent);
        
        // Tạo và lưu thông tin thanh toán
        Payment payment = createPayment(savedOrder, paymentMethod);
        paymentRepository.save(payment);
        
        // Tạo và lưu thông tin vận chuyển
        Shipment shipment = createShipment(savedOrder, shippingMethod);
        shipmentRepository.save(shipment);
        
        // Update cart status - sử dụng CONVERTED thay vì ORDERED để tuân thủ ràng buộc CHECK
        String newStatus = CartStatus.CONVERTED.getValue();
        System.out.println("Cập nhật giỏ hàng với ID " + cart.getCartId() + " từ trạng thái '" + cart.getStatus() + "' thành '" + newStatus + "'");
        
        // Trực tiếp cập nhật giỏ hàng - nếu có lỗi, transaction sẽ rollback hoàn toàn
        cart.setStatus(newStatus);
        cartRepository.save(cart);
        System.out.println("Đã cập nhật trạng thái giỏ hàng thành công.");
        
        return savedOrder;
    }
    
    @Override
    @Transactional
    public Order createFromCart(Integer cartId, Integer addressId, Integer paymentMethodId, String shippingMethod) {
        return createFromCart(cartId, addressId, paymentMethodId, shippingMethod, null);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Integer orderId, String status) {
        Optional<Order> optionalOrder = findById(orderId);
        if (!optionalOrder.isPresent()) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
        
        Order order = optionalOrder.get();
        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        
        if (order.getOrderStatus().equals(newStatus.getValue())) {
            return order;
        }
        
        order.setOrderStatus(newStatus.getValue());
        Order updatedOrder = orderRepository.save(order);
        
        OrderTimelineEvent timelineEvent = new OrderTimelineEvent();
        timelineEvent.setOrderId(updatedOrder.getOrderId());
        timelineEvent.setStatus(newStatus);
        timelineEvent.setTimestamp(LocalDateTime.now());
        timelineEvent.setNote("Order status updated to " + status);
        orderTimelineEventRepository.save(timelineEvent);
        
        return updatedOrder;
    }

    @Override
    @Transactional
    public void deleteOrder(Integer orderId) {
        Optional<Order> optionalOrder = findById(orderId);
        if (!optionalOrder.isPresent()) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
        
        Order order = optionalOrder.get();
        if (!order.getOrderStatus().equals(OrderStatus.CANCELLED.getValue())) {
            throw new OrderException("Only cancelled orders can be deleted");
        }
        
        orderTimelineEventRepository.deleteByOrderId(orderId);
        orderDetailRepository.deleteByOrderId(orderId);
        orderRepository.deleteById(orderId);
    }
    
    @Override
    public List<OrderDetail> getOrderDetails(Integer orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
    
    @Override
    public List<OrderTimelineEvent> getOrderTimeline(Integer orderId) {
        return orderTimelineEventRepository.findByOrderIdOrderByTimestampDesc(orderId);
    }
    
    @Override
    @Transactional
    public void cancelOrder(Integer orderId) {
        Optional<Order> optionalOrder = findById(orderId);
        if (!optionalOrder.isPresent()) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
        
        Order order = optionalOrder.get();
        order.setOrderStatus(OrderStatus.CANCELLED.getValue());
        orderRepository.save(order);
        
        OrderTimelineEvent timelineEvent = new OrderTimelineEvent();
        timelineEvent.setOrderId(order.getOrderId());
        timelineEvent.setStatus(OrderStatus.CANCELLED);
        timelineEvent.setTimestamp(LocalDateTime.now());
        timelineEvent.setNote("Order cancelled");
        orderTimelineEventRepository.save(timelineEvent);
    }
    
    @Override
    @Transactional
    public void updateStatus(Integer orderId, String status) {
        updateOrderStatus(orderId, status);
    }

    @Override
    public List<Order> findByStatus(String status) {
        validateOrderStatus(status);
        return orderRepository.findByOrderStatus(status);
    }
    
    @Override
    public OrderTimelineEvent addTimelineEvent(OrderTimelineEvent event) {
        Optional<Order> optionalOrder = findById(event.getOrderId());
        if (!optionalOrder.isPresent()) {
            throw new ResourceNotFoundException("Order not found with id: " + event.getOrderId());
        }
        
        event.setTimestamp(LocalDateTime.now());
        return orderTimelineEventRepository.save(event);
    }

    @Override
    @Transactional
    public Order applyDiscount(Integer orderId, String discountCode) {
        Optional<Order> optionalOrder = findById(orderId);
        if (!optionalOrder.isPresent()) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
        
        Order order = optionalOrder.get();
        
        // In a real application, this would check against a discount/promo table
        // and apply a more sophisticated logic. For now, applying a simple discount
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        if ("FREESHIP".equalsIgnoreCase(discountCode)) {
            // Free shipping discount
            discountAmount = order.getShippingFee();
        } else if ("WELCOME10".equalsIgnoreCase(discountCode)) {
            // 10% off discount
            discountAmount = order.getSubtotal().multiply(new BigDecimal("0.1"));
        } else if ("BLACKFRIDAY".equalsIgnoreCase(discountCode)) {
            // 20% off discount
            discountAmount = order.getSubtotal().multiply(new BigDecimal("0.2"));
        } else {
            // Default discount for any other code (5%)
            discountAmount = order.getSubtotal().multiply(new BigDecimal("0.05"));
        }
        
        // Set discount amount
        order.setDiscountAmount(discountAmount);
        
        // Recalculate total
        BigDecimal newTotal = order.getSubtotal().add(order.getShippingFee()).subtract(discountAmount);
        order.setTotal(newTotal);
        
        // Save and return updated order
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public OrderDetail updateOrderDetail(OrderDetail orderDetail) {
        // Verify order exists
        Optional<Order> optionalOrder = findById(orderDetail.getOrderId());
        if (!optionalOrder.isPresent()) {
            throw new ResourceNotFoundException("Order not found with id: " + orderDetail.getOrderId());
        }
        
        // Verify order detail exists
        OrderDetail existingDetail = orderDetailRepository.findById(orderDetail.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Order detail not found with id: " + orderDetail.getOrderItemId()));
        
        // Update fields and save
        return orderDetailRepository.save(orderDetail);
    }

    private BigDecimal calculateShippingFee(String shippingMethod) {
        if (shippingMethod == null) {
            return BigDecimal.ZERO;
        }

        return switch (shippingMethod.toLowerCase()) {
            case "standard" -> new BigDecimal("30000.00");
            case "express" -> new BigDecimal("50000.00");
            case "overnight", "same_day" -> new BigDecimal("70000.00"); // Thêm "same_day" vào cùng case với "overnight"
            default -> new BigDecimal("30000.00");
        };
    }

    private LocalDate calculateEstimatedDeliveryDate(String shippingMethod) {
        if (shippingMethod == null) {
            return LocalDate.now().plusDays(5);
        }

        return switch (shippingMethod.toLowerCase()) {
            case "standard" -> LocalDate.now().plusDays(5);
            case "express" -> LocalDate.now().plusDays(3);
            case "overnight" -> LocalDate.now().plusDays(1);
            default -> LocalDate.now().plusDays(5);
        };
    }

    private void validateOrderStatus(String status) {
        try {
            OrderStatus.fromValue(status);
        } catch (IllegalArgumentException e) {
            throw OrderException.invalidStatus(status);
        }
    }

    /**
     * Tạo đối tượng Payment từ thông tin đơn hàng
     * 
     * @param order Đơn hàng đã được lưu
     * @param paymentMethod Phương thức thanh toán
     * @return Đối tượng Payment với thông tin đầy đủ
     */
    private Payment createPayment(Order order, PaymentMethod paymentMethod) {
        Payment payment = new Payment();
        payment.setOrderId(order.getOrderId());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(order.getTotal());
        payment.setPaymentStatus(PaymentStatus.PENDING.getValue());  // Chuyển từ COMPLETED sang PENDING
        payment.setCurrency("VND");
        payment.setPaymentMethodId(paymentMethod.getPaymentMethodId());
        // Không thiết lập createdAt nữa vì đã đánh dấu là @Transient
        
        // Ghi chú thanh toán dựa trên phương thức thanh toán
        String noteFormat = "Thanh toán qua %s - đang xử lý";  // Thay đổi ghi chú
        payment.setNote(String.format(noteFormat, paymentMethod.getMethodName()));
        
        // Mã giao dịch dựa trên ID đơn hàng và thời gian unix epoch
        String transactionId = "TXN" + order.getOrderId() + System.currentTimeMillis();
        payment.setTransactionId(transactionId);
        
        return payment;
    }
    
    /**
     * Tạo đối tượng Shipment từ thông tin đơn hàng
     * 
     * @param order Đơn hàng đã được lưu
     * @param shippingMethod Phương thức vận chuyển
     * @return Đối tượng Shipment với thông tin đầy đủ
     */
    private Shipment createShipment(Order order, String shippingMethod) {
        Shipment shipment = new Shipment();
        shipment.setOrderId(order.getOrderId());
        shipment.setShipmentStatus(ShipmentStatus.PENDING.getValue());
        shipment.setShippingCost(order.getShippingFee());
        shipment.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        
        // Xác định nhà cung cấp dịch vụ vận chuyển dựa trên phương thức
        String provider;
        String trackingPrefix;
        
        if (shippingMethod.equalsIgnoreCase("express")) {
            provider = "Giao Hàng Nhanh";
            trackingPrefix = "GHN";
        } else if (shippingMethod.equalsIgnoreCase("standard")) {
            provider = "Giao Hàng Tiết Kiệm";
            trackingPrefix = "GHTK";
        } else if (shippingMethod.equalsIgnoreCase("overnight") || shippingMethod.equalsIgnoreCase("same_day")) {
            provider = "Giao Hàng Hỏa Tốc";
            trackingPrefix = "HT";
        } else {
            provider = "Dịch vụ vận chuyển tiêu chuẩn";
            trackingPrefix = "STD";
        }
        
        shipment.setShippingProvider(provider);
        
        // Tạo mã vận đơn theo yêu cầu: prefix + epoch time
        String trackingNumber = trackingPrefix + System.currentTimeMillis();
        shipment.setTrackingNumber(trackingNumber);
        
        return shipment;
    }
}
