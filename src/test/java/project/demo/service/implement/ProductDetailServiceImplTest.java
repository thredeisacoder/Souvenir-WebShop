package project.demo.service.implement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import project.demo.exception.ProductDetailException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Product;
import project.demo.model.ProductDetail;
import project.demo.repository.ProductDetailRepository;
import project.demo.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductDetailServiceImplTest {

    @Mock
    private ProductDetailRepository productDetailRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductDetailServiceImpl productDetailService;

    private ProductDetail testProductDetail;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test product
        testProduct = new Product();
        testProduct.setProductId(1);
        testProduct.setCatalogId(1);
        testProduct.setProductName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("100.00"));

        // Setup test product detail
        testProductDetail = new ProductDetail();
        testProductDetail.setDetailId(1);
        testProductDetail.setProductId(1);
        testProductDetail.setDiscountPrice(new BigDecimal("80.00"));
        testProductDetail.setQuantityInStock(10);
        testProductDetail.setReorderLevel(5);
        testProductDetail.setImageUrl("http://example.com/image.jpg");
    }

    @Test
    void findById_ExistingDetail_ReturnsDetail() {
        // Arrange
        when(productDetailRepository.findById(1)).thenReturn(Optional.of(testProductDetail));

        // Act
        ProductDetail result = productDetailService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProductDetail.getDetailId(), result.getDetailId());
        verify(productDetailRepository, times(1)).findById(1);
    }

    @Test
    void findById_NonExistingDetail_ThrowsException() {
        // Arrange
        when(productDetailRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productDetailService.findById(999));
        verify(productDetailRepository, times(1)).findById(999);
    }

    @Test
    void findByProductId_ExistingProduct_ReturnsDetail() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        // Act
        ProductDetail result = productDetailService.findByProductId(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProductDetail.getDetailId(), result.getDetailId());
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
    }

    @Test
    void findByProductId_NonExistingProduct_ThrowsException() {
        // Arrange
        when(productRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productDetailService.findByProductId(999));
        verify(productRepository, times(1)).existsById(999);
        verify(productDetailRepository, never()).findByProductId(anyInt());
    }

    @Test
    void findByProductId_NonExistingDetail_ThrowsException() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productDetailService.findByProductId(1));
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
    }

    @Test
    void save_ValidDetail_SavesDetail() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.empty());
        when(productDetailRepository.save(any(ProductDetail.class))).thenReturn(testProductDetail);

        // Act
        ProductDetail result = productDetailService.save(testProductDetail);

        // Assert
        assertNotNull(result);
        assertEquals(testProductDetail.getDetailId(), result.getDetailId());
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, times(1)).save(testProductDetail);
    }

    @Test
    void save_NonExistingProduct_ThrowsException() {
        // Arrange
        when(productRepository.findById(999)).thenReturn(Optional.empty());
        
        ProductDetail detail = new ProductDetail();
        detail.setProductId(999);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productDetailService.save(detail));
        verify(productRepository, times(1)).findById(999);
        verify(productDetailRepository, never()).save(any(ProductDetail.class));
    }

    @Test
    void save_ExistingDetail_ThrowsException() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        // Act & Assert
        assertThrows(ProductDetailException.class, () -> productDetailService.save(testProductDetail));
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, never()).save(any(ProductDetail.class));
    }

    @Test
    void save_InvalidDiscountPrice_ThrowsException() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.empty());
        
        testProductDetail.setDiscountPrice(new BigDecimal("150.00")); // Greater than regular price

        // Act & Assert
        assertThrows(ProductDetailException.class, () -> productDetailService.save(testProductDetail));
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, never()).save(any(ProductDetail.class));
    }

    @Test
    void update_ValidDetail_UpdatesDetail() {
        // Arrange
        when(productDetailRepository.findById(1)).thenReturn(Optional.of(testProductDetail));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productDetailRepository.save(any(ProductDetail.class))).thenReturn(testProductDetail);

        // Act
        ProductDetail result = productDetailService.update(testProductDetail);

        // Assert
        assertNotNull(result);
        assertEquals(testProductDetail.getDetailId(), result.getDetailId());
        verify(productDetailRepository, times(1)).findById(1);
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).save(testProductDetail);
    }

    @Test
    void update_NonExistingDetail_ThrowsException() {
        // Arrange
        when(productDetailRepository.findById(999)).thenReturn(Optional.empty());
        
        ProductDetail detail = new ProductDetail();
        detail.setDetailId(999);
        detail.setProductId(1);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productDetailService.update(detail));
        verify(productDetailRepository, times(1)).findById(999);
        verify(productDetailRepository, never()).save(any(ProductDetail.class));
    }

    @Test
    void updateDiscountPrice_ValidPrice_UpdatesPrice() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));
        when(productDetailRepository.save(any(ProductDetail.class))).thenReturn(testProductDetail);

        BigDecimal newDiscountPrice = new BigDecimal("90.00");

        // Act
        ProductDetail result = productDetailService.updateDiscountPrice(1, newDiscountPrice);

        // Assert
        assertNotNull(result);
        assertEquals(newDiscountPrice, testProductDetail.getDiscountPrice());
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, times(1)).save(testProductDetail);
    }

    @Test
    void updateDiscountPrice_InvalidPrice_ThrowsException() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        BigDecimal invalidDiscountPrice = new BigDecimal("150.00"); // Greater than regular price

        // Act & Assert
        assertThrows(ProductDetailException.class, () -> productDetailService.updateDiscountPrice(1, invalidDiscountPrice));
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, never()).save(any(ProductDetail.class));
    }

    @Test
    void updateStockQuantity_ValidQuantity_UpdatesQuantity() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));
        when(productDetailRepository.save(any(ProductDetail.class))).thenReturn(testProductDetail);

        Integer newQuantity = 20;

        // Act
        ProductDetail result = productDetailService.updateStockQuantity(1, newQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(newQuantity, testProductDetail.getQuantityInStock());
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, times(1)).save(testProductDetail);
    }

    @Test
    void updateStockQuantity_InvalidQuantity_ThrowsException() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        Integer invalidQuantity = -10;

        // Act & Assert
        assertThrows(ProductDetailException.class, () -> productDetailService.updateStockQuantity(1, invalidQuantity));
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, never()).save(any(ProductDetail.class));
    }

    @Test
    void updateImageUrl_ValidUrl_UpdatesUrl() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));
        when(productDetailRepository.save(any(ProductDetail.class))).thenReturn(testProductDetail);

        String newImageUrl = "http://example.com/new-image.jpg";

        // Act
        ProductDetail result = productDetailService.updateImageUrl(1, newImageUrl);

        // Assert
        assertNotNull(result);
        assertEquals(newImageUrl, testProductDetail.getImageUrl());
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, times(1)).save(testProductDetail);
    }

    @Test
    void updateImageUrl_InvalidUrl_ThrowsException() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        String invalidImageUrl = "";

        // Act & Assert
        assertThrows(ProductDetailException.class, () -> productDetailService.updateImageUrl(1, invalidImageUrl));
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, never()).save(any(ProductDetail.class));
    }

    @Test
    void isInStock_ProductInStock_ReturnsTrue() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        // Act
        boolean result = productDetailService.isInStock(1);

        // Assert
        assertTrue(result);
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
    }

    @Test
    void isInStock_ProductOutOfStock_ReturnsFalse() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        
        testProductDetail.setQuantityInStock(0);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        // Act
        boolean result = productDetailService.isInStock(1);

        // Assert
        assertFalse(result);
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
    }

    @Test
    void needsReorder_ProductNeedsReorder_ReturnsTrue() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        
        testProductDetail.setQuantityInStock(5); // Equal to reorder level
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        // Act
        boolean result = productDetailService.needsReorder(1);

        // Assert
        assertTrue(result);
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
    }

    @Test
    void needsReorder_ProductDoesNotNeedReorder_ReturnsFalse() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        
        testProductDetail.setQuantityInStock(10); // Greater than reorder level
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        // Act
        boolean result = productDetailService.needsReorder(1);

        // Assert
        assertFalse(result);
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
    }

    @Test
    void getEffectivePrice_WithDiscount_ReturnsDiscountPrice() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        // Act
        BigDecimal result = productDetailService.getEffectivePrice(1);

        // Assert
        assertEquals(testProductDetail.getDiscountPrice(), result);
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
    }

    @Test
    void getEffectivePrice_WithoutDiscount_ReturnsRegularPrice() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        
        testProductDetail.setDiscountPrice(null);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        // Act
        BigDecimal result = productDetailService.getEffectivePrice(1);

        // Assert
        assertEquals(testProduct.getPrice(), result);
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
    }

    @Test
    void getEffectivePrice_NoProductDetail_ReturnsRegularPrice() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.empty());

        // Act
        BigDecimal result = productDetailService.getEffectivePrice(1);

        // Assert
        assertEquals(testProduct.getPrice(), result);
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
    }
}
