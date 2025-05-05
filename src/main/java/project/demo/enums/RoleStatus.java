package project.demo.enums;

/**
 * Enum representing the possible statuses of a role.
 * Based on the CK_Role_Status check constraint in the database.
 */
public enum RoleStatus {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    RoleStatus(String value) {
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
    public static RoleStatus fromValue(String value) {
        for (RoleStatus status : RoleStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid RoleStatus value: " + value);
    }
}
