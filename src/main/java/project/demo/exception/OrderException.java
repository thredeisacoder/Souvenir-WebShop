package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import project.demo.enums.OrderStatus;

/**
 * Exception thrown when there is an issue with an order.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OrderException extends BusinessLogicException {
    
    private static final String DEFAULT_ERROR_CODE = "ORDER_ERROR";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public OrderException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public OrderException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Creates a new exception for an invalid order status.
     * 
     * @param status the invalid status
     * @return a new OrderException
     */
    public static OrderException invalidStatus(String status) {
        return new OrderException(
            "INVALID_ORDER_STATUS",
            String.format("Invalid order status: %s", status)
        );
    }
    
    /**
     * Creates a new exception for an invalid order status transition.
     * 
     * @param currentStatus the current status
     * @param newStatus the new status
     * @return a new OrderException
     */
    public static OrderException invalidStatusTransition(String currentStatus, String newStatus) {
        return new OrderException(
            "INVALID_STATUS_TRANSITION",
            String.format("Cannot change order status from '%s' to '%s'", currentStatus, newStatus)
        );
    }
    
    /**
     * Creates a new exception for an order that cannot be cancelled.
     * 
     * @param orderId the ID of the order
     * @param status the current status
     * @return a new OrderException
     */
    public static OrderException cannotCancel(Integer orderId, String status) {
        return new OrderException(
            "CANNOT_CANCEL_ORDER",
            String.format("Cannot cancel order with ID %d because it is already in '%s' status", orderId, status)
        );
    }
    
    /**
     * Creates a new exception for an empty cart.
     * 
     * @param cartId the ID of the cart
     * @return a new OrderException
     */
    public static OrderException emptyCart(Integer cartId) {
        return new OrderException(
            "EMPTY_CART",
            String.format("Cannot create order from empty cart with ID %d", cartId)
        );
    }
    
    /**
     * Creates a new exception for an order that belongs to another customer.
     * 
     * @param orderId the ID of the order
     * @param customerId the ID of the customer
     * @return a new OrderException
     */
    public static OrderException notOwnedByCustomer(Integer orderId, Integer customerId) {
        return new OrderException(
            "ORDER_NOT_OWNED_BY_CUSTOMER",
            String.format("Order with ID %d does not belong to customer with ID %d", orderId, customerId)
        );
    }
    
    /**
     * Creates a new exception for a missing required field.
     * 
     * @param fieldName the name of the missing field
     * @return a new OrderException
     */
    public static OrderException missingRequiredField(String fieldName) {
        return new OrderException(
            "MISSING_REQUIRED_FIELD",
            String.format("%s is required", fieldName)
        );
    }
    
    /**
     * Creates a new exception for an order that has already been paid.
     * 
     * @param orderId the ID of the order
     * @return a new OrderException
     */
    public static OrderException alreadyPaid(Integer orderId) {
        return new OrderException(
            "ORDER_ALREADY_PAID",
            String.format("Order with ID %d has already been paid", orderId)
        );
    }
    
    /**
     * Creates a new exception for an order that has no payment method.
     * 
     * @param orderId the ID of the order
     * @return a new OrderException
     */
    public static OrderException noPaymentMethod(Integer orderId) {
        return new OrderException(
            "NO_PAYMENT_METHOD",
            String.format("Order with ID %d has no payment method", orderId)
        );
    }
    
    /**
     * Creates a new exception for an order that has no shipping address.
     * 
     * @param orderId the ID of the order
     * @return a new OrderException
     */
    public static OrderException noShippingAddress(Integer orderId) {
        return new OrderException(
            "NO_SHIPPING_ADDRESS",
            String.format("Order with ID %d has no shipping address", orderId)
        );
    }
}
