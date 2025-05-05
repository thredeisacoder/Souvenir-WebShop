package project.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import project.demo.model.OrderTimelineEvent;

/**
 * Service interface for managing OrderTimelineEvent entities
 */
public interface IOrderTimelineEventsService {
    
    /**
     * Find a timeline event by its ID
     * 
     * @param id the ID of the timeline event to find
     * @return the timeline event if found
     */
    OrderTimelineEvent findById(Long id);
    
    /**
     * Find timeline events by order ID
     * 
     * @param orderId the ID of the order
     * @return a list of timeline events for the order
     */
    List<OrderTimelineEvent> findByOrderId(Integer orderId);
    
    /**
     * Find timeline events by order ID, ordered by timestamp
     * 
     * @param orderId the ID of the order
     * @return a list of timeline events for the order, ordered by timestamp
     */
    List<OrderTimelineEvent> findByOrderIdOrderByTimestampDesc(Integer orderId);
    
    /**
     * Add a new timeline event
     * 
     * @param orderId the ID of the order
     * @param status the status of the event
     * @param description the description of the event
     * @param icon the icon for the event
     * @param iconBackgroundColor the background color for the icon
     * @return the created timeline event
     */
    OrderTimelineEvent addEvent(Integer orderId, String status, String description, String icon, String iconBackgroundColor);
    
    /**
     * Add a new timeline event with a specific timestamp
     * 
     * @param orderId the ID of the order
     * @param status the status of the event
     * @param description the description of the event
     * @param icon the icon for the event
     * @param iconBackgroundColor the background color for the icon
     * @param timestamp the timestamp of the event
     * @return the created timeline event
     */
    OrderTimelineEvent addEvent(Integer orderId, String status, String description, String icon, String iconBackgroundColor, LocalDateTime timestamp);
    
    /**
     * Save a timeline event
     * 
     * @param event the timeline event to save
     * @return the saved timeline event
     */
    OrderTimelineEvent save(OrderTimelineEvent event);
    
    /**
     * Delete a timeline event
     * 
     * @param id the ID of the timeline event to delete
     */
    void delete(Long id);
    
    /**
     * Get the latest timeline event for an order
     * 
     * @param orderId the ID of the order
     * @return the latest timeline event
     */
    OrderTimelineEvent getLatestEvent(Integer orderId);
}
