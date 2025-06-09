package project.demo.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "Shipment", schema = "dbo", catalog = "SouvenirShopDBUser", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "order_id" })
})
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Integer shipmentId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "shipping_provider", length = 50)
    private String shippingProvider;

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @Column(name = "shipment_status", nullable = false, length = 20)
    private String shipmentStatus;

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "shipping_date")
    private LocalDate shippingDate;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @OneToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    public Shipment() {}

    public Shipment(Integer shipmentId, Integer orderId, String shippingProvider,
            String trackingNumber, String shipmentStatus, BigDecimal shippingCost,
            LocalDate shippingDate, LocalDate estimatedDeliveryDate, LocalDate deliveryDate) {
        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.shippingProvider = shippingProvider;
        this.trackingNumber = trackingNumber;
        this.shipmentStatus = shipmentStatus;
        this.shippingCost = shippingCost;
        this.shippingDate = shippingDate;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.deliveryDate = deliveryDate;
    }

    public Integer getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Integer shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getShippingProvider() {
        return shippingProvider;
    }

    public void setShippingProvider(String shippingProvider) {
        this.shippingProvider = shippingProvider;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getShipmentStatus() {
        return shipmentStatus;
    }

    public void setShipmentStatus(String shipmentStatus) {
        this.shipmentStatus = shipmentStatus;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public LocalDate getShippingDate() {
        return shippingDate;
    }

    public void setShippingDate(LocalDate shippingDate) {
        this.shippingDate = shippingDate;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
