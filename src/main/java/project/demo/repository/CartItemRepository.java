package project.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.demo.model.CartItem;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entities
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    /**
     * Find all items in a cart
     *
     * @param cartId the ID of the cart
     * @return a list of cart items
     */
    List<CartItem> findByCartId(Integer cartId);

    /**
     * Find a specific item in a cart by product
     *
     * @param cartId    the ID of the cart
     * @param productId the ID of the product
     * @return an Optional containing the cart item if found
     */
    Optional<CartItem> findByCartIdAndProductId(Integer cartId, Integer productId);

    /**
     * Find selected items in a cart
     *
     * @param cartId     the ID of the cart
     * @param isSelected the selection status
     * @return a list of selected cart items
     */
    List<CartItem> findByCartIdAndIsSelected(Integer cartId, Boolean isSelected);

    /**
     * Delete all items in a cart
     *
     * @param cartId the ID of the cart
     */
    void deleteByCartId(Integer cartId);

    /**
     * Count the number of items in a cart
     *
     * @param cartId the ID of the cart
     * @return the number of items in the cart
     */
    long countByCartId(Integer cartId);

}
