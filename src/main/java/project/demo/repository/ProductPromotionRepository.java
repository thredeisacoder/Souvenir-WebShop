package project.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.demo.model.ProductPromotion;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for ProductPromotion entities
 */
@Repository
public interface ProductPromotionRepository extends JpaRepository<ProductPromotion, Integer> {
    
    /**
     * Find product promotions by product ID
     * 
     * @param productId the ID of the product
     * @return a list of product promotions for the product
     */
    List<ProductPromotion> findByProductId(Integer productId);
    
    /**
     * Find product promotions by promotion ID
     * 
     * @param promotionId the ID of the promotion
     * @return a list of product promotions for the promotion
     */
    List<ProductPromotion> findByPromotionId(Integer promotionId);
    
    /**
     * Find product promotions by status
     * 
     * @param status the status to search for
     * @return a list of product promotions with the specified status
     */
    List<ProductPromotion> findByStatus(String status);
    
    /**
     * Find active product promotions for a product
     * 
     * @param productId the ID of the product
     * @param status the status (typically "active")
     * @param currentDate the current date
     * @return a list of active product promotions for the product
     */
    @Query("SELECT pp FROM ProductPromotion pp WHERE pp.productId = ?1 AND pp.status = ?2 AND pp.startDate <= ?3 AND (pp.endDate IS NULL OR pp.endDate >= ?3)")
    List<ProductPromotion> findActivePromotionsByProductId(Integer productId, String status, LocalDate currentDate);
    
    /**
     * Find product promotions by product ID and promotion ID
     * 
     * @param productId the ID of the product
     * @param promotionId the ID of the promotion
     * @return a list of product promotions for the product and promotion
     */
    List<ProductPromotion> findByProductIdAndPromotionId(Integer productId, Integer promotionId);
    
    /**
     * Find product promotions with end dates before the given date
     * 
     * @param date the date to compare against
     * @return a list of product promotions that have ended
     */
    List<ProductPromotion> findByEndDateBefore(LocalDate date);
    
    /**
     * Find product promotions with start dates after the given date
     * 
     * @param date the date to compare against
     * @return a list of product promotions that have not yet started
     */
    List<ProductPromotion> findByStartDateAfter(LocalDate date);
    
    /**
     * Delete product promotions by product ID
     * 
     * @param productId the ID of the product
     */
    void deleteByProductId(Integer productId);
}
