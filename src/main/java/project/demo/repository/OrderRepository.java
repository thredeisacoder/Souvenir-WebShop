package project.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.demo.model.Order;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Order entities
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    /**
     * Find orders by customer ID
     * 
     * @param customerId the ID of the customer
     * @return a list of orders for the customer
     */
    List<Order> findByCustomerId(Integer customerId);
    
    /**
     * Find orders by customer ID, ordered by order date descending
     * 
     * @param customerId the ID of the customer
     * @return a list of orders for the customer, ordered by date
     */
    List<Order> findByCustomerIdOrderByOrderDateDesc(Integer customerId);
    
    /**
     * Find orders by status
     * 
     * @param orderStatus the status to search for
     * @return a list of orders with the specified status
     */
    List<Order> findByOrderStatus(String orderStatus);
    
    /**
     * Find orders by status and customer ID
     * 
     * @param orderStatus the status to search for
     * @param customerId the ID of the customer
     * @return a list of orders with the specified status for the customer
     */
    List<Order> findByOrderStatusAndCustomerId(String orderStatus, Integer customerId);
    
    /**
     * Find orders created between two dates
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of orders created between the dates
     */
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find orders with pagination
     * 
     * @param customerId the ID of the customer
     * @param pageable the pagination information
     * @return a page of orders for the customer
     */
    Page<Order> findByCustomerId(Integer customerId, Pageable pageable);
    
    /**
     * Find orders by tracking number
     * 
     * @param trackingNumber the tracking number to search for
     * @return a list of orders with the specified tracking number
     */
    List<Order> findByTrackingNumber(String trackingNumber);
    
    /**
     * Count orders by status
     * 
     * @param orderStatus the status to count
     * @return the number of orders with the specified status
     */
    long countByOrderStatus(String orderStatus);
    
    /**
     * Find recent orders for a customer
     * 
     * @param customerId the ID of the customer
     * @param limit the maximum number of orders to return
     * @return a list of recent orders for the customer
     */
    @Query(value = "SELECT o FROM Order o WHERE o.customerId = ?1 ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(Integer customerId, Pageable limit);
}
