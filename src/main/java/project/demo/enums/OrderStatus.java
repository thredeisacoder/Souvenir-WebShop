package project.demo.enums;

/**
 * Enum representing the possible statuses of an order.
 * Based on the CK_Order_Status check constraint in the database.
 */
public enum OrderStatus {
    NEW("new"),
    PENDING("pending"),
    ORDER_PLACED("Order Placed"),
    PROCESSING_PAYMENT_VERIFICATION("processing_payment_verification"),
    PROCESSING_PAYMENT_CONFIRMED("processing_payment_confirmed"),
    PROCESSING_PACKING("processing_packing"),
    SHIPPED("shipped"),
    IN_TRANSIT("in_transit"),
    OUT_FOR_DELIVERY("out_for_delivery"),
    DELIVERED("delivered"),
    CANCELLED("cancelled"),
    ON_HOLD("on_hold");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Convert a string value to the corresponding enum value.
     *
     * @param value the string value to convert
     * @return the corresponding enum value
     * @throws IllegalArgumentException if no matching enum value is found
     */
    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid OrderStatus value: " + value);
    }
}
