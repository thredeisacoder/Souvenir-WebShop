package project.demo.service.implement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import project.demo.enums.DiscountType;
import project.demo.enums.PromotionStatus;
import project.demo.exception.PromotionException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Order;
import project.demo.model.OrderPromotion;
import project.demo.model.ProductPromotion;
import project.demo.model.Promotion;
import project.demo.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PromotionServiceImplTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductPromotionRepository productPromotionRepository;

    @Mock
    private OrderPromotionRepository orderPromotionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    private Promotion testPromotion;
    private ProductPromotion testProductPromotion;
    private OrderPromotion testOrderPromotion;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test promotion
        testPromotion = new Promotion();
        testPromotion.setPromotionId(1);
        testPromotion.setPromotionName("Test Promotion");
        testPromotion.setDescription("Test Description");
        testPromotion.setStartDate(LocalDate.now());
        testPromotion.setEndDate(LocalDate.now().plusDays(30));
        testPromotion.setDiscountType(DiscountType.PERCENTAGE.getValue());
        testPromotion.setDiscountValue(new BigDecimal("10.00"));
        testPromotion.setStatus(PromotionStatus.ACTIVE.getValue());
        testPromotion.setUsageLimit(100);

        // Setup test product promotion
        testProductPromotion = new ProductPromotion();
        testProductPromotion.setProductPromotionId(1);
        testProductPromotion.setProductId(1);
        testProductPromotion.setPromotionId(1);
        testProductPromotion.setStartDate(LocalDate.now());
        testProductPromotion.setEndDate(LocalDate.now().plusDays(30));
        testProductPromotion.setStatus(PromotionStatus.ACTIVE.getValue());

        // Setup test order
        testOrder = new Order();
        testOrder.setOrderId(1);
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        testOrder.setShippingFee(new BigDecimal("10.00"));

        // Setup test order promotion
        testOrderPromotion = new OrderPromotion();
        testOrderPromotion.setOrderPromotionId(1);
        testOrderPromotion.setOrderId(1);
        testOrderPromotion.setPromotionId(1);
        testOrderPromotion.setDiscountAmount(new BigDecimal("10.00"));
    }

    @Test
    void findById_ExistingPromotion_ReturnsPromotion() {
        // Arrange
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));

        // Act
        Promotion result = promotionService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testPromotion.getPromotionId(), result.getPromotionId());
        verify(promotionRepository, times(1)).findById(1);
    }

    @Test
    void findById_NonExistingPromotion_ThrowsException() {
        // Arrange
        when(promotionRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> promotionService.findById(999));
        verify(promotionRepository, times(1)).findById(999);
    }

    @Test
    void findAllActive_ReturnsActivePromotions() {
        // Arrange
        List<Promotion> promotions = new ArrayList<>();
        promotions.add(testPromotion);
        when(promotionRepository.findActivePromotions(eq(PromotionStatus.ACTIVE.getValue()), any(LocalDate.class)))
                .thenReturn(promotions);

        // Act
        List<Promotion> result = promotionService.findAllActive();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPromotion.getPromotionId(), result.get(0).getPromotionId());
        verify(promotionRepository, times(1)).findActivePromotions(eq(PromotionStatus.ACTIVE.getValue()), any(LocalDate.class));
    }

    @Test
    void save_ValidPromotion_SavesPromotion() {
        // Arrange
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);

        // Act
        Promotion result = promotionService.save(testPromotion);

        // Assert
        assertNotNull(result);
        assertEquals(testPromotion.getPromotionId(), result.getPromotionId());
        verify(promotionRepository, times(1)).save(testPromotion);
    }

    @Test
    void save_NullPromotionName_ThrowsException() {
        // Arrange
        testPromotion.setPromotionName(null);

        // Act & Assert
        assertThrows(PromotionException.class, () -> promotionService.save(testPromotion));
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void save_InvalidDateRange_ThrowsException() {
        // Arrange
        testPromotion.setEndDate(testPromotion.getStartDate().minusDays(1));

        // Act & Assert
        assertThrows(PromotionException.class, () -> promotionService.save(testPromotion));
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void save_InvalidDiscountType_ThrowsException() {
        // Arrange
        testPromotion.setDiscountType("invalid_type");

        // Act & Assert
        assertThrows(PromotionException.class, () -> promotionService.save(testPromotion));
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void save_InvalidPercentage_ThrowsException() {
        // Arrange
        testPromotion.setDiscountType(DiscountType.PERCENTAGE.getValue());
        testPromotion.setDiscountValue(new BigDecimal("101.00"));

        // Act & Assert
        assertThrows(PromotionException.class, () -> promotionService.save(testPromotion));
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void update_ValidPromotion_UpdatesPromotion() {
        // Arrange
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);

        // Act
        Promotion result = promotionService.update(testPromotion);

        // Assert
        assertNotNull(result);
        assertEquals(testPromotion.getPromotionId(), result.getPromotionId());
        verify(promotionRepository, times(1)).findById(1);
        verify(promotionRepository, times(1)).save(testPromotion);
    }

    @Test
    void update_NonExistingPromotion_ThrowsException() {
        // Arrange
        when(promotionRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> promotionService.update(testPromotion));
        verify(promotionRepository, times(1)).findById(1);
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void delete_PromotionWithNoRelations_DeletesPromotion() {
        // Arrange
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));
        when(productPromotionRepository.findByPromotionId(1)).thenReturn(new ArrayList<>());
        when(orderPromotionRepository.findByPromotionId(1)).thenReturn(new ArrayList<>());
        doNothing().when(promotionRepository).deleteById(1);

        // Act
        promotionService.delete(1);

        // Assert
        verify(promotionRepository, times(1)).findById(1);
        verify(productPromotionRepository, times(1)).findByPromotionId(1);
        verify(orderPromotionRepository, times(1)).findByPromotionId(1);
        verify(promotionRepository, times(1)).deleteById(1);
    }

    @Test
    void delete_PromotionWithProductPromotions_ThrowsException() {
        // Arrange
        List<ProductPromotion> productPromotions = new ArrayList<>();
        productPromotions.add(testProductPromotion);
        
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));
        when(productPromotionRepository.findByPromotionId(1)).thenReturn(productPromotions);

        // Act & Assert
        assertThrows(PromotionException.class, () -> promotionService.delete(1));
        verify(promotionRepository, times(1)).findById(1);
        verify(productPromotionRepository, times(1)).findByPromotionId(1);
        verify(promotionRepository, never()).deleteById(anyInt());
    }

    @Test
    void findProductPromotionsByProductId_ExistingProduct_ReturnsPromotions() {
        // Arrange
        List<ProductPromotion> productPromotions = new ArrayList<>();
        productPromotions.add(testProductPromotion);
        
        when(productRepository.existsById(1)).thenReturn(true);
        when(productPromotionRepository.findByProductId(1)).thenReturn(productPromotions);

        // Act
        List<ProductPromotion> result = promotionService.findProductPromotionsByProductId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductPromotion.getProductPromotionId(), result.get(0).getProductPromotionId());
        verify(productRepository, times(1)).existsById(1);
        verify(productPromotionRepository, times(1)).findByProductId(1);
    }

    @Test
    void findProductPromotionsByProductId_NonExistingProduct_ThrowsException() {
        // Arrange
        when(productRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> promotionService.findProductPromotionsByProductId(999));
        verify(productRepository, times(1)).existsById(999);
        verify(productPromotionRepository, never()).findByProductId(anyInt());
    }

    @Test
    void applyPromotionToProduct_ValidData_AppliesPromotion() {
        // Arrange
        when(productRepository.existsById(1)).thenReturn(true);
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));
        when(productPromotionRepository.findByProductIdAndPromotionId(1, 1)).thenReturn(new ArrayList<>());
        when(productPromotionRepository.save(any(ProductPromotion.class))).thenReturn(testProductPromotion);

        // Act
        ProductPromotion result = promotionService.applyPromotionToProduct(1, 1);

        // Assert
        assertNotNull(result);
        assertEquals(testProductPromotion.getProductPromotionId(), result.getProductPromotionId());
        verify(productRepository, times(1)).existsById(1);
        verify(promotionRepository, times(1)).findById(1);
        verify(productPromotionRepository, times(1)).findByProductIdAndPromotionId(1, 1);
        verify(productPromotionRepository, times(1)).save(any(ProductPromotion.class));
    }

    @Test
    void applyPromotionToProduct_DuplicateApplication_ThrowsException() {
        // Arrange
        List<ProductPromotion> productPromotions = new ArrayList<>();
        productPromotions.add(testProductPromotion);
        
        when(productRepository.existsById(1)).thenReturn(true);
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));
        when(productPromotionRepository.findByProductIdAndPromotionId(1, 1)).thenReturn(productPromotions);

        // Act & Assert
        assertThrows(PromotionException.class, () -> promotionService.applyPromotionToProduct(1, 1));
        verify(productRepository, times(1)).existsById(1);
        verify(promotionRepository, times(1)).findById(1);
        verify(productPromotionRepository, times(1)).findByProductIdAndPromotionId(1, 1);
        verify(productPromotionRepository, never()).save(any(ProductPromotion.class));
    }

    @Test
    void removePromotionFromProduct_ExistingPromotion_RemovesPromotion() {
        // Arrange
        when(productPromotionRepository.existsById(1)).thenReturn(true);
        doNothing().when(productPromotionRepository).deleteById(1);

        // Act
        promotionService.removePromotionFromProduct(1);

        // Assert
        verify(productPromotionRepository, times(1)).existsById(1);
        verify(productPromotionRepository, times(1)).deleteById(1);
    }

    @Test
    void removePromotionFromProduct_NonExistingPromotion_ThrowsException() {
        // Arrange
        when(productPromotionRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> promotionService.removePromotionFromProduct(999));
        verify(productPromotionRepository, times(1)).existsById(999);
        verify(productPromotionRepository, never()).deleteById(anyInt());
    }

    @Test
    void applyPromotionToOrder_ValidData_AppliesPromotion() {
        // Arrange
        when(orderRepository.existsById(1)).thenReturn(true);
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));
        when(orderPromotionRepository.findByOrderIdAndPromotionId(1, 1)).thenReturn(new ArrayList<>());
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderPromotionRepository.save(any(OrderPromotion.class))).thenReturn(testOrderPromotion);

        // Act
        OrderPromotion result = promotionService.applyPromotionToOrder(1, 1);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderPromotion.getOrderPromotionId(), result.getOrderPromotionId());
        verify(orderRepository, times(1)).existsById(1);
        verify(promotionRepository, times(1)).findById(1);
        verify(orderPromotionRepository, times(1)).findByOrderIdAndPromotionId(1, 1);
        verify(orderRepository, times(1)).findById(1);
        verify(orderPromotionRepository, times(1)).save(any(OrderPromotion.class));
    }

    @Test
    void applyPromotionToOrder_DuplicateApplication_ThrowsException() {
        // Arrange
        List<OrderPromotion> orderPromotions = new ArrayList<>();
        orderPromotions.add(testOrderPromotion);
        
        when(orderRepository.existsById(1)).thenReturn(true);
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));
        when(orderPromotionRepository.findByOrderIdAndPromotionId(1, 1)).thenReturn(orderPromotions);

        // Act & Assert
        assertThrows(PromotionException.class, () -> promotionService.applyPromotionToOrder(1, 1));
        verify(orderRepository, times(1)).existsById(1);
        verify(promotionRepository, times(1)).findById(1);
        verify(orderPromotionRepository, times(1)).findByOrderIdAndPromotionId(1, 1);
        verify(orderPromotionRepository, never()).save(any(OrderPromotion.class));
    }

    @Test
    void applyPromotionToOrder_UsageLimitReached_ThrowsException() {
        // Arrange
        testPromotion.setUsageLimit(10);
        
        when(orderRepository.existsById(1)).thenReturn(true);
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));
        when(orderPromotionRepository.findByOrderIdAndPromotionId(1, 1)).thenReturn(new ArrayList<>());
        when(orderPromotionRepository.countByPromotionId(1)).thenReturn(10L);

        // Act & Assert
        assertThrows(PromotionException.class, () -> promotionService.applyPromotionToOrder(1, 1));
        verify(orderRepository, times(1)).existsById(1);
        verify(promotionRepository, times(1)).findById(1);
        verify(orderPromotionRepository, times(1)).findByOrderIdAndPromotionId(1, 1);
        verify(orderPromotionRepository, times(1)).countByPromotionId(1);
        verify(orderPromotionRepository, never()).save(any(OrderPromotion.class));
    }

    @Test
    void calculateOrderDiscount_PercentageDiscount_CalculatesCorrectly() {
        // Arrange
        testPromotion.setDiscountType(DiscountType.PERCENTAGE.getValue());
        testPromotion.setDiscountValue(new BigDecimal("10.00"));
        
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));

        // Act
        BigDecimal result = promotionService.calculateOrderDiscount(1, 1);

        // Assert
        assertEquals(new BigDecimal("10.00"), result);
        verify(orderRepository, times(1)).findById(1);
        verify(promotionRepository, times(1)).findById(1);
    }

    @Test
    void calculateOrderDiscount_FixedAmountDiscount_CalculatesCorrectly() {
        // Arrange
        testPromotion.setDiscountType(DiscountType.FIXED_AMOUNT.getValue());
        testPromotion.setDiscountValue(new BigDecimal("15.00"));
        
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));

        // Act
        BigDecimal result = promotionService.calculateOrderDiscount(1, 1);

        // Assert
        assertEquals(new BigDecimal("15.00"), result);
        verify(orderRepository, times(1)).findById(1);
        verify(promotionRepository, times(1)).findById(1);
    }

    @Test
    void calculateOrderDiscount_FreeShippingDiscount_CalculatesCorrectly() {
        // Arrange
        testPromotion.setDiscountType(DiscountType.FREE_SHIPPING.getValue());
        
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));

        // Act
        BigDecimal result = promotionService.calculateOrderDiscount(1, 1);

        // Assert
        assertEquals(new BigDecimal("10.00"), result);
        verify(orderRepository, times(1)).findById(1);
        verify(promotionRepository, times(1)).findById(1);
    }

    @Test
    void findOrderPromotionsByOrderId_ExistingOrder_ReturnsPromotions() {
        // Arrange
        List<OrderPromotion> orderPromotions = new ArrayList<>();
        orderPromotions.add(testOrderPromotion);
        
        when(orderRepository.existsById(1)).thenReturn(true);
        when(orderPromotionRepository.findByOrderId(1)).thenReturn(orderPromotions);

        // Act
        List<OrderPromotion> result = promotionService.findOrderPromotionsByOrderId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrderPromotion.getOrderPromotionId(), result.get(0).getOrderPromotionId());
        verify(orderRepository, times(1)).existsById(1);
        verify(orderPromotionRepository, times(1)).findByOrderId(1);
    }

    @Test
    void findOrderPromotionsByOrderId_NonExistingOrder_ThrowsException() {
        // Arrange
        when(orderRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> promotionService.findOrderPromotionsByOrderId(999));
        verify(orderRepository, times(1)).existsById(999);
        verify(orderPromotionRepository, never()).findByOrderId(anyInt());
    }

    @Test
    void activatePromotion_ExistingPromotion_ActivatesPromotion() {
        // Arrange
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);

        // Act
        promotionService.activatePromotion(1);

        // Assert
        assertEquals(PromotionStatus.ACTIVE.getValue(), testPromotion.getStatus());
        verify(promotionRepository, times(1)).findById(1);
        verify(promotionRepository, times(1)).save(testPromotion);
    }

    @Test
    void deactivatePromotion_ExistingPromotion_DeactivatesPromotion() {
        // Arrange
        when(promotionRepository.findById(1)).thenReturn(Optional.of(testPromotion));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);

        // Act
        promotionService.deactivatePromotion(1);

        // Assert
        assertEquals(PromotionStatus.INACTIVE.getValue(), testPromotion.getStatus());
        verify(promotionRepository, times(1)).findById(1);
        verify(promotionRepository, times(1)).save(testPromotion);
    }
}
