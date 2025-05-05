package project.demo.service.implement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import project.demo.enums.CartStatus;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Cart;
import project.demo.model.CartItem;
import project.demo.model.Product;
import project.demo.repository.CartItemRepository;
import project.demo.repository.CartRepository;
import project.demo.repository.ProductRepository;

class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private CartItem testCartItem;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test cart
        testCart = new Cart();
        testCart.setCartId(1);
        testCart.setCustomerId(1);
        testCart.setStatus(CartStatus.ACTIVE.getValue());
        testCart.setTotalAmount(new BigDecimal("100.00"));

        // Setup test product
        testProduct = new Product();
        testProduct.setProductId(1);
        testProduct.setProductName("Test Product");
        testProduct.setPrice(new BigDecimal("50.00"));

        // Setup test cart item
        testCartItem = new CartItem();
        testCartItem.setCartItemId(1);
        testCartItem.setCartId(1);
        testCartItem.setProductId(1);
        testCartItem.setQuantity(2);
        testCartItem.setUnitPrice(new BigDecimal("50.00"));
        testCartItem.setIsSelected(true);
    }

    @Test
    void findById_ExistingCart_ReturnsCart() {
        // Arrange
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));

        // Act
        Cart result = cartService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testCart.getCartId(), result.getCartId());
        verify(cartRepository, times(1)).findById(1);
    }

    @Test
    void findById_NonExistingCart_ThrowsException() {
        // Arrange
        when(cartRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cartService.findById(999));
        verify(cartRepository, times(1)).findById(999);
    }

    @Test
    void findByCustomerId_ExistingCustomer_ReturnsCart() {
        // Arrange
        when(cartRepository.findByCustomerId(1)).thenReturn(Optional.of(testCart));

        // Act
        Optional<Cart> result = cartService.findByCustomerId(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCart.getCustomerId(), result.get().getCustomerId());
        verify(cartRepository, times(1)).findByCustomerId(1);
    }

    @Test
    void findByCustomerId_NonExistingCustomer_ReturnsEmptyOptional() {
        // Arrange
        when(cartRepository.findByCustomerId(999)).thenReturn(Optional.empty());

        // Act
        Optional<Cart> result = cartService.findByCustomerId(999);

        // Assert
        assertTrue(result.isEmpty());
        verify(cartRepository, times(1)).findByCustomerId(999);
    }

    @Test
    void getOrCreateCart_ExistingCart_ReturnsExistingCart() {
        // Arrange
        when(cartRepository.findByCustomerIdAndStatus(1, CartStatus.ACTIVE.getValue()))
                .thenReturn(Optional.of(testCart));

        // Act
        Cart result = cartService.getOrCreateCart(1);

        // Assert
        assertNotNull(result);
        assertEquals(testCart.getCartId(), result.getCartId());
        verify(cartRepository, times(1)).findByCustomerIdAndStatus(1, CartStatus.ACTIVE.getValue());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getOrCreateCart_NonExistingCart_CreatesNewCart() {
        // Arrange
        when(cartRepository.findByCustomerIdAndStatus(1, CartStatus.ACTIVE.getValue()))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.getOrCreateCart(1);

        // Assert
        assertNotNull(result);
        assertEquals(testCart.getCartId(), result.getCartId());
        verify(cartRepository, times(1)).findByCustomerIdAndStatus(1, CartStatus.ACTIVE.getValue());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addItem_NewItem_AddsItemToCart() {
        // Arrange
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(1, 1)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Mock the calculateTotal method
        doReturn(new BigDecimal("100.00")).when(cartRepository).save(any(Cart.class));

        // Act
        CartItem result = cartService.addItem(1, 1, 2);

        // Assert
        assertNotNull(result);
        assertEquals(testCartItem.getCartItemId(), result.getCartItemId());
        verify(cartRepository, times(1)).findById(1);
        verify(productRepository, times(1)).findById(1);
        verify(cartItemRepository, times(1)).findByCartIdAndProductId(1, 1);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addItem_ExistingItem_UpdatesQuantity() {
        // Arrange
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(1, 1)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Mock the calculateTotal method
        doReturn(new BigDecimal("100.00")).when(cartRepository).save(any(Cart.class));

        // Act
        CartItem result = cartService.addItem(1, 1, 2);

        // Assert
        assertNotNull(result);
        assertEquals(testCartItem.getCartItemId(), result.getCartItemId());
        verify(cartRepository, times(1)).findById(1);
        verify(productRepository, times(1)).findById(1);
        verify(cartItemRepository, times(1)).findByCartIdAndProductId(1, 1);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void updateItemQuantity_ExistingItem_UpdatesQuantity() {
        // Arrange
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Mock the calculateTotal method
        doReturn(new BigDecimal("100.00")).when(cartRepository).save(any(Cart.class));

        // Act
        CartItem result = cartService.updateItemQuantity(1, 3);

        // Assert
        assertNotNull(result);
        assertEquals(testCartItem.getCartItemId(), result.getCartItemId());
        verify(cartItemRepository, times(1)).findById(1);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void removeItem_ExistingItem_RemovesItem() {
        // Arrange
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).deleteById(1);

        // Mock the calculateTotal method
        doReturn(new BigDecimal("0.00")).when(cartRepository).save(any(Cart.class));

        // Act
        cartService.removeItem(1);

        // Assert
        verify(cartItemRepository, times(1)).findById(1);
        verify(cartItemRepository, times(1)).deleteById(1);
    }

    @Test
    void getCartItems_ExistingCart_ReturnsItems() {
        // Arrange
        when(cartRepository.existsById(1)).thenReturn(true);
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(testCartItem);
        when(cartItemRepository.findByCartId(1)).thenReturn(cartItems);

        // Act
        List<CartItem> result = cartService.getCartItems(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCartItem.getCartItemId(), result.get(0).getCartItemId());
        verify(cartRepository, times(1)).existsById(1);
        verify(cartItemRepository, times(1)).findByCartId(1);
    }

    @Test
    void calculateTotal_ExistingCart_CalculatesTotal() {
        // Arrange
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(testCartItem);
        when(cartItemRepository.findByCartIdAndIsSelected(1, true)).thenReturn(cartItems);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        BigDecimal result = cartService.calculateTotal(1);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), testCart.getTotalAmount());
        verify(cartRepository, times(1)).findById(1);
        verify(cartItemRepository, times(1)).findByCartIdAndIsSelected(1, true);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void clearCart_ExistingCart_ClearsItems() {
        // Arrange
        when(cartRepository.existsById(1)).thenReturn(true);
        doNothing().when(cartItemRepository).deleteByCartId(1);
        when(cartRepository.findById(1)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        cartService.clearCart(1);

        // Assert
        verify(cartRepository, times(1)).existsById(1);
        verify(cartItemRepository, times(1)).deleteByCartId(1);
        verify(cartRepository, times(1)).findById(1);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void updateItemSelection_ExistingItem_UpdatesSelection() {
        // Arrange
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Mock the calculateTotal method
        doReturn(new BigDecimal("100.00")).when(cartRepository).save(any(Cart.class));

        // Act
        CartItem result = cartService.updateItemSelection(1, false);

        // Assert
        assertNotNull(result);
        assertEquals(testCartItem.getCartItemId(), result.getCartItemId());
        verify(cartItemRepository, times(1)).findById(1);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }
}
