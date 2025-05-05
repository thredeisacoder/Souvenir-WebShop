package project.demo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity to store daily/monthly/yearly revenue data
 */
@Entity
@Table(name = "RevenueReport", schema = "dbo", catalog = "SouvenirShopDB")
public class RevenueReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer reportId;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;
    
    @Column(name = "total_revenue", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenue;
    
    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;
    
    @Column(name = "total_inventory_cost", precision = 15, scale = 2)
    private BigDecimal totalInventoryCost;
    
    @Column(name = "total_shipping_cost", precision = 15, scale = 2)
    private BigDecimal totalShippingCost;
    
    @Column(name = "total_profit", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalProfit;
    
    @Column(name = "report_type", nullable = false, length = 20)
    private String reportType;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "order_id")
    private Integer orderId;
    
    @Column(name = "inventory_transaction_id")
    private Integer inventoryTransactionId;
    
    @Column(name = "shipping_revenue", precision = 15, scale = 2)
    private BigDecimal shippingRevenue;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "net_revenue", precision = 15, scale = 2)
    private BigDecimal netRevenue;
    
    @ManyToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "inventory_transaction_id", insertable = false, updatable = false)
    private StockLedger stockLedger;
    
    public RevenueReport() {
        this.createdAt = LocalDateTime.now();
        this.totalRevenue = BigDecimal.ZERO;
        this.totalOrders = 0;
        this.totalInventoryCost = BigDecimal.ZERO;
        this.totalShippingCost = BigDecimal.ZERO;
        this.totalProfit = BigDecimal.ZERO;
        this.shippingRevenue = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.netRevenue = BigDecimal.ZERO;
    }
    
    public RevenueReport(LocalDate reportDate, String reportType) {
        this();
        this.reportDate = reportDate;
        this.reportType = reportType;
        this.startDate = reportDate;
        this.endDate = reportDate;
    }
    
    public RevenueReport(Integer reportId, LocalDate reportDate, BigDecimal totalRevenue, Integer totalOrders,
                          BigDecimal totalInventoryCost, BigDecimal totalShippingCost, BigDecimal totalProfit,
                          String reportType, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt,
                          LocalDateTime updatedAt, Integer orderId, Integer inventoryTransactionId) {
        this.reportId = reportId;
        this.reportDate = reportDate;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.totalInventoryCost = totalInventoryCost;
        this.totalShippingCost = totalShippingCost;
        this.totalProfit = totalProfit;
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.orderId = orderId;
        this.inventoryTransactionId = inventoryTransactionId;
        this.shippingRevenue = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.netRevenue = BigDecimal.ZERO;
    }
    
    /**
     * Add order information to the revenue report
     * 
     * @param orderTotal      The subtotal of the order
     * @param shippingFee     The shipping fee
     * @param discountAmount  The discount amount
     */
    public void addOrder(Double orderTotal, Double shippingFee, Double discountAmount) {
        this.totalOrders++;
        
        BigDecimal orderTotalBD = BigDecimal.valueOf(orderTotal);
        BigDecimal shippingFeeBD = BigDecimal.valueOf(shippingFee);
        BigDecimal discountAmountBD = BigDecimal.valueOf(discountAmount);
        
        // Add to total revenue (subtotal + shipping)
        if (this.totalRevenue == null) {
            this.totalRevenue = BigDecimal.ZERO;
        }
        this.totalRevenue = this.totalRevenue.add(orderTotalBD.add(shippingFeeBD));
        
        // Add to shipping revenue
        if (this.shippingRevenue == null) {
            this.shippingRevenue = BigDecimal.ZERO;
        }
        this.shippingRevenue = this.shippingRevenue.add(shippingFeeBD);
        
        // Add to discount amount
        if (this.discountAmount == null) {
            this.discountAmount = BigDecimal.ZERO;
        }
        this.discountAmount = this.discountAmount.add(discountAmountBD);
        
        // Calculate net revenue (total revenue - discount)
        if (this.netRevenue == null) {
            this.netRevenue = BigDecimal.ZERO;
        }
        this.netRevenue = this.totalRevenue.subtract(this.discountAmount);
        
        // Update updated_at timestamp
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalInventoryCost() {
        return totalInventoryCost;
    }

    public void setTotalInventoryCost(BigDecimal totalInventoryCost) {
        this.totalInventoryCost = totalInventoryCost;
    }

    public BigDecimal getTotalShippingCost() {
        return totalShippingCost;
    }

    public void setTotalShippingCost(BigDecimal totalShippingCost) {
        this.totalShippingCost = totalShippingCost;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getInventoryTransactionId() {
        return inventoryTransactionId;
    }

    public void setInventoryTransactionId(Integer inventoryTransactionId) {
        this.inventoryTransactionId = inventoryTransactionId;
    }
    
    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public StockLedger getStockLedger() {
        return stockLedger;
    }

    public void setStockLedger(StockLedger stockLedger) {
        this.stockLedger = stockLedger;
    }

    public BigDecimal getShippingRevenue() {
        return shippingRevenue;
    }

    public void setShippingRevenue(BigDecimal shippingRevenue) {
        this.shippingRevenue = shippingRevenue;
    }
    
    public void setShippingRevenue(Double shippingRevenue) {
        this.shippingRevenue = BigDecimal.valueOf(shippingRevenue);
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = BigDecimal.valueOf(discountAmount);
    }

    public BigDecimal getNetRevenue() {
        return netRevenue;
    }

    public void setNetRevenue(BigDecimal netRevenue) {
        this.netRevenue = netRevenue;
    }
    
    public void setNetRevenue(Double netRevenue) {
        this.netRevenue = BigDecimal.valueOf(netRevenue);
    }
}
