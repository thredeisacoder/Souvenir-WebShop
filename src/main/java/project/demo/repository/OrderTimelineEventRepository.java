package project.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import project.demo.model.OrderTimelineEvent;

/**
 * Repository interface for OrderTimelineEvent entities
 */
@Repository
public interface OrderTimelineEventRepository extends JpaRepository<OrderTimelineEvent, Long> {
    
    /**
     * Find timeline events by order ID
     * 
     * @param orderId the ID of the order
     * @return a list of timeline events for the order
     */
    List<OrderTimelineEvent> findByOrderId(Integer orderId);
    
    /**
     * Find timeline events by order ID, ordered by timestamp descending
     * 
     * @param orderId the ID of the order
     * @return a list of timeline events for the order, ordered by timestamp
     */
    List<OrderTimelineEvent> findByOrderIdOrderByTimestampDesc(Integer orderId);
    
    /**
     * Find timeline events by status
     * 
     * @param status the status to search for
     * @return a list of timeline events with the specified status
     */
    List<OrderTimelineEvent> findByStatus(String status);
    
    /**
     * Find the most recent timeline event for an order
     * 
     * @param orderId the ID of the order
     * @return a list containing the most recent timeline event
     */
    List<OrderTimelineEvent> findFirstByOrderIdOrderByTimestampDesc(Integer orderId);
    
    /**
     * Delete timeline events by order ID
     * 
     * @param orderId the ID of the order
     */
    void deleteByOrderId(Integer orderId);
}