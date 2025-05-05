package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user is not authorized to perform an action.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "UNAUTHORIZED";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public UnauthorizedException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public UnauthorizedException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Constructs a new exception with a default error code, the specified detail message, and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }
    
    /**
     * Creates a new exception for an invalid authentication.
     * 
     * @return a new UnauthorizedException
     */
    public static UnauthorizedException invalidAuthentication() {
        return new UnauthorizedException("Invalid authentication credentials");
    }
    
    /**
     * Creates a new exception for a missing authentication.
     * 
     * @return a new UnauthorizedException
     */
    public static UnauthorizedException missingAuthentication() {
        return new UnauthorizedException("Authentication credentials are required");
    }
}
