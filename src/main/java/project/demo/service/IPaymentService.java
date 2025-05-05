package project.demo.service;

import project.demo.model.Payment;
import project.demo.model.PaymentMethod;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing payments
 */
public interface IPaymentService {
    /**
     * Get customer's payment methods
     */
    List<PaymentMethod> getCustomerPaymentMethods(Integer customerId);

    /**
     * Add new payment method for customer
     */
    PaymentMethod addPaymentMethod(PaymentMethod paymentMethod);

    /**
     * Delete payment method
     */
    boolean deletePaymentMethod(Integer paymentMethodId, Integer customerId);

    /**
     * Create payment link
     */
    String createPaymentLink(Integer orderId, Integer paymentMethodId);

    /**
     * Process payment for an order
     */
    Payment processPayment(Integer orderId, Integer paymentMethodId, Double amount);

    /**
     * Confirm payment result from payment gateway
     */
    Payment confirmPayment(String transactionId, String status, Double amount);

    /**
     * Get payment information by ID
     */
    Optional<Payment> getPaymentById(Integer paymentId);

    /**
     * Get payment history for an order
     */
    List<Payment> getPaymentsByOrderId(Integer orderId);

    /**
     * Update payment status
     */
    Payment updatePaymentStatus(Integer paymentId, String status);

    /**
     * Refund a payment
     */
    Payment refundPayment(Integer paymentId, Double amount, String reason);
}
