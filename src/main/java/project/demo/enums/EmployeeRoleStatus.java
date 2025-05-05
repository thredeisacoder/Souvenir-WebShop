package project.demo.enums;

/**
 * Enum representing the possible statuses of an employee role assignment.
 * Based on the CK_EmployeeRole_Status check constraint in the database.
 */
public enum EmployeeRoleStatus {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    EmployeeRoleStatus(String value) {
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
    public static EmployeeRoleStatus fromValue(String value) {
        for (EmployeeRoleStatus status : EmployeeRoleStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid EmployeeRoleStatus value: " + value);
    }
}
