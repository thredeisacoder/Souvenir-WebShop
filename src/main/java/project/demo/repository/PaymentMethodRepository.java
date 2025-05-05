package project.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import project.demo.model.PaymentMethod;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

    /**
     * Lấy danh sách phương thức thanh toán của khách hàng
     *
     * @param customerId ID khách hàng
     * @return Danh sách phương thức thanh toán
     */
    List<PaymentMethod> findByCustomerId(int customerId);

    /**
     * Lấy phương thức thanh toán theo ID và ID khách hàng
     *
     * @param paymentMethodId ID phương thức thanh toán
     * @param customerId      ID khách hàng
     * @return Optional chứa phương thức thanh toán hoặc empty nếu không tìm thấy
     */
    Optional<PaymentMethod> findByPaymentMethodIdAndCustomerId(int paymentMethodId, int customerId);

    // Các phương thức liên quan đến isDefault đã được loại bỏ vì isDefault là trường @Transient
    // Việc quản lý phương thức thanh toán mặc định sẽ được xử lý trong service

    /**
     * Xóa phương thức thanh toán theo ID và ID khách hàng
     *
     * @param paymentMethodId ID phương thức thanh toán
     * @param customerId      ID khách hàng
     * @return Số bản ghi đã xóa
     */
    int deleteByPaymentMethodIdAndCustomerId(int paymentMethodId, int customerId);

    /**
     * Kiểm tra phương thức thanh toán có thuộc về khách hàng không
     *
     * @param paymentMethodId ID phương thức thanh toán
     * @param customerId      ID khách hàng
     * @return true nếu phương thức thanh toán thuộc về khách hàng, false nếu không
     */
    boolean existsByPaymentMethodIdAndCustomerId(int paymentMethodId, int customerId);

    /**
     * Find payment methods by method name
     */
    List<PaymentMethod> findByMethodName(String methodName);

    /**
     * Find payment methods by provider
     */
    List<PaymentMethod> findByProvider(String provider);

    /**
     * Find payment methods by customer ID and method name
     */
    List<PaymentMethod> findByCustomerIdAndMethodName(Integer customerId, String methodName);
}