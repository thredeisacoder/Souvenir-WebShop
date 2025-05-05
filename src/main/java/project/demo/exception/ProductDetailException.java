package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an issue with a product detail.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductDetailException extends BusinessLogicException {
    
    private static final String DEFAULT_ERROR_CODE = "PRODUCT_DETAIL_ERROR";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public ProductDetailException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public ProductDetailException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Creates a new exception for a missing required field.
     * 
     * @param fieldName the name of the missing field
     * @return a new ProductDetailException
     */
    public static ProductDetailException missingRequiredField(String fieldName) {
        return new ProductDetailException(
            "MISSING_REQUIRED_FIELD",
            String.format("%s is required", fieldName)
        );
    }
    
    /**
     * Creates a new exception for an invalid discount price.
     * 
     * @param discountPrice the invalid discount price
     * @return a new ProductDetailException
     */
    public static ProductDetailException invalidDiscountPrice(String discountPrice) {
        return new ProductDetailException(
            "INVALID_DISCOUNT_PRICE",
            String.format("Invalid discount price: %s", discountPrice)
        );
    }
    
    /**
     * Creates a new exception for a discount price greater than the regular price.
     * 
     * @param discountPrice the discount price
     * @param regularPrice the regular price
     * @return a new ProductDetailException
     */
    public static ProductDetailException discountPriceGreaterThanRegularPrice(String discountPrice, String regularPrice) {
        return new ProductDetailException(
            "DISCOUNT_PRICE_GREATER_THAN_REGULAR_PRICE",
            String.format("Discount price (%s) cannot be greater than regular price (%s)", discountPrice, regularPrice)
        );
    }
    
    /**
     * Creates a new exception for an invalid stock quantity.
     * 
     * @param quantity the invalid quantity
     * @return a new ProductDetailException
     */
    public static ProductDetailException invalidStockQuantity(Integer quantity) {
        return new ProductDetailException(
            "INVALID_STOCK_QUANTITY",
            String.format("Invalid stock quantity: %d", quantity)
        );
    }
    
    /**
     * Creates a new exception for an invalid reorder level.
     * 
     * @param reorderLevel the invalid reorder level
     * @return a new ProductDetailException
     */
    public static ProductDetailException invalidReorderLevel(Integer reorderLevel) {
        return new ProductDetailException(
            "INVALID_REORDER_LEVEL",
            String.format("Invalid reorder level: %d", reorderLevel)
        );
    }
    
    /**
     * Creates a new exception for an invalid image URL.
     * 
     * @param imageUrl the invalid image URL
     * @return a new ProductDetailException
     */
    public static ProductDetailException invalidImageUrl(String imageUrl) {
        return new ProductDetailException(
            "INVALID_IMAGE_URL",
            String.format("Invalid image URL: %s", imageUrl)
        );
    }
}
