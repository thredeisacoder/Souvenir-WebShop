package project.demo.enums;

/**
 * Enum representing the possible statuses of a promotion.
 * Based on the CK_Promotion_Status check constraint in the database.
 */
public enum PromotionStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    EXPIRED("expired");

    private final String value;

    PromotionStatus(String value) {
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
    public static PromotionStatus fromValue(String value) {
        for (PromotionStatus status : PromotionStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid PromotionStatus value: " + value);
    }
}
