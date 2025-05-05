package project.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.demo.model.Product;
import project.demo.model.ProductDetail;

import java.util.List;

/**
 * Service interface for managing Product entities
 */
public interface IProductService {

    /**
     * Find a product by its ID
     *
     * @param productId the ID of the product to find
     * @return the product if found
     */
    Product findById(Integer productId);

    /**
     * Get all products
     *
     * @return a list of all products
     */
    List<Product> findAll();

    /**
     * Find products by catalog ID
     *
     * @param catalogId the ID of the catalog
     * @return a list of products in the catalog
     */
    List<Product> findByCatalogId(Integer catalogId);

    /**
     * Search for products by name
     *
     * @param keyword the search keyword
     * @return a list of products matching the search
     */
    List<Product> searchByName(String keyword);

    /**
     * Save a new product
     *
     * @param product the product to save
     * @return the saved product
     */
    Product save(Product product);

    /**
     * Update an existing product
     *
     * @param product the product with updated information
     * @return the updated product
     */
    Product update(Product product);

    /**
     * Delete a product
     *
     * @param productId the ID of the product to delete
     */
    void delete(Integer productId);

    /**
     * Get product details
     *
     * @param productId the ID of the product
     * @return the product details
     */
    ProductDetail getProductDetail(Integer productId);

    /**
     * Save product details
     *
     * @param productDetail the product details to save
     * @return the saved product details
     */
    ProductDetail saveProductDetail(ProductDetail productDetail);

    /**
     * Update product details
     *
     * @param productDetail the product details with updated information
     * @return the updated product details
     */
    ProductDetail updateProductDetail(ProductDetail productDetail);

    /**
     * Get all products with pagination
     *
     * @param pageable the pagination information
     * @return a page of products
     */
    Page<Product> findAllPaginated(Pageable pageable);

    /**
     * Find products by catalog ID with pagination
     *
     * @param catalogId the ID of the catalog
     * @param pageable  the pagination information
     * @return a page of products in the catalog
     */
    Page<Product> findByCatalogIdPaginated(Integer catalogId, Pageable pageable);

    /**
     * Search for products by name with pagination
     *
     * @param keyword  the search keyword
     * @param pageable the pagination information
     * @return a page of products matching the search
     */
    Page<Product> searchByNamePaginated(String keyword, Pageable pageable);
}
