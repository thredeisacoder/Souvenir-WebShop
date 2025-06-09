package project.demo.model;

import java.time.LocalDateTime;

public class PendingPayment {
    private String transactionId;
    private String vnpayReference;
    private Long amount;
    private Integer orderId; // Actual order ID when order creation succeeds
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String status; // "pending_order_creation", "completed", "failed"
    private String customerNote;
    
    // Session data that was lost
    private Integer customerId;
    private Integer addressId;
    private String shippingMethodId;
    private String orderNote;
    
    public PendingPayment() {
        this.paymentTime = LocalDateTime.now();
        this.status = "pending_order_creation";
        this.paymentMethod = "VNPay";
    }
    
    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getVnpayReference() { return vnpayReference; }
    public void setVnpayReference(String vnpayReference) { this.vnpayReference = vnpayReference; }
    
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCustomerNote() { return customerNote; }
    public void setCustomerNote(String customerNote) { this.customerNote = customerNote; }
    
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    
    public Integer getAddressId() { return addressId; }
    public void setAddressId(Integer addressId) { this.addressId = addressId; }
    
    public String getShippingMethodId() { return shippingMethodId; }
    public void setShippingMethodId(String shippingMethodId) { this.shippingMethodId = shippingMethodId; }
    
    public String getOrderNote() { return orderNote; }
    public void setOrderNote(String orderNote) { this.orderNote = orderNote; }
} 