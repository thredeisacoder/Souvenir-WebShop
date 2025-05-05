package project.demo.service.implement;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.demo.exception.PaymentMethodException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.PaymentMethod;
import project.demo.repository.PaymentMethodRepository;
import project.demo.service.IPaymentMethodService;

/**
 * Implementation of the IPaymentMethodService interface for managing PaymentMethod entities
 */
@Service
public class PaymentMethodServiceImpl implements IPaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    public PaymentMethodServiceImpl(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    @Override
    public PaymentMethod findById(Integer paymentMethodId) {
        return paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_METHOD_NOT_FOUND",
                        "Payment method not found with ID: " + paymentMethodId));
    }

    @Override
    public List<PaymentMethod> findByCustomerId(Integer customerId) {
        return paymentMethodRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional
    public PaymentMethod save(PaymentMethod paymentMethod) {
        validatePaymentMethod(paymentMethod);
        
        // If this payment method is set as default, unset all other payment methods for this customer
        if (paymentMethod.getIsDefault() != null && paymentMethod.getIsDefault()) {
            unsetDefaultPaymentMethods(paymentMethod.getCustomerId());
        }
        
        return paymentMethodRepository.save(paymentMethod);
    }

    @Override
    @Transactional
    public PaymentMethod update(PaymentMethod paymentMethod) {
        // Check if payment method exists
        findById(paymentMethod.getPaymentMethodId());

        validatePaymentMethod(paymentMethod);
        
        // If this payment method is set as default, unset all other payment methods for this customer
        if (paymentMethod.getIsDefault() != null && paymentMethod.getIsDefault()) {
            unsetDefaultPaymentMethods(paymentMethod.getCustomerId());
        }
        
        return paymentMethodRepository.save(paymentMethod);
    }

    @Override
    @Transactional
    public void delete(Integer paymentMethodId) {
        // Check if payment method exists
        PaymentMethod paymentMethod = findById(paymentMethodId);
        
        // If this is the default payment method, check if there are other payment methods
        if (paymentMethod.getIsDefault() != null && paymentMethod.getIsDefault()) {
            List<PaymentMethod> customerPaymentMethods = findByCustomerId(paymentMethod.getCustomerId());
            if (customerPaymentMethods.size() <= 1) {
                throw new PaymentMethodException("CANNOT_DELETE_DEFAULT",
                        "Cannot delete the default payment method. Please add another payment method first.");
            }
        }

        paymentMethodRepository.deleteById(paymentMethodId);
    }

    @Override
    @Transactional
    public PaymentMethod setAsDefault(Integer paymentMethodId, Integer customerId) {
        // Check if payment method exists and belongs to the customer
        PaymentMethod paymentMethod = findById(paymentMethodId);
        if (!paymentMethod.getCustomerId().equals(customerId)) {
            throw new PaymentMethodException("INVALID_PAYMENT_METHOD",
                    "Payment method does not belong to the customer");
        }
        
        // Unset all other payment methods for this customer
        unsetDefaultPaymentMethods(customerId);
        
        // Set this payment method as default
        paymentMethod.setIsDefault(true);
        return paymentMethodRepository.save(paymentMethod);
    }

    @Override
    public List<PaymentMethod> findByMethodName(String methodName) {
        return paymentMethodRepository.findByMethodName(methodName);
    }

    @Override
    public List<PaymentMethod> findByProvider(String provider) {
        return paymentMethodRepository.findByProvider(provider);
    }

    @Override
    public List<PaymentMethod> findByCustomerIdAndMethodName(Integer customerId, String methodName) {
        return paymentMethodRepository.findByCustomerIdAndMethodName(customerId, methodName);
    }
    
    /**
     * Unset the default flag for all payment methods of a customer
     * 
     * @param customerId the ID of the customer
     */
    private void unsetDefaultPaymentMethods(Integer customerId) {
        List<PaymentMethod> paymentMethods = findByCustomerId(customerId);
        for (PaymentMethod pm : paymentMethods) {
            if (pm.getIsDefault() != null && pm.getIsDefault()) {
                pm.setIsDefault(false);
                paymentMethodRepository.save(pm);
            }
        }
    }
    
    /**
     * Validate a payment method
     * 
     * @param paymentMethod the payment method to validate
     */
    private void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod.getCustomerId() == null) {
            throw new PaymentMethodException("INVALID_PAYMENT_METHOD",
                    "Customer ID is required");
        }
        
        if (paymentMethod.getMethodName() == null || paymentMethod.getMethodName().trim().isEmpty()) {
            throw new PaymentMethodException("INVALID_PAYMENT_METHOD",
                    "Method name is required");
        }
        
        // Xác định loại phương thức thanh toán
        boolean isCreditCard = paymentMethod.getMethodName().toLowerCase().contains("credit") || 
                               paymentMethod.getMethodName().toLowerCase().contains("thẻ tín dụng") ||
                               paymentMethod.getMethodName().toLowerCase().contains("visa") ||
                               paymentMethod.getMethodName().toLowerCase().contains("mastercard");
        
        // Chỉ yêu cầu account_number đối với thanh toán bằng thẻ tín dụng
        if (isCreditCard) {
            if (paymentMethod.getAccountNumber() == null || paymentMethod.getAccountNumber().trim().isEmpty()) {
                throw new PaymentMethodException("INVALID_PAYMENT_METHOD",
                        "Card number (Account number) is required for credit card payment methods");
            }
            
            if (paymentMethod.getProvider() == null || paymentMethod.getProvider().trim().isEmpty()) {
                throw new PaymentMethodException("INVALID_PAYMENT_METHOD",
                        "Card provider is required for credit card payment methods");
            }
        }
        
        // Không yêu cầu account_number và provider cho các phương thức thanh toán khác
        // Chúng có thể để NULL, không cần gán giá trị mặc định
    }
}
