package project.demo.enums;

/**
 * Enum representing the possible statuses of a customer.
 * Based on the CK_Customer_Status check constraint in the database.
 */
public enum CustomerStatus {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    CustomerStatus(String value) {
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
    public static CustomerStatus fromValue(String value) {
        for (CustomerStatus status : CustomerStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid CustomerStatus value: " + value);
    }
}
