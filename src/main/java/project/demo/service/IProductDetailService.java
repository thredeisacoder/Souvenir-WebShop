package project.demo.service;

import java.math.BigDecimal;

import project.demo.model.ProductDetail;

/**
 * Service interface for managing ProductDetail entities
 */
public interface IProductDetailService {
    
    /**
     * Find product details by detail ID
     * 
     * @param detailId the ID of the product detail to find
     * @return the product detail if found
     */
    ProductDetail findById(Integer detailId);
    
    /**
     * Find product details by product ID
     * 
     * @param productId the ID of the product
     * @return the product detail if found
     */
    ProductDetail findByProductId(Integer productId);
    
    /**
     * Save new product details
     * 
     * @param productDetail the product detail to save
     * @return the saved product detail
     */
    ProductDetail save(ProductDetail productDetail);
    
    /**
     * Update existing product details
     * 
     * @param productDetail the product detail with updated information
     * @return the updated product detail
     */
    ProductDetail update(ProductDetail productDetail);
    
    /**
     * Update the discount price of a product
     * 
     * @param productId the ID of the product
     * @param discountPrice the new discount price
     * @return the updated product detail
     */
    ProductDetail updateDiscountPrice(Integer productId, BigDecimal discountPrice);
    
    /**
     * Update the stock quantity of a product
     * 
     * @param productId the ID of the product
     * @param quantity the new quantity
     * @return the updated product detail
     */
    ProductDetail updateStockQuantity(Integer productId, Integer quantity);
    
    /**
     * Decrease the stock quantity of a product
     * 
     * @param productId the ID of the product
     * @param quantity the quantity to decrease
     * @return the updated product detail
     * @throws project.demo.exception.ProductDetailException if resulting quantity would be negative
     */
    ProductDetail decreaseStockQuantity(Integer productId, Integer quantity);
    
    /**
     * Increase the stock quantity of a product
     * 
     * @param productId the ID of the product
     * @param quantity the quantity to increase
     * @return the updated product detail
     */
    ProductDetail increaseStockQuantity(Integer productId, Integer quantity);
    
    /**
     * Update the image URL of a product
     * 
     * @param productId the ID of the product
     * @param imageUrl the new image URL
     * @return the updated product detail
     */
    ProductDetail updateImageUrl(Integer productId, String imageUrl);
    
    /**
     * Check if a product is in stock
     * 
     * @param productId the ID of the product
     * @return true if the product is in stock
     */
    boolean isInStock(Integer productId);
    
    /**
     * Check if a product needs to be reordered
     * 
     * @param productId the ID of the product
     * @return true if the product needs to be reordered
     */
    boolean needsReorder(Integer productId);
    
    /**
     * Get the effective price of a product (considering discount)
     * 
     * @param productId the ID of the product
     * @return the effective price
     */
    BigDecimal getEffectivePrice(Integer productId);
    
    /**
     * Get the current stock quantity for a product
     * 
     * @param productId the product ID to check
     * @return the current quantity in stock, or 0 if no stock info is available
     * @throws ResourceNotFoundException if the product does not exist
     */
    Integer getQuantityInStock(Integer productId);
}
