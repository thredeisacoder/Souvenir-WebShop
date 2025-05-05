package project.demo.service.implement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import project.demo.enums.CartStatus;
import project.demo.enums.OrderStatus;
import project.demo.exception.OrderException;
import project.demo.model.Address;
import project.demo.model.Cart;
import project.demo.model.CartItem;
import project.demo.model.Order;
import project.demo.model.OrderDetail;
import project.demo.model.OrderTimelineEvent;
import project.demo.model.PaymentMethod;
import project.demo.repository.AddressRepository;
import project.demo.repository.CartItemRepository;
import project.demo.repository.CartRepository;
import project.demo.repository.OrderDetailRepository;
import project.demo.repository.OrderRepository;
import project.demo.repository.OrderTimelineEventRepository;
import project.demo.repository.PaymentMethodRepository;
import project.demo.repository.ProductRepository;

class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private OrderTimelineEventRepository orderTimelineEventRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private Cart testCart;
    private List<CartItem> testCartItems;
    private Address testAddress;
    private PaymentMethod testPaymentMethod;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test order
        testOrder = new Order();
        testOrder.setOrderId(1);
        testOrder.setCustomerId(1);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        testOrder.setShippingFee(new BigDecimal("15.00"));
        testOrder.setOrderStatus(OrderStatus.NEW.getValue());
        testOrder.setAddressId(1);
        testOrder.setShippingMethod("standard");
        testOrder.setPaymentMethodId(1);
        testOrder.setEstimatedDeliveryDate(LocalDate.now().plusDays(5));

        // Setup test cart
        testCart = new Cart();
        testCart.setCartId(1);
        testCart.setCustomerId(1);
        testCart.setStatus(CartStatus.ACTIVE.getValue());
        testCart.setTotalAmount(new BigDecimal("100.00"));

        // Setup test cart items
        testCartItems = new ArrayList<>();
        CartItem item1 = new CartItem();
        item1.setCartItemId(1);
        item1.setCartId(1);
        item1.setProductId(1);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("50.00"));
        item1.setIsSelected(true);
        testCartItems.add(item1);

        // Setup test address
        testAddress = new Address();
        testAddress.setAddressId(1);
        testAddress.setCustomerId(1);
        testAddress.setAddressLine("123 Test Street");
        testAddress.setCity("Test City");
        testAddress.setCountry("Test Country");

        // Setup test payment method
        testPaymentMethod = new PaymentMethod();
        testPaymentMethod.setPaymentMethodId(1);
        testPaymentMethod.setCustomerId(1);
        testPaymentMethod.setMethodName("Credit Card");
        testPaymentMethod.setProvider("Visa");
    }

    @Test
    void findById_ExistingOrder_ReturnsOrder() {
        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        // Act
        Optional<Order> result = orderService.findById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testOrder.getOrderId(), result.get().getOrderId());
        verify(orderRepository, times(1)).findById(1);
    }

    @Test
    void findById_NonExistingOrder_ReturnsEmptyOptional() {
        // Arrange
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.findById(999);

        // Assert
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findById(999);
    }

    @Test
    void findByCustomerId_ReturnsOrders() {
        // Arrange
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);
        when(orderRepository.findByCustomerIdOrderByOrderDateDesc(1)).thenReturn(orders);

        // Act
        List<Order> result = orderService.findByCustomerId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getOrderId(), result.get(0).getOrderId());
        verify(orderRepository, times(1)).findByCustomerIdOrderByOrderDateDesc(1);
    }

    @Test
    void createFromCart_ValidCart_CreatesOrder() {
        // Arrange
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndIsSelected(1, true)).thenReturn(testCartItems);
        when(addressRepository.findById(1)).thenReturn(Optional.of(testAddress));
        when(paymentMethodRepository.findById(1)).thenReturn(Optional.of(testPaymentMethod));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderDetailRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(orderTimelineEventRepository.save(any(OrderTimelineEvent.class))).thenReturn(new OrderTimelineEvent());

        // Act
        Order result = orderService.createFromCart(1, 1, 1, "standard", "Test note");

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getOrderId(), result.getOrderId());
        verify(cartRepository, times(1)).findById(1);
        verify(cartItemRepository, times(1)).findByCartIdAndIsSelected(1, true);
        verify(addressRepository, times(1)).findById(1);
        verify(paymentMethodRepository, times(1)).findById(1);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderDetailRepository, times(1)).saveAll(anyList());
        verify(orderTimelineEventRepository, times(1)).save(any(OrderTimelineEvent.class));
        verify(cartRepository, times(1)).save(testCart);

        // Check that cart status was updated
        assertEquals(CartStatus.CONVERTED.getValue(), testCart.getStatus());
    }

    @Test
    void createFromCart_EmptyCart_ThrowsException() {
        // Arrange
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndIsSelected(1, true)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(OrderException.class, () -> orderService.createFromCart(1, 1, 1, "standard", "Test note"));
        verify(cartRepository, times(1)).findById(1);
        verify(cartItemRepository, times(1)).findByCartIdAndIsSelected(1, true);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createFromCart_InvalidCartStatus_ThrowsException() {
        // Arrange
        testCart.setStatus(CartStatus.CONVERTED.getValue());
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThrows(OrderException.class, () -> orderService.createFromCart(1, 1, 1, "standard", "Test note"));
        verify(cartRepository, times(1)).findById(1);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createFromCart_AddressNotOwnedByCustomer_ThrowsException() {
        // Arrange
        Address otherCustomerAddress = new Address();
        otherCustomerAddress.setAddressId(1);
        otherCustomerAddress.setCustomerId(2); // Different customer ID

        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndIsSelected(1, true)).thenReturn(testCartItems);
        when(addressRepository.findById(1)).thenReturn(Optional.of(otherCustomerAddress));

        // Act & Assert
        assertThrows(OrderException.class, () -> orderService.createFromCart(1, 1, 1, "standard", "Test note"));
        verify(cartRepository, times(1)).findById(1);
        verify(cartItemRepository, times(1)).findByCartIdAndIsSelected(1, true);
        verify(addressRepository, times(1)).findById(1);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateStatus_ValidTransition_UpdatesStatus() {
        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderTimelineEventRepository.save(any(OrderTimelineEvent.class))).thenReturn(new OrderTimelineEvent());

        // Act
        orderService.updateStatus(1, OrderStatus.PROCESSING_PAYMENT_VERIFICATION.getValue());

        // Assert
        assertEquals(OrderStatus.PROCESSING_PAYMENT_VERIFICATION.getValue(), testOrder.getOrderStatus());
        verify(orderRepository, times(1)).findById(1);
        verify(orderRepository, times(1)).save(testOrder);
        verify(orderTimelineEventRepository, times(1)).save(any(OrderTimelineEvent.class));
    }

    @Test
    void updateStatus_InvalidStatus_ThrowsException() {
        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(OrderException.class, () -> orderService.updateStatus(1, "invalid_status"));
        verify(orderRepository, times(1)).findById(1);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateStatus_InvalidTransition_ThrowsException() {
        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(OrderException.class, () -> orderService.updateStatus(1, OrderStatus.DELIVERED.getValue()));
        verify(orderRepository, times(1)).findById(1);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderDetails_ReturnsDetails() {
        // Arrange
        List<OrderDetail> orderDetails = new ArrayList<>();
        OrderDetail detail = new OrderDetail();
        detail.setOrderItemId(1);
        detail.setOrderId(1);
        detail.setProductId(1);
        detail.setQuantity(2);
        detail.setUnitPrice(new BigDecimal("50.00"));
        orderDetails.add(detail);

        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderDetailRepository.findByOrderId(1)).thenReturn(orderDetails);

        // Act
        List<OrderDetail> result = orderService.getOrderDetails(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(detail.getOrderItemId(), result.get(0).getOrderItemId());
        verify(orderRepository, times(1)).findById(1);
        verify(orderDetailRepository, times(1)).findByOrderId(1);
    }

    @Test
    void cancelOrder_PendingOrder_CancelsOrder() {
        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderTimelineEventRepository.save(any(OrderTimelineEvent.class))).thenReturn(new OrderTimelineEvent());

        // Act
        orderService.cancelOrder(1);

        // Assert
        assertEquals(OrderStatus.CANCELLED.getValue(), testOrder.getOrderStatus());
        verify(orderRepository, times(1)).findById(1);
        verify(orderRepository, times(1)).save(testOrder);
        verify(orderTimelineEventRepository, times(1)).save(any(OrderTimelineEvent.class));
    }

    @Test
    void cancelOrder_DeliveredOrder_ThrowsException() {
        // Arrange
        testOrder.setOrderStatus(OrderStatus.DELIVERED.getValue());
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(OrderException.class, () -> orderService.cancelOrder(1));
        verify(orderRepository, times(1)).findById(1);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderTimeline_ReturnsTimeline() {
        // Arrange
        List<OrderTimelineEvent> timeline = new ArrayList<>();
        OrderTimelineEvent event = new OrderTimelineEvent();
        event.setId(1L);
        event.setOrderId(1);
        event.setStatus(OrderStatus.NEW.getValue());
        event.setTimestamp(LocalDateTime.now());
        timeline.add(event);

        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderTimelineEventRepository.findByOrderIdOrderByTimestampDesc(1)).thenReturn(timeline);

        // Act
        List<OrderTimelineEvent> result = orderService.getOrderTimeline(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(event.getId(), result.get(0).getId());
        verify(orderRepository, times(1)).findById(1);
        verify(orderTimelineEventRepository, times(1)).findByOrderIdOrderByTimestampDesc(1);
    }

    @Test
    void addTimelineEvent_AddsEvent() {
        // Arrange
        OrderTimelineEvent event = new OrderTimelineEvent();
        event.setOrderId(1);
        event.setStatus("Custom Status");
        event.setDescription("Custom event");

        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderTimelineEventRepository.save(any(OrderTimelineEvent.class))).thenReturn(event);

        // Act
        OrderTimelineEvent result = orderService.addTimelineEvent(event);

        // Assert
        assertNotNull(result);
        assertEquals(event.getStatus(), result.getStatus());
        verify(orderRepository, times(1)).findById(1);
        verify(orderTimelineEventRepository, times(1)).save(event);
    }

    @Test
    void findByStatus_ReturnsOrders() {
        // Arrange
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);
        when(orderRepository.findByOrderStatus(OrderStatus.NEW.getValue())).thenReturn(orders);

        // Act
        List<Order> result = orderService.findByStatus(OrderStatus.NEW.getValue());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getOrderId(), result.get(0).getOrderId());
        verify(orderRepository, times(1)).findByOrderStatus(OrderStatus.NEW.getValue());
    }

    @Test
    void findByStatus_InvalidStatus_ThrowsException() {
        // Act & Assert
        assertThrows(OrderException.class, () -> orderService.findByStatus("invalid_status"));
        verify(orderRepository, never()).findByOrderStatus(anyString());
    }
}
