package project.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.demo.model.OrderDetail;

import java.util.List;

/**
 * Repository interface for OrderDetail entities
 */
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    /**
     * Find order details by order ID
     * 
     * @param orderId the ID of the order
     * @return a list of order details for the order
     */
    List<OrderDetail> findByOrderId(Integer orderId);

    /**
     * Find order details by product ID
     * 
     * @param productId the ID of the product
     * @return a list of order details for the product
     */
    List<OrderDetail> findByProductId(Integer productId);

    /**
     * Find order details by order ID and product ID
     * 
     * @param orderId   the ID of the order
     * @param productId the ID of the product
     * @return a list of order details for the order and product
     */
    List<OrderDetail> findByOrderIdAndProductId(Integer orderId, Integer productId);

    /**
     * Count the number of times a product has been ordered
     * 
     * @param productId the ID of the product
     * @return the number of times the product has been ordered
     */
    long countByProductId(Integer productId);

    /**
     * Calculate the total quantity of a product ordered
     * 
     * @param productId the ID of the product
     * @return the total quantity of the product ordered
     */
    @Query("SELECT SUM(od.quantity) FROM OrderDetail od WHERE od.productId = ?1")
    Integer getTotalQuantityByProductId(Integer productId);

    /**
     * Delete order details by order ID
     * 
     * @param orderId the ID of the order
     */
    void deleteByOrderId(Integer orderId);
}
