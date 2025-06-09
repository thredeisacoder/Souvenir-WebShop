package project.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import project.demo.model.Cart;

/**
 * Repository interface for Cart entities
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    
    /**
     * Find a cart by customer ID
     * 
     * @param customerId the ID of the customer
     * @return an Optional containing the cart if found
     */
    Optional<Cart> findByCustomerId(Integer customerId);
    
    /**
     * Find carts by status
     * 
     * @param status the status to search for
     * @return a list of carts with the specified status
     */
    List<Cart> findByStatus(String status);
    
    /**
     * Find active cart by customer ID
     * 
     * @param customerId the ID of the customer
     * @param status the status (typically "active")
     * @return an Optional containing the active cart if found
     */
    Optional<Cart> findByCustomerIdAndStatus(Integer customerId, String status);
    
    /**
     * Find carts by session ID
     * 
     * @param sessionId the session ID to search for
     * @return an Optional containing the cart if found
     */
    Optional<Cart> findBySessionId(String sessionId);
    
    /**
     * Update cart status directly using SQL query
     * 
     * @param cartId ID của giỏ hàng cần cập nhật
     * @param status Trạng thái mới cần cập nhật
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE SouvenirShopDBUser.dbo.Cart SET status = :status WHERE cart_id = :cartId", nativeQuery = true)
    void updateCartStatus(@Param("cartId") Integer cartId, @Param("status") String status);
}
