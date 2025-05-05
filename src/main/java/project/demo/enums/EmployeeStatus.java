package project.demo.enums;

/**
 * Enum representing the possible statuses of an employee.
 * Based on the CK_Employee_Status check constraint in the database.
 */
public enum EmployeeStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    ON_LEAVE("on_leave");

    private final String value;

    EmployeeStatus(String value) {
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
    public static EmployeeStatus fromValue(String value) {
        for (EmployeeStatus status : EmployeeStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid EmployeeStatus value: " + value);
    }
}
