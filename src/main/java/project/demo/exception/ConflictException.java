package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a request conflicts with the current state of the server.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "CONFLICT";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public ConflictException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public ConflictException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Constructs a new exception with a default error code, the specified detail message, and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ConflictException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }
    
    /**
     * Creates a new exception for a duplicate resource.
     * 
     * @param resourceType the type of resource
     * @param identifier the identifier of the resource
     * @return a new ConflictException
     */
    public static ConflictException duplicateResource(String resourceType, String identifier) {
        return new ConflictException(
            String.format("%s with %s already exists", resourceType, identifier)
        );
    }
    
    /**
     * Creates a new exception for a resource that cannot be deleted due to dependencies.
     * 
     * @param resourceType the type of resource
     * @param resourceId the ID of the resource
     * @param dependencyType the type of dependency
     * @return a new ConflictException
     */
    public static ConflictException cannotDeleteDueToReferences(String resourceType, Object resourceId, String dependencyType) {
        return new ConflictException(
            String.format("Cannot delete %s with ID %s because it is referenced by %s", resourceType, resourceId, dependencyType)
        );
    }
}
