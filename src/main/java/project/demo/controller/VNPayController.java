package project.demo.controller;

import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import project.demo.model.Cart;
import project.demo.model.Order;
import project.demo.model.Payment;
import project.demo.model.PaymentMethod;
import project.demo.model.PendingPayment;
import project.demo.repository.PaymentRepository;
import project.demo.service.ICartService;
import project.demo.service.IOrderService;
import project.demo.service.IPaymentMethodService;
import project.demo.service.IPaymentService;
import project.demo.service.VNPayService;

@Controller
@RequestMapping("/vnpay")
public class VNPayController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private IPaymentService paymentService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private ICartService cartService;

    @Autowired
    private IPaymentMethodService paymentMethodService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    // Simple in-memory storage for pending payments (in production, use database)
    private static final Map<String, PendingPayment> pendingPayments = new ConcurrentHashMap<>();

    @PostMapping("/create-payment")
    public String createPayment(@RequestParam("orderId") String orderId,
                                @RequestParam("amount") long amount,
                                @RequestParam("orderInfo") String orderInfo,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            String ipAddress = getClientIP(request);
            String paymentUrl = vnPayService.createPaymentUrl(orderId, amount, orderInfo, ipAddress);
            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tạo thanh toán: " + e.getMessage());
            return "redirect:/checkout/payment-failed";
        }
    }

    @GetMapping("/payment-return")
    public String paymentReturn(HttpServletRequest request, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Validate signature
            if (!vnPayService.validateSignature(request)) {
                model.addAttribute("errorMessage", "Chữ ký thanh toán không hợp lệ. Vui lòng thử lại.");
                model.addAttribute("errorDetails", "Chi tiết lỗi: Chữ ký thanh toán không hợp lệ. Vui lòng thử lại.");
                System.err.println("VNPay signature validation failed for request: " + request.getQueryString());
                return "payment/vnpay-result";
            }

            String paymentStatus = vnPayService.getPaymentStatus(request);
            String orderCode = vnPayService.getOrderId(request); // This is the order code we sent to VNPay
            String transactionId = vnPayService.getTransactionId(request);
            long amount = vnPayService.getAmount(request);

            if ("SUCCESS".equals(paymentStatus)) {
                // Payment was successful, now try to create order
                Integer actualOrderId = createOrderAfterSuccessfulPayment(session, orderCode, transactionId, amount);
                
                if (actualOrderId != null) {
                    // Both payment and order creation successful
                    model.addAttribute("paymentStatus", "success");
                    model.addAttribute("orderId", actualOrderId); // This is the actual order ID from database
                    model.addAttribute("transactionId", transactionId);
                    model.addAttribute("amount", amount);
                    
                    // Clear VNPay session data
                    clearVNPaySessionData(session);
                    
                    return "payment/vnpay-result";
                } else {
                    // Payment successful but order creation failed - this is a special case
                    // Save this successful payment for manual processing
                    savePendingPayment(transactionId, orderCode, amount, session);
                    
                    model.addAttribute("paymentStatus", "payment_success_order_failed");
                    model.addAttribute("errorMessage", "Thanh toán thành công nhưng không thể tạo đơn hàng. Vui lòng liên hệ hỗ trợ.");
                    model.addAttribute("errorDetails", "Thanh toán VNPay đã thành công nhưng hệ thống không thể tạo đơn hàng. Chúng tôi sẽ xử lý và liên hệ với bạn sớm nhất.");
                    model.addAttribute("transactionId", transactionId);
                    model.addAttribute("amount", amount);
                    model.addAttribute("vnpayReference", orderCode);
                    model.addAttribute("paymentSuccessful", true); // Flag to show payment was successful
                    
                    // Don't clear session data yet - might need it for manual order creation
                    return "payment/vnpay-result";
                }
            } else {
                // Payment actually failed at VNPay level
                model.addAttribute("paymentStatus", "failed");
                model.addAttribute("transactionId", transactionId);
                model.addAttribute("amount", amount);
                model.addAttribute("vnpayReference", orderCode);
                
                return "payment/vnpay-result";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi xử lý kết quả thanh toán: " + e.getMessage());
            return "payment/payment-failed";
        }
    }

    @PostMapping("/payment-notify")
    @ResponseBody
    public String paymentNotify(HttpServletRequest request) {
        try {
            // Validate signature
            if (!vnPayService.validateSignature(request)) {
                return "Invalid signature";
            }

            String paymentStatus = vnPayService.getPaymentStatus(request);
            String orderId = vnPayService.getOrderId(request);
            String transactionId = vnPayService.getTransactionId(request);
            long amount = vnPayService.getAmount(request);

            // Update payment status
            updatePaymentStatus(orderId, transactionId, 
                "SUCCESS".equals(paymentStatus) ? "completed" : "failed", amount);

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private void updatePaymentStatus(String orderId, String transactionId, String status, long amount) {
        try {
            // For VNPay flow, orderId might be the order code, not actual order ID
            // We need to create the order first if payment is successful
            if ("completed".equals(status)) {
                createOrderFromVNPayPayment(orderId, transactionId, amount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createOrderFromVNPayPayment(String orderCode, String transactionId, long amount) {
        try {
            System.out.println("Creating order for VNPay payment - Order code: " + orderCode + ", Transaction: " + transactionId);
            
            // This method will need access to HttpSession to get pending order info
            // For now, just log the successful payment
            // The actual order creation will be handled in the payment return callback
            // where we have access to the session
            
        } catch (Exception e) {
            System.err.println("Error creating order from VNPay payment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Integer createOrderAfterSuccessfulPayment(HttpSession session, String orderCode, String transactionId, long amount) {
        try {
            System.out.println("=== Creating order after successful VNPay payment ===");
            System.out.println("Order code: " + orderCode + ", Transaction ID: " + transactionId + ", Amount: " + amount);
            
            // Get pending order information from session
            String orderNote = (String) session.getAttribute("vnpay_pending_order_note");
            Integer addressId = (Integer) session.getAttribute("vnpay_pending_address_id");
            String shippingMethodId = (String) session.getAttribute("vnpay_pending_shipping_method");
            Integer customerId = (Integer) session.getAttribute("vnpay_pending_customer_id");
            
            System.out.println("Session data - customerId: " + customerId + ", addressId: " + addressId + 
                             ", shippingMethodId: " + shippingMethodId + ", orderNote: " + orderNote);
            
            if (customerId == null || addressId == null || shippingMethodId == null) {
                System.err.println("=== CRITICAL: Missing required session data for order creation ===");
                System.err.println("- customerId: " + customerId);
                System.err.println("- addressId: " + addressId);
                System.err.println("- shippingMethodId: " + shippingMethodId);
                System.err.println("- orderNote: " + orderNote);
                System.err.println("Session may have expired during VNPay payment process");
                
                // Log all session attributes for debugging
                System.err.println("All current session attributes:");
                Enumeration<String> attributeNames = session.getAttributeNames();
                boolean hasAnyVnpayData = false;
                while (attributeNames.hasMoreElements()) {
                    String attrName = attributeNames.nextElement();
                    Object attrValue = session.getAttribute(attrName);
                    System.err.println("  " + attrName + " = " + attrValue);
                    if (attrName.contains("vnpay")) {
                        hasAnyVnpayData = true;
                    }
                }
                
                if (!hasAnyVnpayData) {
                    System.err.println("=== SESSION COMPLETELY LOST: No VNPay session data found ===");
                } else {
                    System.err.println("=== PARTIAL SESSION LOSS: Some VNPay data exists but required fields missing ===");
                }
                
                return null;
            }
            
            // Create VNPay payment method record
            System.out.println("Creating VNPay payment method record...");
            PaymentMethod vnpayMethod = new PaymentMethod();
            vnpayMethod.setCustomerId(customerId);
            vnpayMethod.setMethodName("VNPay");
            vnpayMethod.setProvider("VNPay");
            vnpayMethod.setDescription("Thanh toán trực tuyến qua cổng thanh toán VNPay");
            vnpayMethod = paymentMethodService.save(vnpayMethod);
            System.out.println("VNPay payment method created with ID: " + vnpayMethod.getPaymentMethodId());
            
            // Get customer's cart
            System.out.println("Finding cart for customer: " + customerId);
            Optional<Cart> cartOpt = cartService.findByCustomerId(customerId);
            if (!cartOpt.isPresent()) {
                System.err.println("Cart not found for customer: " + customerId);
                return null;
            }
            
            Cart cart = cartOpt.get();
            System.out.println("Found cart with ID: " + cart.getCartId());
            
            // Create order
            System.out.println("Creating order from cart...");
            System.out.println("Parameters - cartId: " + cart.getCartId() + ", addressId: " + addressId + 
                             ", paymentMethodId: " + vnpayMethod.getPaymentMethodId() + ", shippingMethodId: " + shippingMethodId);
            Order order = orderService.createFromCart(
                cart.getCartId(), 
                addressId, 
                vnpayMethod.getPaymentMethodId(), 
                shippingMethodId,
                orderNote
            );
            
            if (order != null) {
                System.out.println("Order created successfully with ID: " + order.getOrderId());
                
                // Process payment with VNPay transaction ID
                System.out.println("Processing VNPay payment...");
                paymentService.processPayment(order.getOrderId(), vnpayMethod.getPaymentMethodId(), (double) amount);
                
                // Update payment with VNPay transaction ID (override the default TXN format)
                updatePaymentWithVNPayTransactionId(order.getOrderId(), transactionId);
                System.out.println("Payment processed with VNPay transaction ID: " + transactionId);
                
                // Try to clear cart (but handle if already converted)
                System.out.println("Attempting to clear cart...");
                try {
                    cartService.clearCart(cart.getCartId());
                    System.out.println("Cart cleared successfully");
                } catch (Exception e) {
                    System.out.println("Cart clear failed (likely already converted): " + e.getMessage());
                    // This is okay - cart might already be converted from the order creation
                }
                
                System.out.println("=== Order creation completed successfully ===");
                return order.getOrderId();
            } else {
                System.err.println("Order creation returned null from orderService.createFromCart()");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error creating order after VNPay payment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    private void clearVNPaySessionData(HttpSession session) {
        session.removeAttribute("vnpay_pending_order_note");
        session.removeAttribute("vnpay_pending_address_id");
        session.removeAttribute("vnpay_pending_shipping_method");
        session.removeAttribute("vnpay_pending_customer_id");
        session.removeAttribute("vnpay_pending_total");
        session.removeAttribute("vnpay_pending_reference");
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
    
    private void savePendingPayment(String transactionId, String vnpayReference, long amount, HttpSession session) {
        try {
            PendingPayment pendingPayment = new PendingPayment();
            pendingPayment.setTransactionId(transactionId);
            pendingPayment.setVnpayReference(vnpayReference);
            pendingPayment.setAmount(amount);
            
            // Try to get session data if available
            pendingPayment.setCustomerId((Integer) session.getAttribute("vnpay_pending_customer_id"));
            pendingPayment.setAddressId((Integer) session.getAttribute("vnpay_pending_address_id"));
            pendingPayment.setShippingMethodId((String) session.getAttribute("vnpay_pending_shipping_method"));
            pendingPayment.setOrderNote((String) session.getAttribute("vnpay_pending_order_note"));
            
            // Store in memory (in production, save to database)
            pendingPayments.put(transactionId, pendingPayment);
            
            System.out.println("=== SAVED PENDING PAYMENT ===");
            System.out.println("Transaction ID: " + transactionId);
            System.out.println("VNPay Reference: " + vnpayReference);
            System.out.println("Amount: " + amount);
            System.out.println("Customer ID: " + pendingPayment.getCustomerId());
            System.out.println("Address ID: " + pendingPayment.getAddressId());
            System.out.println("Shipping Method: " + pendingPayment.getShippingMethodId());
            System.out.println("Total pending payments: " + pendingPayments.size());
            
        } catch (Exception e) {
            System.err.println("Error saving pending payment: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @GetMapping("/pending-payments")
    @ResponseBody
    public Map<String, PendingPayment> getPendingPayments() {
        // Admin endpoint to view pending payments (for debugging/manual processing)
        return pendingPayments;
    }
    
    private void updatePaymentWithVNPayTransactionId(Integer orderId, String vnpayTransactionId) {
        try {
            // Find the payment for this order
            Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId).stream().findFirst();
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                // Update transaction ID with VNPay transaction ID
                payment.setTransactionId(vnpayTransactionId);
                // Update note to reflect VNPay payment (without transaction ID)
                payment.setNote("Thanh toán VNPay thành công");
                paymentRepository.save(payment);
                System.out.println("Updated payment transaction ID to VNPay ID: " + vnpayTransactionId);
            } else {
                System.err.println("Payment not found for order ID: " + orderId);
            }
        } catch (Exception e) {
            System.err.println("Error updating payment with VNPay transaction ID: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 