package project.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.demo.model.Promotion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Promotion entities
 */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    
    /**
     * Find promotions by name
     * 
     * @param promotionName the name to search for
     * @return a list of promotions with the specified name
     */
    List<Promotion> findByPromotionName(String promotionName);
    
    /**
     * Find promotions by status
     * 
     * @param status the status to search for
     * @return a list of promotions with the specified status
     */
    List<Promotion> findByStatus(String status);
    
    /**
     * Find active promotions
     * 
     * @param status the status (typically "active")
     * @param currentDate the current date
     * @return a list of active promotions
     */
    @Query("SELECT p FROM Promotion p WHERE p.status = ?1 AND p.startDate <= ?2 AND (p.endDate IS NULL OR p.endDate >= ?2)")
    List<Promotion> findActivePromotions(String status, LocalDate currentDate);
    
    /**
     * Find promotions by discount type
     * 
     * @param discountType the discount type to search for
     * @return a list of promotions with the specified discount type
     */
    List<Promotion> findByDiscountType(String discountType);
    
    /**
     * Find promotions with end dates before the given date
     * 
     * @param date the date to compare against
     * @return a list of promotions that have ended
     */
    List<Promotion> findByEndDateBefore(LocalDate date);
    
    /**
     * Find promotions with start dates after the given date
     * 
     * @param date the date to compare against
     * @return a list of promotions that have not yet started
     */
    List<Promotion> findByStartDateAfter(LocalDate date);
    
    /**
     * Find promotions by name containing the given keyword
     * 
     * @param keyword the keyword to search for
     * @return a list of promotions with names containing the keyword
     */
    List<Promotion> findByPromotionNameContainingIgnoreCase(String keyword);
}
