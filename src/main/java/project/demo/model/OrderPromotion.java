package project.demo.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "OrderPromotion", schema = "dbo", catalog = "SouvenirShopDB")
public class OrderPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_promotion_id")
    private Integer orderPromotionId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "promotion_id", nullable = false)
    private Integer promotionId;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "note", columnDefinition = "nvarchar(max)")
    private String note;

    @ManyToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "promotion_id", insertable = false, updatable = false)
    private Promotion promotion;

    public OrderPromotion() {}

    public OrderPromotion(Integer orderPromotionId, Integer orderId, Integer promotionId,
            BigDecimal discountAmount, String note) {
        this.orderPromotionId = orderPromotionId;
        this.orderId = orderId;
        this.promotionId = promotionId;
        this.discountAmount = discountAmount;
        this.note = note;
    }

    public Integer getOrderPromotionId() {
        return orderPromotionId;
    }

    public void setOrderPromotionId(Integer orderPromotionId) {
        this.orderPromotionId = orderPromotionId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Integer promotionId) {
        this.promotionId = promotionId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }
}
