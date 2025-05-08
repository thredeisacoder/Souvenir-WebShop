package project.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import project.demo.model.Product;

/**
 * Repository interface for Product entities
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    /**
     * Find products by catalog ID
     * 
     * @param catalogId the ID of the catalog
     * @return a list of products in the catalog
     */
    List<Product> findByCatalogId(Integer catalogId);
    
    /**
     * Find products by name containing the given keyword
     * 
     * @param keyword the keyword to search for
     * @return a list of products with names containing the keyword
     */
    List<Product> findByProductNameContainingIgnoreCase(String keyword);
    
    /**
     * Find products by price less than or equal to the given value
     * 
     * @param price the maximum price
     * @return a list of products with prices less than or equal to the given value
     */
    List<Product> findByPriceLessThanEqual(BigDecimal price);
    
    /**
     * Find products by price greater than or equal to the given value
     * 
     * @param price the minimum price
     * @return a list of products with prices greater than or equal to the given value
     */
    List<Product> findByPriceGreaterThanEqual(BigDecimal price);
    
    /**
     * Find products by price between two values
     * 
     * @param minPrice the minimum price
     * @param maxPrice the maximum price
     * @return a list of products with prices between the given values
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Find products with pagination
     * 
     * @param catalogId the ID of the catalog
     * @param pageable the pagination information
     * @return a page of products in the catalog
     */
    Page<Product> findByCatalogId(Integer catalogId, Pageable pageable);
    
    /**
     * Search for products by name or description
     * 
     * @param keyword the keyword to search for
     * @return a list of products with names or descriptions containing the keyword
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Product> searchByNameOrDescription(String keyword);
    
    /**
     * Find products by catalog ID and price range
     * 
     * @param catalogId the ID of the catalog
     * @param minPrice the minimum price
     * @param maxPrice the maximum price
     * @return a list of products in the catalog with prices between the given values
     */
    List<Product> findByCatalogIdAndPriceBetween(Integer catalogId, BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Find products by name containing the given keyword with pagination
     * 
     * @param keyword the keyword to search for
     * @param pageable the pagination information
     * @return a page of products with names containing the keyword
     */
    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);
    
    /**
     * Search for products by name or description (with pagination)
     * 
     * @param keyword the keyword to search for
     * @param pageable the pagination information
     * @return a page of products with names or descriptions containing the keyword
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', ?1, '%'))")
    Page<Product> searchByNameOrDescriptionPaginated(String keyword, Pageable pageable);
}
