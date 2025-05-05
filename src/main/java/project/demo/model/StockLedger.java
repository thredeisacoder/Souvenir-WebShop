package project.demo.model;

import java.math.BigDecimal;
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
import jakarta.persistence.Table;

@Entity
@Table(name = "StockLedger", schema = "dbo", catalog = "SouvenirShopDB1")
public class StockLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_transaction_id")
    private Integer inventoryTransactionId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "before_quantity")
    private Integer beforeQuantity;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "after_quantity")
    private Integer afterQuantity;

    @Column(name = "performed_by")
    private Integer performedBy;

    @Column(name = "reason", columnDefinition = "nvarchar(max)")
    private String reason;

    @Column(name = "note", columnDefinition = "nvarchar(max)")
    private String note;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "performed_by", insertable = false, updatable = false)
    private Employee performedByEmployee;

    @OneToMany(mappedBy = "stockLedger")
    private List<RevenueReport> revenueReports;

    public StockLedger() {}

    public StockLedger(Integer inventoryTransactionId, Integer productId, String transactionType,
            LocalDateTime transactionDate, Integer beforeQuantity, Integer quantity,
            Integer afterQuantity, Integer performedBy, String reason, String note,
            BigDecimal costPrice) {
        this.inventoryTransactionId = inventoryTransactionId;
        this.productId = productId;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.beforeQuantity = beforeQuantity;
        this.quantity = quantity;
        this.afterQuantity = afterQuantity;
        this.performedBy = performedBy;
        this.reason = reason;
        this.note = note;
        this.costPrice = costPrice;
    }

    public Integer getInventoryTransactionId() {
        return inventoryTransactionId;
    }

    public void setInventoryTransactionId(Integer inventoryTransactionId) {
        this.inventoryTransactionId = inventoryTransactionId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Integer getBeforeQuantity() {
        return beforeQuantity;
    }

    public void setBeforeQuantity(Integer beforeQuantity) {
        this.beforeQuantity = beforeQuantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getAfterQuantity() {
        return afterQuantity;
    }

    public void setAfterQuantity(Integer afterQuantity) {
        this.afterQuantity = afterQuantity;
    }

    public Integer getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Integer performedBy) {
        this.performedBy = performedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Employee getPerformedByEmployee() {
        return performedByEmployee;
    }

    public void setPerformedByEmployee(Employee performedByEmployee) {
        this.performedByEmployee = performedByEmployee;
    }

    public List<RevenueReport> getRevenueReports() {
        return revenueReports;
    }

    public void setRevenueReports(List<RevenueReport> revenueReports) {
        this.revenueReports = revenueReports;
    }
}
