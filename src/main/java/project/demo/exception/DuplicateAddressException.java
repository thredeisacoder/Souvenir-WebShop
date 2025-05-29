package project.demo.exception;

/**
 * Specific exception for duplicate address validation
 * This exception does NOT inherit from BaseException to avoid GlobalExceptionHandler interference
 */
public class DuplicateAddressException extends RuntimeException {
    
    private final String errorCode;
    
    public DuplicateAddressException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
} 