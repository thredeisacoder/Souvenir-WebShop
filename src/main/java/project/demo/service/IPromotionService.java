package project.demo.service;

import project.demo.model.Promotion;
import project.demo.model.ProductPromotion;
import project.demo.model.OrderPromotion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for managing Promotion entities
 */
public interface IPromotionService {
    
    /**
     * Find a promotion by its ID
     * 
     * @param promotionId the ID of the promotion to find
     * @return the promotion if found
     */
    Promotion findById(Integer promotionId);
    
    /**
     * Get all active promotions
     * 
     * @return a list of all active promotions
     */
    List<Promotion> findAllActive();
    
    /**
     * Save a new promotion
     * 
     * @param promotion the promotion to save
     * @return the saved promotion
     */
    Promotion save(Promotion promotion);
    
    /**
     * Update an existing promotion
     * 
     * @param promotion the promotion with updated information
     * @return the updated promotion
     */
    Promotion update(Promotion promotion);
    
    /**
     * Delete a promotion
     * 
     * @param promotionId the ID of the promotion to delete
     */
    void delete(Integer promotionId);
    
    /**
     * Find product promotions by product ID
     * 
     * @param productId the ID of the product
     * @return a list of product promotions
     */
    List<ProductPromotion> findProductPromotionsByProductId(Integer productId);
    
    /**
     * Apply a promotion to a product
     * 
     * @param productId the ID of the product
     * @param promotionId the ID of the promotion
     * @return the created product promotion
     */
    ProductPromotion applyPromotionToProduct(Integer productId, Integer promotionId);
    
    /**
     * Remove a promotion from a product
     * 
     * @param productPromotionId the ID of the product promotion to remove
     */
    void removePromotionFromProduct(Integer productPromotionId);
    
    /**
     * Apply a promotion to an order
     * 
     * @param orderId the ID of the order
     * @param promotionId the ID of the promotion
     * @return the created order promotion
     */
    OrderPromotion applyPromotionToOrder(Integer orderId, Integer promotionId);
    
    /**
     * Calculate the discount amount for an order
     * 
     * @param orderId the ID of the order
     * @param promotionId the ID of the promotion
     * @return the calculated discount amount
     */
    BigDecimal calculateOrderDiscount(Integer orderId, Integer promotionId);
    
    /**
     * Find order promotions by order ID
     * 
     * @param orderId the ID of the order
     * @return a list of order promotions
     */
    List<OrderPromotion> findOrderPromotionsByOrderId(Integer orderId);
    
    /**
     * Activate a promotion
     * 
     * @param promotionId the ID of the promotion to activate
     */
    void activatePromotion(Integer promotionId);
    
    /**
     * Deactivate a promotion
     * 
     * @param promotionId the ID of the promotion to deactivate
     */
    void deactivatePromotion(Integer promotionId);
}
