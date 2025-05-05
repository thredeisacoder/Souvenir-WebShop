package project.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.demo.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /**
     * Lấy danh sách thanh toán theo đơn hàng
     *
     * @param orderId ID đơn hàng
     * @return Danh sách thanh toán
     */
    List<Payment> findByOrderId(int orderId);

    /**
     * Lấy danh sách thanh toán theo khách hàng
     *
     * @param customerId ID khách hàng
     * @return Danh sách thanh toán
     */
    @Query("SELECT p FROM Payment p JOIN p.order o WHERE o.customerId = :customerId ORDER BY p.paymentDate DESC")
    List<Payment> findByCustomerId(@Param("customerId") int customerId);

    /**
     * Lấy danh sách thanh toán theo trạng thái
     *
     * @param paymentStatus Trạng thái thanh toán
     * @return Danh sách thanh toán
     */
    List<Payment> findByPaymentStatus(String paymentStatus);

    /**
     * Lấy danh sách thanh toán theo ID phương thức thanh toán
     *
     * @param paymentMethodId ID phương thức thanh toán
     * @return Danh sách thanh toán
     */
    List<Payment> findByPaymentMethodId(int paymentMethodId);

    /**
     * Lấy danh sách thanh toán trong khoảng thời gian
     *
     * @param startDate Thời gian bắt đầu
     * @param endDate   Thời gian kết thúc
     * @return Danh sách thanh toán
     */
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Cập nhật trạng thái thanh toán
     *
     * @param paymentId ID thanh toán
     * @param status    Trạng thái mới
     * @return Số bản ghi đã cập nhật
     */
    @Modifying
    @Query("UPDATE Payment p SET p.paymentStatus = :status WHERE p.paymentId = :paymentId")
    int updateStatus(@Param("paymentId") int paymentId, @Param("status") String status);

    /**
     * Tính tổng số tiền thanh toán thành công của đơn hàng
     *
     * @param orderId ID đơn hàng
     * @return Tổng số tiền
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.orderId = :orderId AND p.paymentStatus = 'SUCCESS'")
    BigDecimal getTotalSuccessfulPaymentAmount(@Param("orderId") int orderId);

    /**
     * Kiểm tra đơn hàng đã thanh toán đủ chưa
     *
     * @param orderId ID đơn hàng
     * @return true nếu đã thanh toán đủ, false nếu chưa
     */
    @Query("SELECT CASE WHEN SUM(p.amount) >= o.totalAmount THEN true ELSE false END " +
            "FROM Payment p JOIN p.order o " +
            "WHERE p.orderId = :orderId AND p.paymentStatus = 'SUCCESS' GROUP BY o.totalAmount")
    boolean isOrderFullyPaid(@Param("orderId") int orderId);
}