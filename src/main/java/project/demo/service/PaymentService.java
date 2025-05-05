package project.demo.service;

import java.util.List;
import java.util.Optional;

import project.demo.model.Payment;
import project.demo.model.PaymentMethod;

/**
 * Service interface for managing payments
 */
public interface PaymentService {

    /**
     * Process payment for an order
     */
    Payment processPayment(int orderId, int paymentMethodId, double amount);

    /**
     * Get payment information by ID
     */
    Optional<Payment> getPaymentById(int paymentId);

    /**
     * Get payment history for an order
     */
    List<Payment> getPaymentsByOrderId(int orderId);

    /**
     * Update payment status
     */
    Payment updatePaymentStatus(int paymentId, String status);

    /**
     * Refund a payment
     */
    Payment refundPayment(int paymentId, Double amount, String reason);

    /**
     * Get customer's payment methods
     */
    List<PaymentMethod> getCustomerPaymentMethods(int customerId);
    
    /**
     * Get payment method by ID
     */
    PaymentMethod getPaymentMethod(Integer paymentMethodId);

    /**
     * Add new payment method for customer
     */
    PaymentMethod addPaymentMethod(PaymentMethod paymentMethod);

    /**
     * Delete payment method
     */
    boolean deletePaymentMethod(int paymentMethodId, int customerId);

    /**
     * Create payment link
     */
    String createPaymentLink(int orderId, int paymentMethodId);

    /**
     * Confirm payment result from payment gateway
     */
    Payment confirmPayment(String transactionId, String status, double amount);
}
