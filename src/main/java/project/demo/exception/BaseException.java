package project.demo.exception;

/**
 * Base exception class for all application-specific exceptions.
 * Extends RuntimeException to avoid forcing clients to catch exceptions.
 */
public abstract class BaseException extends RuntimeException {
    
    private final String errorCode;
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new exception with the specified error code, detail message, and cause.
     * 
     * @param errorCode the error code
     * @param message the detail message
     * @param cause the cause
     */
    public BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Gets the error code associated with this exception.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}
