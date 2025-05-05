package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an issue with a catalog.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CatalogException extends BusinessLogicException {
    
    private static final String DEFAULT_ERROR_CODE = "CATALOG_ERROR";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public CatalogException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public CatalogException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Creates a new exception for a missing required field.
     * 
     * @param fieldName the name of the missing field
     * @return a new CatalogException
     */
    public static CatalogException missingRequiredField(String fieldName) {
        return new CatalogException(
            "MISSING_REQUIRED_FIELD",
            String.format("%s is required", fieldName)
        );
    }
    
    /**
     * Creates a new exception for a duplicate catalog name.
     * 
     * @param catalogName the duplicate catalog name
     * @return a new CatalogException
     */
    public static CatalogException duplicateName(String catalogName) {
        return new CatalogException(
            "DUPLICATE_CATALOG_NAME",
            String.format("Catalog with name '%s' already exists", catalogName)
        );
    }
    
    /**
     * Creates a new exception for a catalog that cannot be deleted.
     * 
     * @param catalogId the ID of the catalog
     * @param reason the reason why the catalog cannot be deleted
     * @return a new CatalogException
     */
    public static CatalogException cannotDelete(Integer catalogId, String reason) {
        return new CatalogException(
            "CANNOT_DELETE_CATALOG",
            String.format("Cannot delete catalog with ID %d: %s", catalogId, reason)
        );
    }
    
    /**
     * Creates a new exception for an invalid status.
     * 
     * @param status the invalid status
     * @return a new CatalogException
     */
    public static CatalogException invalidStatus(Boolean status) {
        return new CatalogException(
            "INVALID_CATALOG_STATUS",
            String.format("Invalid catalog status: %s", status)
        );
    }
}
