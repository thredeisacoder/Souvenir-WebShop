package project.demo.exception;

/**
 * Exception thrown for payment method related errors
 */
public class PaymentMethodException extends RuntimeException {

    private final String errorCode;

    public PaymentMethodException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
