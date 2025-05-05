package project.demo.enums;

/**
 * Enum representing the possible statuses of a shopping cart.
 * Based on the CK_Cart_Status check constraint in the database.
 */
public enum CartStatus {
    ACTIVE("active"),
    ABANDONED("abandoned"),
    ORDERED("ordered"),
    CONVERTED("converted"),
    DELETED("deleted");

    private final String value;

    CartStatus(String value) {
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
    public static CartStatus fromValue(String value) {
        for (CartStatus status : CartStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid CartStatus value: " + value);
    }
}
