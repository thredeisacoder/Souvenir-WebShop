package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a request contains invalid parameters or data.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "BAD_REQUEST";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public BadRequestException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public BadRequestException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Constructs a new exception with a default error code, the specified detail message, and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public BadRequestException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }
    
    /**
     * Creates a new exception for a missing required parameter.
     * 
     * @param paramName the name of the parameter
     * @return a new BadRequestException
     */
    public static BadRequestException missingParameter(String paramName) {
        return new BadRequestException(
            String.format("Required parameter '%s' is missing", paramName)
        );
    }
    
    /**
     * Creates a new exception for an invalid parameter value.
     * 
     * @param paramName the name of the parameter
     * @param value the invalid value
     * @return a new BadRequestException
     */
    public static BadRequestException invalidParameter(String paramName, Object value) {
        return new BadRequestException(
            String.format("Parameter '%s' has invalid value: %s", paramName, value)
        );
    }
}
