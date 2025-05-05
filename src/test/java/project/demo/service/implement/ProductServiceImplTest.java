package project.demo.service.implement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import project.demo.exception.ProductException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Product;
import project.demo.model.ProductDetail;
import project.demo.repository.CatalogRepository;
import project.demo.repository.OrderDetailRepository;
import project.demo.repository.ProductDetailRepository;
import project.demo.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductDetailRepository productDetailRepository;

    @Mock
    private CatalogRepository catalogRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductDetail testProductDetail;

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
    void findById_ExistingProduct_ReturnsProduct() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));

        // Act
        Product result = productService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getProductId(), result.getProductId());
        verify(productRepository, times(1)).findById(1);
    }

    @Test
    void findById_NonExistingProduct_ThrowsException() {
        // Arrange
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.findById(999));
        verify(productRepository, times(1)).findById(999);
    }

    @Test
    void findAll_ReturnsAllProducts() {
        // Arrange
        List<Product> products = new ArrayList<>();
        products.add(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getProductId(), result.get(0).getProductId());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void findByCatalogId_ExistingCatalog_ReturnsProducts() {
        // Arrange
        List<Product> products = new ArrayList<>();
        products.add(testProduct);
        when(catalogRepository.existsById(1)).thenReturn(true);
        when(productRepository.findByCatalogId(1)).thenReturn(products);

        // Act
        List<Product> result = productService.findByCatalogId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getProductId(), result.get(0).getProductId());
        verify(catalogRepository, times(1)).existsById(1);
        verify(productRepository, times(1)).findByCatalogId(1);
    }

    @Test
    void findByCatalogId_NonExistingCatalog_ThrowsException() {
        // Arrange
        when(catalogRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.findByCatalogId(999));
        verify(catalogRepository, times(1)).existsById(999);
        verify(productRepository, never()).findByCatalogId(anyInt());
    }

    @Test
    void searchByName_ValidKeyword_ReturnsProducts() {
        // Arrange
        List<Product> products = new ArrayList<>();
        products.add(testProduct);
        when(productRepository.findByProductNameContainingIgnoreCase("Test")).thenReturn(products);

        // Act
        List<Product> result = productService.searchByName("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getProductId(), result.get(0).getProductId());
        verify(productRepository, times(1)).findByProductNameContainingIgnoreCase("Test");
    }

    @Test
    void searchByName_EmptyKeyword_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> productService.searchByName(""));
        verify(productRepository, never()).findByProductNameContainingIgnoreCase(anyString());
    }

    @Test
    void save_ValidProduct_SavesProduct() {
        // Arrange
        when(catalogRepository.existsById(1)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.save(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getProductId(), result.getProductId());
        verify(catalogRepository, times(1)).existsById(1);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void save_NonExistingCatalog_ThrowsException() {
        // Arrange
        when(catalogRepository.existsById(1)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.save(testProduct));
        verify(catalogRepository, times(1)).existsById(1);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void save_MissingProductName_ThrowsException() {
        // Arrange
        testProduct.setProductName(null);

        // Act & Assert
        assertThrows(ProductException.class, () -> productService.save(testProduct));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void save_InvalidPrice_ThrowsException() {
        // Arrange
        testProduct.setPrice(new BigDecimal("-10.00"));

        // Act & Assert
        assertThrows(ProductException.class, () -> productService.save(testProduct));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void update_ValidProduct_UpdatesProduct() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(catalogRepository.existsById(1)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.update(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getProductId(), result.getProductId());
        verify(productRepository, times(1)).findById(1);
        verify(catalogRepository, times(1)).existsById(1);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void update_NonExistingProduct_ThrowsException() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.update(testProduct));
        verify(productRepository, times(1)).findById(1);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void delete_ProductWithNoOrders_DeletesProduct() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(orderDetailRepository.countByProductId(1)).thenReturn(0L);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));
        doNothing().when(productDetailRepository).deleteById(1);
        doNothing().when(productRepository).deleteById(1);

        // Act
        productService.delete(1);

        // Assert
        verify(productRepository, times(1)).findById(1);
        verify(orderDetailRepository, times(1)).countByProductId(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, times(1)).deleteById(1);
        verify(productRepository, times(1)).deleteById(1);
    }

    @Test
    void delete_ProductWithOrders_ThrowsException() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(orderDetailRepository.countByProductId(1)).thenReturn(5L);

        // Act & Assert
        assertThrows(ProductException.class, () -> productService.delete(1));
        verify(productRepository, times(1)).findById(1);
        verify(orderDetailRepository, times(1)).countByProductId(1);
        verify(productRepository, never()).deleteById(anyInt());
    }

    @Test
    void getProductDetail_ExistingDetail_ReturnsDetail() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        // Act
        ProductDetail result = productService.getProductDetail(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProductDetail.getDetailId(), result.getDetailId());
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
    }

    @Test
    void getProductDetail_NonExistingProduct_ThrowsException() {
        // Arrange
        when(productRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductDetail(999));
        verify(productRepository, times(1)).existsById(999);
        verify(productDetailRepository, never()).findByProductId(anyInt());
    }

    @Test
    void getProductDetail_NonExistingDetail_ThrowsException() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductDetail(1));
        verify(productRepository, times(1)).existsById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
    }

    @Test
    void saveProductDetail_ValidDetail_SavesDetail() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.empty());
        when(productDetailRepository.save(any(ProductDetail.class))).thenReturn(testProductDetail);

        // Act
        ProductDetail result = productService.saveProductDetail(testProductDetail);

        // Assert
        assertNotNull(result);
        assertEquals(testProductDetail.getDetailId(), result.getDetailId());
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, times(1)).save(testProductDetail);
    }

    @Test
    void saveProductDetail_ExistingDetail_ThrowsException() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productDetailRepository.findByProductId(1)).thenReturn(Optional.of(testProductDetail));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> productService.saveProductDetail(testProductDetail));
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).findByProductId(1);
        verify(productDetailRepository, never()).save(any(ProductDetail.class));
    }

    @Test
    void updateProductDetail_ValidDetail_UpdatesDetail() {
        // Arrange
        when(productDetailRepository.findById(1)).thenReturn(Optional.of(testProductDetail));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productDetailRepository.save(any(ProductDetail.class))).thenReturn(testProductDetail);

        // Act
        ProductDetail result = productService.updateProductDetail(testProductDetail);

        // Assert
        assertNotNull(result);
        assertEquals(testProductDetail.getDetailId(), result.getDetailId());
        verify(productDetailRepository, times(1)).findById(1);
        verify(productRepository, times(1)).findById(1);
        verify(productDetailRepository, times(1)).save(testProductDetail);
    }

    @Test
    void updateProductDetail_NonExistingDetail_ThrowsException() {
        // Arrange
        when(productDetailRepository.findById(999)).thenReturn(Optional.empty());
        
        ProductDetail detail = new ProductDetail();
        detail.setDetailId(999);
        detail.setProductId(1);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.updateProductDetail(detail));
        verify(productDetailRepository, times(1)).findById(999);
        verify(productDetailRepository, never()).save(any(ProductDetail.class));
    }
}
