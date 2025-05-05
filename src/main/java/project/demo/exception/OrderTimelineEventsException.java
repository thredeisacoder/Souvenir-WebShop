package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an issue with an order timeline event.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OrderTimelineEventsException extends BusinessLogicException {
    
    private static final String DEFAULT_ERROR_CODE = "ORDER_TIMELINE_EVENT_ERROR";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public OrderTimelineEventsException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public OrderTimelineEventsException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Creates a new exception for a missing required field.
     * 
     * @param fieldName the name of the missing field
     * @return a new OrderTimelineEventsException
     */
    public static OrderTimelineEventsException missingRequiredField(String fieldName) {
        return new OrderTimelineEventsException(
            "MISSING_REQUIRED_FIELD",
            String.format("%s is required", fieldName)
        );
    }
    
    /**
     * Creates a new exception for an invalid status.
     * 
     * @param status the invalid status
     * @return a new OrderTimelineEventsException
     */
    public static OrderTimelineEventsException invalidStatus(String status) {
        return new OrderTimelineEventsException(
            "INVALID_STATUS",
            String.format("Invalid status: %s", status)
        );
    }
    
    /**
     * Creates a new exception for an event that belongs to another order.
     * 
     * @param eventId the ID of the event
     * @param orderId the ID of the order
     * @return a new OrderTimelineEventsException
     */
    public static OrderTimelineEventsException notOwnedByOrder(Long eventId, Integer orderId) {
        return new OrderTimelineEventsException(
            "EVENT_NOT_OWNED_BY_ORDER",
            String.format("Event with ID %d does not belong to order with ID %d", eventId, orderId)
        );
    }
    
    /**
     * Creates a new exception for an invalid timestamp.
     * 
     * @param timestamp the invalid timestamp
     * @return a new OrderTimelineEventsException
     */
    public static OrderTimelineEventsException invalidTimestamp(String timestamp) {
        return new OrderTimelineEventsException(
            "INVALID_TIMESTAMP",
            String.format("Invalid timestamp: %s", timestamp)
        );
    }
}
