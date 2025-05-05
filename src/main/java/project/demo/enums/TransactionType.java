package project.demo.enums;

/**
 * Enum representing the possible types of stock transactions.
 * Based on the CK_StockLedger_Type check constraint in the database.
 */
public enum TransactionType {
    IMPORT("import"),
    EXPORT("export"),
    SELL("sell"),
    COMBINE("combine");

    private final String value;

    TransactionType(String value) {
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
    public static TransactionType fromValue(String value) {
        for (TransactionType type : TransactionType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid TransactionType value: " + value);
    }
}
