package project.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.demo.model.OrderPromotion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for OrderPromotion entities
 */
@Repository
public interface OrderPromotionRepository extends JpaRepository<OrderPromotion, Integer> {
    
    /**
     * Find order promotions by order ID
     * 
     * @param orderId the ID of the order
     * @return a list of order promotions for the order
     */
    List<OrderPromotion> findByOrderId(Integer orderId);
    
    /**
     * Find order promotions by promotion ID
     * 
     * @param promotionId the ID of the promotion
     * @return a list of order promotions for the promotion
     */
    List<OrderPromotion> findByPromotionId(Integer promotionId);
    
    /**
     * Find order promotions by order ID and promotion ID
     * 
     * @param orderId the ID of the order
     * @param promotionId the ID of the promotion
     * @return a list of order promotions for the order and promotion
     */
    List<OrderPromotion> findByOrderIdAndPromotionId(Integer orderId, Integer promotionId);
    
    /**
     * Calculate the total discount amount for an order
     * 
     * @param orderId the ID of the order
     * @return the total discount amount
     */
    @Query("SELECT SUM(op.discountAmount) FROM OrderPromotion op WHERE op.orderId = ?1")
    BigDecimal getTotalDiscountByOrderId(Integer orderId);
    
    /**
     * Delete order promotions by order ID
     * 
     * @param orderId the ID of the order
     */
    void deleteByOrderId(Integer orderId);
    
    /**
     * Count the number of times a promotion has been used
     * 
     * @param promotionId the ID of the promotion
     * @return the number of times the promotion has been used
     */
    long countByPromotionId(Integer promotionId);
}
