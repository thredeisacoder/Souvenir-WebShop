package project.demo.service.implement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.demo.model.Order;
import project.demo.model.Payment;
import project.demo.model.PaymentMethod;
import project.demo.repository.OrderRepository;
import project.demo.repository.PaymentMethodRepository;
import project.demo.repository.PaymentRepository;
import project.demo.service.IPaymentService;

@Service
public class PaymentServiceImpl implements IPaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderRepository orderRepository;
    
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              PaymentMethodRepository paymentMethodRepository,
                              OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<PaymentMethod> getCustomerPaymentMethods(Integer customerId) {
        return paymentMethodRepository.findByCustomerId(customerId);
    }

    @Override
    public PaymentMethod addPaymentMethod(PaymentMethod paymentMethod) {
        // Set default to false if not specified
        if (paymentMethod.getIsDefault() == null) {
            paymentMethod.setIsDefault(false);
        }
        return paymentMethodRepository.save(paymentMethod);
    }

    @Override
    @Transactional
    public boolean deletePaymentMethod(Integer paymentMethodId, Integer customerId) {
        // First verify that the payment method belongs to the customer
        if (!paymentMethodRepository.existsByPaymentMethodIdAndCustomerId(paymentMethodId, customerId)) {
            return false;
        }
        
        paymentMethodRepository.deleteByPaymentMethodIdAndCustomerId(paymentMethodId, customerId);
        return true;
    }

    @Override
    public String createPaymentLink(Integer orderId, Integer paymentMethodId) {
        // In a real implementation, this would connect to a payment gateway
        // Here we'll create a simulated payment link
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        
        Optional<PaymentMethod> methodOpt = paymentMethodRepository.findById(paymentMethodId);
        if (methodOpt.isEmpty()) {
            throw new RuntimeException("Payment method not found");
        }
        
        // Simulate a payment link
        String baseUrl = "http://localhost:8080/payment/process";
        return baseUrl + "?orderId=" + orderId + "&token=" + UUID.randomUUID().toString();
    }

    @Override
    @Transactional
    public Payment processPayment(Integer orderId, Integer paymentMethodId, Double amount) {
        // Verify the order exists
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        
        // Verify the payment method exists
        Optional<PaymentMethod> methodOpt = paymentMethodRepository.findById(paymentMethodId);
        if (methodOpt.isEmpty()) {
            throw new RuntimeException("Payment method not found");
        }
        
        // Tìm thanh toán hiện có cho đơn hàng này
        List<Payment> existingPayments = paymentRepository.findByOrderId(orderId);
        Payment payment;
        
        if (!existingPayments.isEmpty()) {
            // Cập nhật thanh toán hiện có thay vì tạo mới
            payment = existingPayments.get(0); // Lấy thanh toán đầu tiên
            
            // Cập nhật thông tin thanh toán
            payment.setPaymentMethodId(paymentMethodId);
            payment.setAmount(BigDecimal.valueOf(amount));
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentStatus("completed");
            payment.setUpdatedAt(LocalDateTime.now());
            payment.setNote("Thanh toán thành công qua " + methodOpt.get().getMethodName());
        } else {
            // Tạo mới bản ghi thanh toán nếu không tìm thấy
            payment = new Payment();
            payment.setOrderId(orderId);
            payment.setPaymentMethodId(paymentMethodId);
            payment.setAmount(BigDecimal.valueOf(amount));
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentStatus("completed");
            payment.setCurrency("VND");
            payment.setTransactionId(generateTransactionId());
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
        }
        
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment confirmPayment(String transactionId, String status, Double amount) {
        // Find payment by transaction ID
        List<Payment> payments = paymentRepository.findAll(); // In a real implementation, we'd have a findByTransactionId method
        
        for (Payment payment : payments) {
            if (transactionId.equals(payment.getTransactionId())) {
                payment.setPaymentStatus(status);
                payment.setAmount(BigDecimal.valueOf(amount));
                payment.setUpdatedAt(LocalDateTime.now());
                return paymentRepository.save(payment);
            }
        }
        
        throw new RuntimeException("Payment with transaction ID " + transactionId + " not found");
    }

    @Override
    public Optional<Payment> getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    public List<Payment> getPaymentsByOrderId(Integer orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(Integer paymentId, String status) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }
        
        Payment payment = paymentOpt.get();
        payment.setPaymentStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment refundPayment(Integer paymentId, Double amount, String reason) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }
        
        // Create a new payment record for the refund
        Payment originalPayment = paymentOpt.get();
        Payment refundPayment = new Payment();
        refundPayment.setOrderId(originalPayment.getOrderId());
        refundPayment.setPaymentMethodId(originalPayment.getPaymentMethodId());
        refundPayment.setAmount(BigDecimal.valueOf(-amount)); // Negative amount for refund
        refundPayment.setPaymentDate(LocalDateTime.now());
        refundPayment.setPaymentStatus("refunded");
        refundPayment.setCurrency(originalPayment.getCurrency());
        refundPayment.setTransactionId(generateTransactionId());
        refundPayment.setNote("Refund for payment #" + paymentId + ". Reason: " + reason);
        refundPayment.setCreatedAt(LocalDateTime.now());
        refundPayment.setUpdatedAt(LocalDateTime.now());
        
        return paymentRepository.save(refundPayment);
    }
    
    // Helper method to generate a transaction ID
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
