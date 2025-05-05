package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an issue with a shopping cart.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CartException extends BusinessLogicException {
    
    private static final String DEFAULT_ERROR_CODE = "CART_ERROR";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public CartException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public CartException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Creates a new exception for an empty cart.
     * 
     * @return a new CartException
     */
    public static CartException emptyCart() {
        return new CartException(
            "EMPTY_CART",
            "The cart is empty"
        );
    }
    
    /**
     * Creates a new exception for a cart with no selected items.
     * 
     * @return a new CartException
     */
    public static CartException noSelectedItems() {
        return new CartException(
            "NO_SELECTED_ITEMS",
            "No items are selected in the cart"
        );
    }
    
    /**
     * Creates a new exception for an invalid cart status.
     * 
     * @param status the invalid status
     * @return a new CartException
     */
    public static CartException invalidStatus(String status) {
        return new CartException(
            "INVALID_CART_STATUS",
            String.format("Invalid cart status: %s", status)
        );
    }
    
    /**
     * Creates a new exception for a cart that has already been converted to an order.
     * 
     * @param cartId the ID of the cart
     * @return a new CartException
     */
    public static CartException alreadyConverted(Integer cartId) {
        return new CartException(
            "CART_ALREADY_CONVERTED",
            String.format("Cart with ID %d has already been converted to an order", cartId)
        );
    }
    
    /**
     * Creates a new exception for a cart that belongs to another customer.
     * 
     * @param cartId the ID of the cart
     * @param customerId the ID of the customer
     * @return a new CartException
     */
    public static CartException notOwnedByCustomer(Integer cartId, Integer customerId) {
        return new CartException(
            "CART_NOT_OWNED_BY_CUSTOMER",
            String.format("Cart with ID %d does not belong to customer with ID %d", cartId, customerId)
        );
    }
}
