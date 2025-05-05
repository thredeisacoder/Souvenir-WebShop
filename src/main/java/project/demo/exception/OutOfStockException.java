package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a product is out of stock.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class OutOfStockException extends BusinessLogicException {
    
    private static final String DEFAULT_ERROR_CODE = "OUT_OF_STOCK";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public OutOfStockException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public OutOfStockException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Creates a new exception for a product that is out of stock.
     * 
     * @param productName the name of the product
     * @return a new OutOfStockException
     */
    public static OutOfStockException forProduct(String productName) {
        return new OutOfStockException(
            String.format("Product '%s' is out of stock", productName)
        );
    }
    
    /**
     * Creates a new exception for a product with insufficient stock.
     * 
     * @param productName the name of the product
     * @param requested the requested quantity
     * @param available the available quantity
     * @return a new OutOfStockException
     */
    public static OutOfStockException insufficientStock(String productName, int requested, int available) {
        return new OutOfStockException(
            String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d", productName, requested, available)
        );
    }
}
