package project.demo.service;

import java.util.List;
import java.util.Optional;

import project.demo.model.Order;
import project.demo.model.OrderDetail;
import project.demo.model.OrderTimelineEvent;

public interface IOrderService {
    Optional<Order> findById(Integer orderId);
    List<Order> findByCustomerId(Integer customerId);
    Order createFromCart(Integer cartId, Integer addressId, Integer paymentMethodId, String shippingMethod);
    Order createFromCart(Integer cartId, Integer addressId, Integer paymentMethodId, String shippingMethod, String note);
    Order updateOrderStatus(Integer orderId, String status);
    void deleteOrder(Integer orderId);
    
    // Thêm các phương thức cần thiết
    List<OrderDetail> getOrderDetails(Integer orderId);
    List<OrderTimelineEvent> getOrderTimeline(Integer orderId);
    void cancelOrder(Integer orderId);
    void updateStatus(Integer orderId, String status);
    List<Order> findByStatus(String status);
    OrderTimelineEvent addTimelineEvent(OrderTimelineEvent event);
    
    /**
     * Apply a discount code to an order
     * @param orderId The order ID to apply the discount to
     * @param discountCode The discount code to apply
     * @return The updated Order after applying the discount
     */
    Order applyDiscount(Integer orderId, String discountCode);
    
    /**
     * Update an order detail
     * @param orderDetail The order detail to update
     * @return The updated OrderDetail
     */
    OrderDetail updateOrderDetail(OrderDetail orderDetail);
}
