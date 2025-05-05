package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an order status transition is invalid.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidOrderStatusException extends BusinessLogicException {
    
    private static final String DEFAULT_ERROR_CODE = "INVALID_ORDER_STATUS";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public InvalidOrderStatusException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public InvalidOrderStatusException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Creates a new exception for an invalid order status transition.
     * 
     * @param currentStatus the current status
     * @param newStatus the new status
     * @return a new InvalidOrderStatusException
     */
    public static InvalidOrderStatusException invalidTransition(String currentStatus, String newStatus) {
        return new InvalidOrderStatusException(
            String.format("Cannot change order status from '%s' to '%s'", currentStatus, newStatus)
        );
    }
    
    /**
     * Creates a new exception for an order that cannot be cancelled.
     * 
     * @param currentStatus the current status
     * @return a new InvalidOrderStatusException
     */
    public static InvalidOrderStatusException cannotCancel(String currentStatus) {
        return new InvalidOrderStatusException(
            String.format("Cannot cancel order with status '%s'", currentStatus)
        );
    }
}
