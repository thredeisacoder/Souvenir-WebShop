package project.demo.enums;

/**
 * Enum representing the possible types of revenue reports.
 * Based on the CK_RevenueReport_Type check constraint in the database.
 */
public enum ReportType {
    DAILY("daily"),
    MONTHLY("monthly"),
    QUARTERLY("quarterly"),
    YEARLY("yearly");

    private final String value;

    ReportType(String value) {
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
    public static ReportType fromValue(String value) {
        for (ReportType type : ReportType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ReportType value: " + value);
    }
}
