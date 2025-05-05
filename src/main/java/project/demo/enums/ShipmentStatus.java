package project.demo.enums;

/**
 * Enum representing the possible statuses of a shipment.
 * Based on the CK_Shipment_Status check constraint in the database.
 */
public enum ShipmentStatus {
    PENDING("pending"),
    IN_TRANSIT("in_transit"),
    DELIVERED("delivered"),
    FAILED("failed");

    private final String value;

    ShipmentStatus(String value) {
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
    public static ShipmentStatus fromValue(String value) {
        for (ShipmentStatus status : ShipmentStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ShipmentStatus value: " + value);
    }
}
