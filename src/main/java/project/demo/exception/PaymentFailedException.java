package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a payment fails.
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class PaymentFailedException extends BusinessLogicException {

    private static final String DEFAULT_ERROR_CODE = "PAYMENT_FAILED";

    /**
     * Constructs a new exception with a default error code and the specified detail
     * message.
     * 
     * @param message the detail message
     */
    public PaymentFailedException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message   the detail message
     */
    public PaymentFailedException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructs a new exception with a default error code, the specified detail
     * message, and cause.
     * 
     * @param message the detail message
     * @param cause   the cause
     */
    public PaymentFailedException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * Creates a new exception for a payment that was declined.
     * 
     * @param reason the reason for the decline
     * @return a new PaymentFailedException
     */
    public static PaymentFailedException declined(String reason) {
        return new PaymentFailedException(
                String.format("Payment was declined: %s", reason));
    }

    /**
     * Creates a new exception for a payment gateway error.
     * 
     * @param errorMessage the error message from the payment gateway
     * @return a new PaymentFailedException
     */
    public static PaymentFailedException gatewayError(String errorMessage) {
        return new PaymentFailedException(
                "PAYMENT_GATEWAY_ERROR",
                String.format("Payment gateway error: %s", errorMessage));
    }
}
