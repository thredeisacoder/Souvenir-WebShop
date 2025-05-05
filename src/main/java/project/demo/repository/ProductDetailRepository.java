package project.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.demo.model.ProductDetail;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductDetail entities
 */
@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Integer> {
    
    /**
     * Find product detail by product ID
     * 
     * @param productId the ID of the product
     * @return an Optional containing the product detail if found
     */
    Optional<ProductDetail> findByProductId(Integer productId);
    
    /**
     * Find products with stock quantity less than the given value
     * 
     * @param quantity the quantity threshold
     * @return a list of product details with stock quantities less than the given value
     */
    List<ProductDetail> findByQuantityInStockLessThan(Integer quantity);
    
    /**
     * Find products with stock quantity less than or equal to their reorder level
     * 
     * @return a list of product details that need to be reordered
     */
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.quantityInStock <= pd.reorderLevel")
    List<ProductDetail> findProductsNeedingReorder();
    
    /**
     * Find products with discount prices
     * 
     * @return a list of product details with discount prices
     */
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.discountPrice IS NOT NULL")
    List<ProductDetail> findProductsWithDiscount();
    
    /**
     * Find products with stock quantity greater than zero
     * 
     * @return a list of product details for products in stock
     */
    List<ProductDetail> findByQuantityInStockGreaterThan(Integer quantity);
    
    /**
     * Find products with images
     * 
     * @return a list of product details with image URLs
     */
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.imageUrl IS NOT NULL")
    List<ProductDetail> findProductsWithImages();
}
