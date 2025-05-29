package project.demo.service.implement;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.demo.exception.ProductDetailException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Product;
import project.demo.model.ProductDetail;
import project.demo.repository.ProductDetailRepository;
import project.demo.repository.ProductRepository;
import project.demo.service.IProductDetailService;

/**
 * Implementation of the IProductDetailService interface for managing ProductDetail entities
 */
@Service
public class ProductDetailServiceImpl implements IProductDetailService {

    private final ProductDetailRepository productDetailRepository;
    private final ProductRepository productRepository;

    public ProductDetailServiceImpl(ProductDetailRepository productDetailRepository,
                                   ProductRepository productRepository) {
        this.productDetailRepository = productDetailRepository;
        this.productRepository = productRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductDetail findById(Integer detailId) {
        return productDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_DETAIL_NOT_FOUND", 
                        "Product detail not found with ID: " + detailId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductDetail findByProductId(Integer productId) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                    "Product not found with ID: " + productId);
        }
        
        return productDetailRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_DETAIL_NOT_FOUND", 
                        "Product detail not found for product with ID: " + productId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductDetail save(ProductDetail productDetail) {
        // Validate product detail
        validateProductDetail(productDetail);
        
        // Check if product exists
        Integer productId = productDetail.getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                        "Product not found with ID: " + productId));
        
        // Check if product detail already exists for this product
        Optional<ProductDetail> existingDetail = productDetailRepository.findByProductId(productId);
        if (existingDetail.isPresent()) {
            throw new ProductDetailException(
                    "PRODUCT_DETAIL_ALREADY_EXISTS",
                    "Product detail already exists for product with ID: " + productId);
        }
        
        // Validate discount price
        validateDiscountPrice(productDetail.getDiscountPrice(), product.getPrice());
        
        return productDetailRepository.save(productDetail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductDetail update(ProductDetail productDetail) {
        // Check if product detail exists
        Integer detailId = productDetail.getDetailId();
        if (detailId == null) {
            throw ProductDetailException.missingRequiredField("Detail ID");
        }
        
        ProductDetail existingDetail = findById(detailId);
        
        // Validate product detail
        validateProductDetail(productDetail);
        
        // Check if product exists
        Integer productId = productDetail.getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                        "Product not found with ID: " + productId));
        
        // Validate discount price
        validateDiscountPrice(productDetail.getDiscountPrice(), product.getPrice());
        
        return productDetailRepository.save(productDetail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductDetail updateDiscountPrice(Integer productId, BigDecimal discountPrice) {
        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                        "Product not found with ID: " + productId));
        
        // Check if product detail exists
        ProductDetail productDetail = findByProductId(productId);
        
        // Validate discount price
        validateDiscountPrice(discountPrice, product.getPrice());
        
        // Update discount price
        productDetail.setDiscountPrice(discountPrice);
        
        return productDetailRepository.save(productDetail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductDetail updateStockQuantity(Integer productId, Integer quantity) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                    "Product not found with ID: " + productId);
        }
        
        // Check if product detail exists
        ProductDetail productDetail = findByProductId(productId);
        
        // Validate quantity
        if (quantity < 0) {
            throw ProductDetailException.invalidStockQuantity(quantity);
        }
        
        // Update quantity
        productDetail.setQuantityInStock(quantity);
        
        return productDetailRepository.save(productDetail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductDetail updateImageUrl(Integer productId, String imageUrl) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                    "Product not found with ID: " + productId);
        }
        
        // Check if product detail exists
        ProductDetail productDetail = findByProductId(productId);
        
        // Validate image URL
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw ProductDetailException.invalidImageUrl(imageUrl);
        }
        
        // Update image URL
        productDetail.setImageUrl(imageUrl);
        
        return productDetailRepository.save(productDetail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInStock(Integer productId) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                    "Product not found with ID: " + productId);
        }
        
        // Check if product detail exists
        Optional<ProductDetail> productDetail = productDetailRepository.findByProductId(productId);
        if (productDetail.isEmpty()) {
            return false;
        }
        
        // Check if product is in stock
        return productDetail.get().getQuantityInStock() != null && 
               productDetail.get().getQuantityInStock() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsReorder(Integer productId) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                    "Product not found with ID: " + productId);
        }
        
        // Check if product detail exists
        Optional<ProductDetail> productDetail = productDetailRepository.findByProductId(productId);
        if (productDetail.isEmpty()) {
            return false;
        }
        
        // Check if product needs reorder
        ProductDetail detail = productDetail.get();
        return detail.getQuantityInStock() != null && 
               detail.getReorderLevel() != null && 
               detail.getQuantityInStock() <= detail.getReorderLevel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getEffectivePrice(Integer productId) {
        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                        "Product not found with ID: " + productId));
        
        // Check if product detail exists
        Optional<ProductDetail> productDetail = productDetailRepository.findByProductId(productId);
        if (productDetail.isEmpty()) {
            return product.getPrice();
        }
        
        // Get effective price
        ProductDetail detail = productDetail.get();
        if (detail.getDiscountPrice() != null && detail.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0) {
            return detail.getDiscountPrice();
        }
        
        return product.getPrice();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getQuantityInStock(Integer productId) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                    "Product not found with ID: " + productId);
        }
        
        // Check if product detail exists
        Optional<ProductDetail> productDetail = productDetailRepository.findByProductId(productId);
        if (productDetail.isEmpty()) {
            return 0;
        }
        
        // Return quantity in stock, or 0 if null
        return productDetail.get().getQuantityInStock() != null ? 
               productDetail.get().getQuantityInStock() : 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductDetail decreaseStockQuantity(Integer productId, Integer quantity) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                    "Product not found with ID: " + productId);
        }
        
        // Check if product detail exists
        ProductDetail productDetail = findByProductId(productId);
        
        // Validate quantity
        if (quantity < 0) {
            throw new ProductDetailException(
                "INVALID_QUANTITY",
                "Decrement quantity cannot be negative: " + quantity
            );
        }
        
        Integer currentStock = productDetail.getQuantityInStock();
        if (currentStock == null) {
            currentStock = 0;
        }
        
        // Ensure we don't go below zero
        if (currentStock < quantity) {
            throw new ProductDetailException(
                "INSUFFICIENT_STOCK",
                "Cannot decrease stock by " + quantity + " as only " + currentStock + " units are available"
            );
        }
        
        // Update quantity
        productDetail.setQuantityInStock(currentStock - quantity);
        
        return productDetailRepository.save(productDetail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductDetail increaseStockQuantity(Integer productId, Integer quantity) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                    "Product not found with ID: " + productId);
        }
        
        // Check if product detail exists
        ProductDetail productDetail = findByProductId(productId);
        
        // Validate quantity
        if (quantity < 0) {
            throw new ProductDetailException(
                "INVALID_QUANTITY",
                "Increment quantity cannot be negative: " + quantity
            );
        }
        
        Integer currentStock = productDetail.getQuantityInStock();
        if (currentStock == null) {
            currentStock = 0;
        }
        
        // Update quantity
        productDetail.setQuantityInStock(currentStock + quantity);
        
        return productDetailRepository.save(productDetail);
    }

    /**
     * Validate a product detail
     * 
     * @param productDetail the product detail to validate
     * @throws ProductDetailException if validation fails
     */
    private void validateProductDetail(ProductDetail productDetail) {
        if (productDetail == null) {
            throw new IllegalArgumentException("Product detail cannot be null");
        }
        
        if (productDetail.getProductId() == null) {
            throw ProductDetailException.missingRequiredField("Product ID");
        }
        
        // Validate quantity
        if (productDetail.getQuantityInStock() != null && productDetail.getQuantityInStock() < 0) {
            throw ProductDetailException.invalidStockQuantity(productDetail.getQuantityInStock());
        }
        
        // Validate reorder level
        if (productDetail.getReorderLevel() != null && productDetail.getReorderLevel() < 0) {
            throw ProductDetailException.invalidReorderLevel(productDetail.getReorderLevel());
        }
    }
    
    /**
     * Validate a discount price
     * 
     * @param discountPrice the discount price to validate
     * @param regularPrice the regular price to compare with
     * @throws ProductDetailException if validation fails
     */
    private void validateDiscountPrice(BigDecimal discountPrice, BigDecimal regularPrice) {
        if (discountPrice == null) {
            return;
        }
        
        if (discountPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw ProductDetailException.invalidDiscountPrice(discountPrice.toString());
        }
        
        if (discountPrice.compareTo(regularPrice) > 0) {
            throw ProductDetailException.discountPriceGreaterThanRegularPrice(
                    discountPrice.toString(), regularPrice.toString());
        }
    }
}
