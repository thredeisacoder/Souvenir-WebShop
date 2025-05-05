package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user is authenticated but not allowed to access a resource or perform an action.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "FORBIDDEN";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public ForbiddenException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public ForbiddenException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Constructs a new exception with a default error code, the specified detail message, and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ForbiddenException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }
    
    /**
     * Creates a new exception for insufficient permissions.
     * 
     * @param action the action being attempted
     * @return a new ForbiddenException
     */
    public static ForbiddenException insufficientPermissions(String action) {
        return new ForbiddenException(
            String.format("You do not have permission to %s", action)
        );
    }
    
    /**
     * Creates a new exception for accessing another user's resource.
     * 
     * @param resourceType the type of resource
     * @return a new ForbiddenException
     */
    public static ForbiddenException accessingOtherUserResource(String resourceType) {
        return new ForbiddenException(
            String.format("You are not allowed to access another user's %s", resourceType)
        );
    }
}
