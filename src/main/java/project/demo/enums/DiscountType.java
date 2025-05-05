package project.demo.enums;

/**
 * Enum representing the possible types of discounts for promotions.
 * Based on the CK_Promotion_Type check constraint in the database.
 */
public enum DiscountType {
    PERCENTAGE("percentage"),
    FIXED_AMOUNT("fixed_amount"),
    FREE_SHIPPING("free_shipping");

    private final String value;

    DiscountType(String value) {
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
    public static DiscountType fromValue(String value) {
        for (DiscountType type : DiscountType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid DiscountType value: " + value);
    }
}
