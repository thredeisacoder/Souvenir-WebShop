package project.demo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import project.demo.enums.OrderStatus;

@Entity
@Table(name = "[Order]", schema = "dbo", catalog = "SouvenirShopDB")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "order_status", nullable = false, length = 20)
    private String orderStatus;

    @Column(name = "address_id", nullable = false)
    private Integer addressId;

    @Column(name = "shipping_method", length = 50)
    private String shippingMethod;

    @Column(name = "payment_method_id")
    private Integer paymentMethodId;

    @Column(name = "note", columnDefinition = "nvarchar(max)")
    private String note;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;
    
    @Transient
    private BigDecimal discountAmount;

    @Transient
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "address_id", insertable = false, updatable = false)
    private Address address;

    @ManyToOne
    @JoinColumn(name = "payment_method_id", insertable = false, updatable = false)
    private PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "order")
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "order")
    private List<OrderPromotion> orderPromotions;

    @OneToMany(mappedBy = "order")
    private List<Payment> payments;

    @OneToOne(mappedBy = "order")
    private Shipment shipment;

    @OneToMany(mappedBy = "order")
    private List<RevenueReport> revenueReports;

    @OneToMany(mappedBy = "order")
    private List<OrderTimelineEvent> orderTimelineEvents;

    public Order() {}

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Integer paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void setTotal(BigDecimal total) {
        this.totalAmount = total;
    }
    
    public OrderStatus getStatus() {
        return OrderStatus.fromValue(this.orderStatus);
    }
    
    public void setStatus(OrderStatus status) {
        this.orderStatus = status.getValue();
    }
    
    public void setShippingAddress(Address address) {
        this.address = address;
        this.addressId = address.getAddressId();
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public List<OrderPromotion> getOrderPromotions() {
        return orderPromotions;
    }

    public void setOrderPromotions(List<OrderPromotion> orderPromotions) {
        this.orderPromotions = orderPromotions;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public List<RevenueReport> getRevenueReports() {
        return revenueReports;
    }

    public void setRevenueReports(List<RevenueReport> revenueReports) {
        this.revenueReports = revenueReports;
    }
    
    public List<OrderTimelineEvent> getOrderTimelineEvents() {
        return orderTimelineEvents;
    }

    public void setOrderTimelineEvents(List<OrderTimelineEvent> orderTimelineEvents) {
        this.orderTimelineEvents = orderTimelineEvents;
    }
    
    public BigDecimal getTotal() {
        return totalAmount;
    }
}
