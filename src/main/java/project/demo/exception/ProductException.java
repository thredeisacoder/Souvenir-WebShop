package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an issue with a product.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductException extends BusinessLogicException {
    
    private static final String DEFAULT_ERROR_CODE = "PRODUCT_ERROR";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public ProductException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public ProductException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Creates a new exception for a missing required field.
     * 
     * @param fieldName the name of the missing field
     * @return a new ProductException
     */
    public static ProductException missingRequiredField(String fieldName) {
        return new ProductException(
            "MISSING_REQUIRED_FIELD",
            String.format("%s is required", fieldName)
        );
    }
    
    /**
     * Creates a new exception for a duplicate product name.
     * 
     * @param productName the duplicate product name
     * @return a new ProductException
     */
    public static ProductException duplicateName(String productName) {
        return new ProductException(
            "DUPLICATE_PRODUCT_NAME",
            String.format("Product with name '%s' already exists", productName)
        );
    }
    
    /**
     * Creates a new exception for an invalid price.
     * 
     * @param price the invalid price
     * @return a new ProductException
     */
    public static ProductException invalidPrice(String price) {
        return new ProductException(
            "INVALID_PRICE",
            String.format("Invalid price: %s", price)
        );
    }
    
    /**
     * Creates a new exception for a product that cannot be deleted.
     * 
     * @param productId the ID of the product
     * @param reason the reason why the product cannot be deleted
     * @return a new ProductException
     */
    public static ProductException cannotDelete(Integer productId, String reason) {
        return new ProductException(
            "CANNOT_DELETE_PRODUCT",
            String.format("Cannot delete product with ID %d: %s", productId, reason)
        );
    }
    
    /**
     * Creates a new exception for a product that is out of stock.
     * 
     * @param productId the ID of the product
     * @return a new ProductException
     */
    public static ProductException outOfStock(Integer productId) {
        return new ProductException(
            "PRODUCT_OUT_OF_STOCK",
            String.format("Product with ID %d is out of stock", productId)
        );
    }
    
    /**
     * Creates a new exception for insufficient stock.
     * 
     * @param productId the ID of the product
     * @param requested the requested quantity
     * @param available the available quantity
     * @return a new ProductException
     */
    public static ProductException insufficientStock(Integer productId, Integer requested, Integer available) {
        return new ProductException(
            "INSUFFICIENT_STOCK",
            String.format("Insufficient stock for product with ID %d. Requested: %d, Available: %d", productId, requested, available)
        );
    }
}
