package project.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import project.demo.model.Cart;
import project.demo.model.CartItem;
import project.demo.model.Product;

/**
 * Service interface for managing Cart entities
 */
public interface ICartService {

    /**
     * Find a cart by its ID
     *
     * @param cartId the ID of the cart to find
     * @return the cart if found
     */
    Cart findById(Integer cartId);

    /**
     * Find a cart by customer ID
     *
     * @param customerId the ID of the customer
     * @return the cart if found
     */
    Optional<Cart> findByCustomerId(Integer customerId);

    /**
     * Get or create a cart for a customer
     *
     * @param customerId the ID of the customer
     * @return the existing or newly created cart
     */
    Cart getOrCreateCart(Integer customerId);

    /**
     * Add an item to a cart
     *
     * @param cartId    the ID of the cart
     * @param productId the ID of the product to add
     * @param quantity  the quantity to add
     * @return the added cart item
     */
    CartItem addItem(Integer cartId, Integer productId, Integer quantity);

    /**
     * Update the quantity of a cart item
     *
     * @param cartItemId the ID of the cart item
     * @param quantity   the new quantity
     * @return the updated cart item
     */
    CartItem updateItemQuantity(Integer cartItemId, Integer quantity);

    /**
     * Remove an item from a cart
     *
     * @param cartItemId the ID of the cart item to remove
     */
    void removeItem(Integer cartItemId);

    /**
     * Get all items in a cart
     *
     * @param cartId the ID of the cart
     * @return a list of cart items
     */
    List<CartItem> getCartItems(Integer cartId);

    /**
     * Get active items in a cart (not saved for later)
     *
     * @param cartId the ID of the cart
     * @return a list of active cart items
     */
    List<CartItem> getActiveCartItems(Integer cartId);

    /**
     * Get saved for later items in a cart
     *
     * @param cartId the ID of the cart
     * @return a list of saved for later cart items
     */
    List<CartItem> getSavedForLaterItems(Integer cartId);

    /**
     * Calculate the total amount of a cart
     *
     * @param cartId the ID of the cart
     * @return the total amount
     */
    BigDecimal calculateTotal(Integer cartId);

    /**
     * Clear all items from a cart
     *
     * @param cartId the ID of the cart to clear
     */
    void clearCart(Integer cartId);

    /**
     * Update the selection status of a cart item
     *
     * @param cartItemId the ID of the cart item
     * @param isSelected the selection status
     * @return the updated cart item
     */
    CartItem updateItemSelection(Integer cartItemId, Boolean isSelected);

    /**
     * Save an item for later
     *
     * @param cartItemId the ID of the cart item
     * @return the updated cart item
     */
    CartItem saveForLater(Integer cartItemId);

    /**
     * Move a saved item back to the cart
     *
     * @param cartItemId the ID of the cart item
     * @return the updated cart item
     */
    CartItem moveToCart(Integer cartItemId);

    /**
     * Apply a promotion code to the cart
     *
     * @param cartId    the ID of the cart
     * @param promoCode the promotion code to apply
     * @return the updated cart
     */
    Cart applyPromoCode(Integer cartId, String promoCode);

    /**
     * Get product recommendations for a cart
     *
     * @param cartId the ID of the cart
     * @param limit  the maximum number of recommendations to return
     * @return a list of recommended products
     */
    List<Product> getRecommendations(Integer cartId, int limit);

    /**
     * Get the count of items in a cart
     *
     * @param cartId the ID of the cart
     * @return the number of items in the cart
     */
    int getItemCount(Integer cartId);

    /**
     * Get items in a cart
     *
     * @param cartId the ID of the cart
     * @return a list of cart items
     */
    List<CartItem> getItems(Integer cartId);

    /**
     * Get the total amount of a cart
     *
     * @param cartId the ID of the cart
     * @return the total amount
     */
    BigDecimal getCartTotal(Integer cartId);

    /**
     * Get a cart item by its ID
     *
     * @param cartItemId the ID of the cart item to find
     * @return the cart item if found
     */
    CartItem getCartItem(Integer cartItemId);
}
