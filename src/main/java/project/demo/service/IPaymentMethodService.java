package project.demo.service;

import java.util.List;

import project.demo.model.PaymentMethod;

/**
 * Service interface for managing PaymentMethod entities
 */
public interface IPaymentMethodService {
    
    /**
     * Find a payment method by its ID
     * 
     * @param paymentMethodId the ID of the payment method to find
     * @return the payment method if found
     */
    PaymentMethod findById(Integer paymentMethodId);
    
    /**
     * Find payment methods by customer ID
     * 
     * @param customerId the ID of the customer
     * @return a list of payment methods for the customer
     */
    List<PaymentMethod> findByCustomerId(Integer customerId);
    
    /**
     * Save a new payment method
     * 
     * @param paymentMethod the payment method to save
     * @return the saved payment method
     */
    PaymentMethod save(PaymentMethod paymentMethod);
    
    /**
     * Update an existing payment method
     * 
     * @param paymentMethod the payment method with updated information
     * @return the updated payment method
     */
    PaymentMethod update(PaymentMethod paymentMethod);
    
    /**
     * Delete a payment method
     * 
     * @param paymentMethodId the ID of the payment method to delete
     */
    void delete(Integer paymentMethodId);
    
    /**
     * Set a payment method as default for a customer
     * 
     * @param paymentMethodId the ID of the payment method to set as default
     * @param customerId the ID of the customer
     * @return the updated payment method
     */
    PaymentMethod setAsDefault(Integer paymentMethodId, Integer customerId);
    
    /**
     * Find payment methods by method name
     * 
     * @param methodName the method name to search for
     * @return a list of payment methods with the specified method name
     */
    List<PaymentMethod> findByMethodName(String methodName);
    
    /**
     * Find payment methods by provider
     * 
     * @param provider the provider to search for
     * @return a list of payment methods with the specified provider
     */
    List<PaymentMethod> findByProvider(String provider);
    
    /**
     * Find payment methods by customer ID and method name
     * 
     * @param customerId the ID of the customer
     * @param methodName the method name to search for
     * @return a list of payment methods for the customer with the specified method name
     */
    List<PaymentMethod> findByCustomerIdAndMethodName(Integer customerId, String methodName);
}
