package project.demo.service.implement;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.demo.exception.ProductException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Product;
import project.demo.model.ProductDetail;
import project.demo.repository.CatalogRepository;
import project.demo.repository.OrderDetailRepository;
import project.demo.repository.ProductDetailRepository;
import project.demo.repository.ProductRepository;
import project.demo.service.IProductService;

/**
 * Implementation of the IProductService interface for managing Product entities
 */
@Service
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final CatalogRepository catalogRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductServiceImpl(ProductRepository productRepository,
            ProductDetailRepository productDetailRepository,
            CatalogRepository catalogRepository,
            OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.productDetailRepository = productDetailRepository;
        this.catalogRepository = catalogRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Product findById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND",
                        "Product not found with ID: " + productId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> findByCatalogId(Integer catalogId) {
        // Check if catalog exists
        if (!catalogRepository.existsById(catalogId)) {
            throw new ResourceNotFoundException("CATALOG_NOT_FOUND",
                    "Catalog not found with ID: " + catalogId);
        }

        return productRepository.findByCatalogId(catalogId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be empty");
        }

        return productRepository.findByProductNameContainingIgnoreCase(keyword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Product save(Product product) {
        // Validate product
        validateProduct(product);

        // Check if catalog exists
        if (!catalogRepository.existsById(product.getCatalogId())) {
            throw new ResourceNotFoundException("CATALOG_NOT_FOUND",
                    "Catalog not found with ID: " + product.getCatalogId());
        }

        // Save product
        return productRepository.save(product);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Product update(Product product) {
        // Check if product exists
        Integer productId = product.getProductId();
        if (productId == null) {
            throw ProductException.missingRequiredField("Product ID");
        }

        findById(productId);

        // Validate product
        validateProduct(product);

        // Check if catalog exists
        if (!catalogRepository.existsById(product.getCatalogId())) {
            throw new ResourceNotFoundException("CATALOG_NOT_FOUND",
                    "Catalog not found with ID: " + product.getCatalogId());
        }

        // Update product
        return productRepository.save(product);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(Integer productId) {
        // Check if product exists
        Product product = findById(productId);

        // Check if product has orders
        long orderCount = orderDetailRepository.countByProductId(productId);
        if (orderCount > 0) {
            throw ProductException.cannotDelete(productId,
                    "Product has been ordered " + orderCount + " times");
        }

        // Delete product details
        Optional<ProductDetail> productDetail = productDetailRepository.findByProductId(productId);
        productDetail.ifPresent(detail -> productDetailRepository.deleteById(detail.getDetailId()));

        // Delete product
        productRepository.deleteById(productId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductDetail getProductDetail(Integer productId) {
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
    public ProductDetail saveProductDetail(ProductDetail productDetail) {
        // Validate product detail
        if (productDetail == null) {
            throw new IllegalArgumentException("Product detail cannot be null");
        }

        if (productDetail.getProductId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        // Check if product exists
        Integer productId = productDetail.getProductId();
        Product product = findById(productId);

        // Check if product detail already exists
        Optional<ProductDetail> existingDetail = productDetailRepository.findByProductId(productId);
        if (existingDetail.isPresent()) {
            throw new IllegalStateException("Product detail already exists for product with ID: " + productId);
        }

        // Validate discount price
        validateDiscountPrice(productDetail.getDiscountPrice(), product.getPrice());

        // Save product detail
        return productDetailRepository.save(productDetail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductDetail updateProductDetail(ProductDetail productDetail) {
        // Validate product detail
        if (productDetail == null) {
            throw new IllegalArgumentException("Product detail cannot be null");
        }

        if (productDetail.getDetailId() == null) {
            throw new IllegalArgumentException("Detail ID cannot be null");
        }

        if (productDetail.getProductId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        // Check if product detail exists
        Integer detailId = productDetail.getDetailId();
        productDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_DETAIL_NOT_FOUND",
                        "Product detail not found with ID: " + detailId));

        // Check if product exists
        Integer productId = productDetail.getProductId();
        Product product = findById(productId);

        // Validate discount price
        validateDiscountPrice(productDetail.getDiscountPrice(), product.getPrice());

        // Update product detail
        return productDetailRepository.save(productDetail);
    }

    /**
     * Validate a product
     *
     * @param product the product to validate
     * @throws ProductException if validation fails
     */
    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        // Validate required fields
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw ProductException.missingRequiredField("Product name");
        }

        if (product.getCatalogId() == null) {
            throw ProductException.missingRequiredField("Catalog ID");
        }

        if (product.getPrice() == null) {
            throw ProductException.missingRequiredField("Price");
        }

        // Validate price
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw ProductException.invalidPrice(product.getPrice().toString());
        }
    }

    /**
     * Validate a discount price
     *
     * @param discountPrice the discount price to validate
     * @param regularPrice  the regular price to compare with
     * @throws ProductException if validation fails
     */
    private void validateDiscountPrice(BigDecimal discountPrice, BigDecimal regularPrice) {
        if (discountPrice == null) {
            return;
        }

        if (discountPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw ProductException.invalidPrice(discountPrice.toString());
        }

        if (discountPrice.compareTo(regularPrice) > 0) {
            throw new IllegalArgumentException(
                    "Discount price (" + discountPrice + ") cannot be greater than regular price (" + regularPrice
                            + ")");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Product> findAllPaginated(Pageable pageable) { // findAll(pageable): trả về tất cả sản phẩm theo trang
        return productRepository.findAll(pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Product> findByCatalogIdPaginated(Integer catalogId, Pageable pageable) {
        // Check if catalog exists
        if (!catalogRepository.existsById(catalogId)) {
            throw new ResourceNotFoundException("CATALOG_NOT_FOUND",
                    "Catalog not found with ID: " + catalogId);
        }

        return productRepository.findByCatalogId(catalogId, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Product> searchByNamePaginated(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be empty");
        }

        return productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Product findProductWithHighestStock() {
        ProductDetail productDetail = productDetailRepository.findProductWithHighestStock();
        if (productDetail == null) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", "No products found in stock");
        }

        return productRepository.findById(productDetail.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND",
                        "Product not found with ID: " + productDetail.getProductId()));
    }
}
