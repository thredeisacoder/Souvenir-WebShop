package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "RESOURCE_NOT_FOUND";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public ResourceNotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Constructs a new exception with a default error code, the specified detail message, and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }
    
    /**
     * Creates a new exception for a resource of the specified type with the specified ID.
     * 
     * @param resourceType the type of resource
     * @param resourceId the ID of the resource
     * @return a new ResourceNotFoundException
     */
    public static ResourceNotFoundException forResource(String resourceType, Object resourceId) {
        return new ResourceNotFoundException(
            String.format("%s not found with ID: %s", resourceType, resourceId)
        );
    }
}
