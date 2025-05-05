package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a service is temporarily unavailable.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "SERVICE_UNAVAILABLE";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public ServiceUnavailableException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public ServiceUnavailableException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Constructs a new exception with a default error code, the specified detail message, and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ServiceUnavailableException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }
    
    /**
     * Creates a new exception for a service that is under maintenance.
     * 
     * @param serviceName the name of the service
     * @return a new ServiceUnavailableException
     */
    public static ServiceUnavailableException underMaintenance(String serviceName) {
        return new ServiceUnavailableException(
            String.format("%s is currently under maintenance. Please try again later.", serviceName)
        );
    }
    
    /**
     * Creates a new exception for a service that is overloaded.
     * 
     * @param serviceName the name of the service
     * @return a new ServiceUnavailableException
     */
    public static ServiceUnavailableException overloaded(String serviceName) {
        return new ServiceUnavailableException(
            String.format("%s is currently experiencing high load. Please try again later.", serviceName)
        );
    }
}
