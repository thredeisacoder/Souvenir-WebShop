package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a business rule is violated.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessLogicException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "BUSINESS_LOGIC_ERROR";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public BusinessLogicException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public BusinessLogicException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Constructs a new exception with a default error code, the specified detail message, and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public BusinessLogicException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }
    
    /**
     * Creates a new exception for insufficient stock.
     * 
     * @param productName the name of the product
     * @param requested the requested quantity
     * @param available the available quantity
     * @return a new BusinessLogicException
     */
    public static BusinessLogicException insufficientStock(String productName, int requested, int available) {
        return new BusinessLogicException(
            "INSUFFICIENT_STOCK",
            String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d", productName, requested, available)
        );
    }
    
    /**
     * Creates a new exception for an expired promotion.
     * 
     * @param promotionCode the promotion code
     * @return a new BusinessLogicException
     */
    public static BusinessLogicException expiredPromotion(String promotionCode) {
        return new BusinessLogicException(
            "EXPIRED_PROMOTION",
            String.format("Promotion '%s' has expired", promotionCode)
        );
    }
    
    /**
     * Creates a new exception for an invalid order status transition.
     * 
     * @param currentStatus the current status
     * @param newStatus the new status
     * @return a new BusinessLogicException
     */
    public static BusinessLogicException invalidOrderStatusTransition(String currentStatus, String newStatus) {
        return new BusinessLogicException(
            "INVALID_ORDER_STATUS_TRANSITION",
            String.format("Cannot change order status from '%s' to '%s'", currentStatus, newStatus)
        );
    }
}
