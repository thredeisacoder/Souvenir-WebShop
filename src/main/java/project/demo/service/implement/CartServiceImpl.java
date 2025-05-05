package project.demo.service.implement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.demo.enums.CartStatus;
import project.demo.exception.CartException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Cart;
import project.demo.model.CartItem;
import project.demo.model.Product;
import project.demo.repository.CartItemRepository;
import project.demo.repository.CartRepository;
import project.demo.repository.ProductRepository;
import project.demo.repository.PromotionRepository;
import project.demo.service.ICartService;
import project.demo.service.IProductDetailService;

/**
 * Implementation of the ICartService interface for managing Cart entities
 */
@Service
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final IProductDetailService productDetailService;
    // private final IRecommendationService recommendationService;

    // public CartServiceImpl(CartRepository cartRepository,
    // CartItemRepository cartItemRepository,
    // ProductRepository productRepository,
    // PromotionRepository promotionRepository,
    // IRecommendationService recommendationService) {
    // this.cartRepository = cartRepository;
    // this.cartItemRepository = cartItemRepository;
    // this.productRepository = productRepository;
    // this.promotionRepository = promotionRepository;
    // this.recommendationService = recommendationService;
    // }

    public CartServiceImpl(CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            PromotionRepository promotionRepository,
            IProductDetailService productDetailService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.promotionRepository = promotionRepository;
        this.productDetailService = productDetailService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cart findById(Integer cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("CART_NOT_FOUND", "Cart not found with ID: " + cartId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Cart> findByCustomerId(Integer customerId) {
        return cartRepository.findByCustomerId(customerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Cart getOrCreateCart(Integer customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }

        try {
            // Try to find an existing active cart for this customer
            Optional<Cart> existingCart = cartRepository.findByCustomerIdAndStatus(customerId,
                    CartStatus.ACTIVE.getValue());

            if (existingCart.isPresent()) {
                Cart cart = existingCart.get();
                try {
                    List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());
                    if (items != null && !items.isEmpty()) {
                        try {
                            // Recalculate total to ensure it's accurate
                            calculateTotal(cart.getCartId());
                        } catch (Exception e) {
                            // Log error but continue with existing cart
                            System.err.println("Error calculating cart total: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching cart items: " + e.getMessage());
                }
                return cart;
            }

            // If no active cart exists, check if there's an abandoned cart
            Optional<Cart> abandonedCart = cartRepository.findByCustomerIdAndStatus(customerId,
                    CartStatus.ABANDONED.getValue());
            
            if (abandonedCart.isPresent()) {
                Cart cart = abandonedCart.get();
                // Reactivate the abandoned cart
                cart.setStatus(CartStatus.ACTIVE.getValue());
                return cartRepository.save(cart);
            }
            
            // Tìm tất cả giỏ hàng của khách hàng để xử lý
            List<Cart> existingCarts = cartRepository.findAll().stream()
                    .filter(c -> customerId.equals(c.getCustomerId()))
                    .toList();
            
            // Nếu có giỏ hàng tồn tại, thử xóa chúng
            if (!existingCarts.isEmpty()) {
                for (Cart oldCart : existingCarts) {
                    try {
                        // Xóa tất cả các mục trong giỏ hàng
                        cartItemRepository.deleteByCartId(oldCart.getCartId());
                        
                        // Sau đó, thử cập nhật trạng thái trước khi xóa
                        try {
                            oldCart.setStatus(CartStatus.CONVERTED.getValue()); // Sử dụng CONVERTED thay vì DELETED
                            cartRepository.save(oldCart);
                        } catch (Exception e) {
                            System.err.println("Error updating cart status: " + e.getMessage());
                        }
                        
                        // Thử xóa giỏ hàng
                        try {
                            cartRepository.delete(oldCart);
                        } catch (Exception e) {
                            System.err.println("Could not delete cart: " + e.getMessage());
                            // Nếu không thể xóa, đã cập nhật trạng thái ở trên
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing old cart: " + e.getMessage());
                    }
                }
                
                try {
                    // Đảm bảo thay đổi được commit
                    cartRepository.flush();
                } catch (Exception e) {
                    System.err.println("Error flushing changes: " + e.getMessage());
                }
                
                // Kiểm tra xem còn giỏ hàng nào không được xóa không
                try {
                    List<Cart> remainingCarts = cartRepository.findAll().stream()
                            .filter(c -> customerId.equals(c.getCustomerId()))
                            .toList();
                            
                    if (!remainingCarts.isEmpty()) {
                        // Nếu vẫn còn giỏ hàng, chuyển đổi giỏ hàng cũ thành active
                        Cart oldCart = remainingCarts.get(0);
                        oldCart.setStatus(CartStatus.ACTIVE.getValue());
                        oldCart.setTotalAmount(BigDecimal.ZERO);
                        
                        // Xóa tất cả các mục trong giỏ hàng cũ (nếu còn)
                        cartItemRepository.deleteByCartId(oldCart.getCartId());
                        
                        return cartRepository.save(oldCart);
                    }
                } catch (Exception e) {
                    System.err.println("Error checking remaining carts: " + e.getMessage());
                }
            }

            // Tạo giỏ hàng mới
            Cart newCart = new Cart();
            newCart.setCustomerId(customerId);
            newCart.setStatus(CartStatus.ACTIVE.getValue());
            newCart.setTotalAmount(BigDecimal.ZERO);
            newCart.setSessionId(generateSessionId());
            
            try {
                return cartRepository.save(newCart);
            } catch (Exception e) {
                System.err.println("Error creating new cart: " + e.getMessage());
                e.printStackTrace();
                
                // Nếu không thể tạo giỏ hàng mới, thử một cách cuối cùng - kiểm tra lại giỏ hàng converted
                try {
                    Optional<Cart> convertedCart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.CONVERTED.getValue());
                    if (convertedCart.isPresent()) {
                        Cart cart = convertedCart.get();
                        cart.setStatus(CartStatus.ACTIVE.getValue());
                        cart.setTotalAmount(BigDecimal.ZERO);
                        
                        // Xóa tất cả các mục trong giỏ hàng
                        cartItemRepository.deleteByCartId(cart.getCartId());
                        
                        return cartRepository.save(cart);
                    }
                } catch (Exception ex) {
                    System.err.println("Error finding converted cart: " + ex.getMessage());
                }
                
                // Nếu tất cả các cách đều thất bại, ném ngoại lệ để thông báo lỗi rõ ràng
                throw new RuntimeException("Không thể tạo hoặc lấy giỏ hàng. Vui lòng thử lại sau.", e);
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in getOrCreateCart: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo hoặc lấy giỏ hàng. Vui lòng thử lại sau.", e);
        }
    }

    /**
     * Generate a unique session ID for the cart
     * @return a unique session ID string
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + Math.random();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CartItem addItem(Integer cartId, Integer productId, Integer quantity) {
        // check if quantity is valid
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // check if cart is active
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("CART_NOT_FOUND", "Cart not found with ID: " + cartId));
        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // Check if the quantity exceeds available stock
        Integer stockQuantity = productDetailService.getQuantityInStock(productId);
        if (quantity > stockQuantity) {
            throw new CartException("INSUFFICIENT_STOCK", 
                "Requested quantity (" + quantity + ") exceeds available stock (" + stockQuantity + ")");
        }

        // check if product exists
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found with ID: " + productId));

        // check if cart item exists
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndProductId(cartId, productId);
        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            
            // Check if the combined quantity exceeds available stock
            int newQuantity = cartItem.getQuantity() + quantity;
            if (newQuantity > stockQuantity) {
                throw new CartException("INSUFFICIENT_STOCK", 
                    "Combined quantity (" + newQuantity + ") exceeds available stock (" + stockQuantity + ")");
            }
            
            cartItem.setQuantity(newQuantity);
        } else {
            // Create new cart item with all required fields
            cartItem = new CartItem();
            cartItem.setCartId(cartId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
            
            // Sử dụng giá hiệu lực (có tính đến giảm giá) thay vì luôn dùng giá gốc
            BigDecimal effectivePrice = productDetailService.getEffectivePrice(productId);
            cartItem.setUnitPrice(effectivePrice);
            
            cartItem.setIsSelected(true); // Default to selected
            // Optionally set the entity references
            cartItem.setCart(cart);
            cartItem.setProduct(product);
        }
        cartItem = cartItemRepository.save(cartItem);

        // update cart total
        calculateTotal(cartId);

        return cartItem;
    }

    /**
     * Update the quantity of a cart item based on cart ID and product ID
     *
     * @param cartId    the ID of the cart
     * @param productId the ID of the product
     * @param quantity  the quantity to update
     * @param action    the action to perform (set, increase, decrease)
     * @return the updated cart item
     */
    @Transactional
    public CartItem updateItemQuantity(Integer cartId, Integer productId, Integer quantity, String action) {
        // check if quantity is valid
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // check if cart is active
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("CART_NOT_FOUND", "Cart not found with ID: " + cartId));
        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // check if cart item exists
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND",
                        "Cart item not found with cart ID: " + cartId + " and product ID: " + productId));

        int newQuantity = cartItem.getQuantity();

        // Calculate the new quantity based on the action
        if ("set".equals(action)) {
            newQuantity = quantity;
        } else if ("increase".equals(action)) {
            newQuantity += quantity;
        } else if ("decrease".equals(action)) {
            newQuantity -= quantity;
            if (newQuantity <= 0) {
                return removeItem(cartId, productId);
            }
        }

        // Check if the new quantity exceeds available stock
        Integer stockQuantity = productDetailService.getQuantityInStock(productId);
        if (newQuantity > stockQuantity) {
            throw new CartException("INSUFFICIENT_STOCK", 
                "Requested quantity (" + newQuantity + ") exceeds available stock (" + stockQuantity + ")");
        }

        // update cart item
        cartItem.setQuantity(newQuantity);
        cartItem = cartItemRepository.save(cartItem);

        // update cart total
        calculateTotal(cartId);

        return cartItem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void removeItem(Integer cartItemId) {
        // Check if cart item exists
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND",
                        "Cart item not found with ID: " + cartItemId));

        // Check if cart is active
        Cart cart = cartRepository.findById(cartItem.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND",
                        "Cart not found with ID: " + cartItem.getCartId()));

        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        Integer cartId = cartItem.getCartId();

        // Delete cart item
        cartItemRepository.deleteById(cartItemId);

        // Update cart total
        calculateTotal(cartId);
    }

    /**
     * Remove an item from a cart by product ID
     *
     * @param cartId    the ID of the cart
     * @param productId the ID of the product to remove
     * @return the removed cart item (before removal)
     */
    public CartItem removeItem(Integer cartId, Integer productId) {
        // Check if cart is active
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("CART_NOT_FOUND", "Cart not found with ID: " + cartId));
        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // Check if cart item exists
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND",
                        "Cart item not found with cart ID: " + cartId + " and product ID: " + productId));

        // Delete cart item
        cartItemRepository.deleteById(cartItem.getCartItemId());

        // Update cart total
        calculateTotal(cartId);

        return cartItem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CartItem> getCartItems(Integer cartId) {
        // Check if cart exists
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("CART_NOT_FOUND", "Cart not found with ID: " + cartId));

        // Check if cart is active or abandoned
        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus()) &&
                !CartStatus.ABANDONED.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        return cartItemRepository.findByCartId(cartId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BigDecimal calculateTotal(Integer cartId) {
        // Check if cart exists
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("CART_NOT_FOUND", "Cart not found with ID: " + cartId));

        // Check if cart is active
        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // Get all selected items in cart
        List<CartItem> selectedItems = cartItemRepository.findByCartIdAndIsSelected(cartId, true);

        // Calculate total
        BigDecimal total = BigDecimal.ZERO;

        // If there are selected items, calculate the total
        if (!selectedItems.isEmpty()) {
            for (CartItem item : selectedItems) {
                BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
                total = total.add(itemTotal);
            }
        }

        // Update cart total
        cart.setTotalAmount(total);
        cartRepository.save(cart);

        return total;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void clearCart(Integer cartId) {
        // Check if cart exists
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("CART_NOT_FOUND", "Cart not found with ID: " + cartId));

        // Check if cart is active
        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // Check if cart has items
        if (cartItemRepository.countByCartId(cartId) == 0) {
            throw CartException.emptyCart();
        }

        // Delete all items in cart
        cartItemRepository.deleteByCartId(cartId);

        // Update cart total
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CartItem updateItemSelection(Integer cartItemId, Boolean isSelected) {
        // Check if cart item exists
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND",
                        "Cart item not found with ID: " + cartItemId));

        // Check if cart is active
        Cart cart = cartRepository.findById(cartItem.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND",
                        "Cart not found with ID: " + cartItem.getCartId()));

        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // Update selection status
        cartItem.setIsSelected(isSelected);

        // Save cart item
        CartItem updatedCartItem = cartItemRepository.save(cartItem);

        // Update cart total
        calculateTotal(updatedCartItem.getCartId());

        return updatedCartItem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CartItem> getActiveCartItems(Integer cartId) {
        // Check if cart exists
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND",
                        "Cart not found with ID: " + cartId));

        // Check if cart is active
        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // Get all items in the cart (ignoring saved_for_later since it doesn't exist in
        // DB)
        return cartItemRepository.findByCartId(cartId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CartItem> getSavedForLaterItems(Integer cartId) {
        // Check if cart exists
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND",
                        "Cart not found with ID: " + cartId));

        // Check if cart is active
        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // Return empty list since we don't support saved for later items yet
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CartItem saveForLater(Integer cartItemId) {
        // Check if cart item exists
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND",
                        "Cart item not found with ID: " + cartItemId));

        // Check if cart is active
        Cart cart = cartRepository.findById(cartItem.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND",
                        "Cart not found with ID: " + cartItem.getCartId()));

        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // Just unselect the item to simulate "save for later"
        cartItem.setIsSelected(false);

        // Save cart item
        CartItem updatedCartItem = cartItemRepository.save(cartItem);

        // Update cart total
        calculateTotal(updatedCartItem.getCartId());

        return updatedCartItem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CartItem moveToCart(Integer cartItemId) {
        // Check if cart item exists
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND",
                        "Cart item not found with ID: " + cartItemId));

        // Check if cart is active
        Cart cart = cartRepository.findById(cartItem.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND",
                        "Cart not found with ID: " + cartItem.getCartId()));

        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // Select the item to move it back to active cart
        cartItem.setIsSelected(true);

        // Save cart item
        CartItem updatedCartItem = cartItemRepository.save(cartItem);

        // Update cart total
        calculateTotal(updatedCartItem.getCartId());

        return updatedCartItem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Cart applyPromoCode(Integer cartId, String promoCode) {
        // Check if cart exists
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND",
                        "Cart not found with ID: " + cartId));

        // Check if cart is active
        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // In a real implementation, you would:
        // 1. Find the promotion by code
        // 2. Validate the promotion (active, not expired, etc.)
        // 3. Apply the promotion to the cart

        // For now, we'll just return the cart
        // This is a simplified implementation

        // Recalculate cart total
        calculateTotal(cartId);

        return cart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> getRecommendations(Integer cartId, int limit) {
        // Check if cart exists
        cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND",
                        "Cart not found with ID: " + cartId));

        // Get active items in cart
        List<CartItem> cartItems = getActiveCartItems(cartId);

        if (cartItems.isEmpty()) {
            // If cart is empty, return some popular products
            // In a real implementation, you would have a method to get popular products
            return productRepository.findAll().stream().limit(limit).toList();
        }

        // Extract product IDs from cart items
        List<Integer> productIds = new ArrayList<>();
        for (CartItem item : cartItems) {
            productIds.add(item.getProductId());
        }

        // In a real implementation, you would have a recommendation service
        // For now, just return products from the same categories
        List<Product> recommendations = new ArrayList<>();
        for (Integer productId : productIds) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null && product.getCatalogId() != null) {
                List<Product> similarProducts = productRepository.findByCatalogId(product.getCatalogId());
                recommendations.addAll(similarProducts.stream()
                        .filter(p -> !productIds.contains(p.getProductId()))
                        .limit(limit - recommendations.size())
                        .toList());

                if (recommendations.size() >= limit) {
                    break;
                }
            }
        }

        return recommendations.stream().limit(limit).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount(Integer cartId) {
        // Check if cart exists
        cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND",
                        "Cart not found with ID: " + cartId));

        // Count all items in the cart (ignoring saved_for_later since it doesn't exist
        // in DB)
        return (int) cartItemRepository.countByCartId(cartId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CartItem getCartItem(Integer cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND",
                        "Cart item not found with ID: " + cartItemId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CartItem> getItems(Integer cartId) {
        return getCartItems(cartId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getCartTotal(Integer cartId) {
        return calculateTotal(cartId);
    }

    /**
     * Update quantity of a cart item by cart ID and product ID
     *
     * @param cartId    the ID of the cart
     * @param productId the ID of the product
     * @param quantity  the new quantity
     * @return the updated cart item
     */
    public CartItem updateQuantity(Integer cartId, Integer productId, Integer quantity) {
        // Check if quantity is valid
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // Check if cart exists and is active
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND", "Cart not found with ID: " + cartId));
        
        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        // Check if the quantity exceeds available stock
        Integer stockQuantity = productDetailService.getQuantityInStock(productId);
        if (quantity > stockQuantity) {
            throw new CartException("INSUFFICIENT_STOCK", 
                "Requested quantity (" + quantity + ") exceeds available stock (" + stockQuantity + ")");
        }

        // Get cart item
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND",
                        "Cart item not found with cartId: " + cartId + " and productId: " + productId));

        // Update quantity
        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);

        // Update cart total
        calculateTotal(cartId);

        return cartItem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CartItem updateItemQuantity(Integer cartItemId, Integer quantity) {
        // Check if quantity is valid
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // Get the cart item
        final CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND",
                        "Cart item not found with ID: " + cartItemId));
        
        // Check if cart is active
        Cart cart = cartRepository.findById(cartItem.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND",
                        "Cart not found with ID: " + cartItem.getCartId()));
        
        if (!CartStatus.ACTIVE.getValue().equals(cart.getStatus())) {
            throw CartException.invalidStatus(cart.getStatus());
        }

        Integer productId = cartItem.getProductId();
        
        // Check if the quantity exceeds available stock
        Integer stockQuantity = productDetailService.getQuantityInStock(productId);
        if (quantity > stockQuantity) {
            throw new CartException("INSUFFICIENT_STOCK", 
                "Requested quantity (" + quantity + ") exceeds available stock (" + stockQuantity + ")");
        }

        // Update quantity
        cartItem.setQuantity(quantity);
        CartItem updatedCartItem = cartItemRepository.save(cartItem);

        // Update cart total
        calculateTotal(cartItem.getCartId());

        return updatedCartItem;
    }
}
