package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an issue with an address.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AddressException extends BusinessLogicException {
    
    private static final String DEFAULT_ERROR_CODE = "ADDRESS_ERROR";
    
    /**
     * Constructs a new exception with a default error code and the specified detail message.
     * 
     * @param message the detail message
     */
    public AddressException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
    
    /**
     * Constructs a new exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public AddressException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Creates a new exception for an address that belongs to another customer.
     * 
     * @param addressId the ID of the address
     * @param customerId the ID of the customer
     * @return a new AddressException
     */
    public static AddressException notOwnedByCustomer(Integer addressId, Integer customerId) {
        return new AddressException(
            "ADDRESS_NOT_OWNED_BY_CUSTOMER",
            String.format("Address with ID %d does not belong to customer with ID %d", addressId, customerId)
        );
    }
    
    /**
     * Creates a new exception for a missing required field.
     * 
     * @param fieldName the name of the missing field
     * @return a new AddressException
     */
    public static AddressException missingRequiredField(String fieldName) {
        return new AddressException(
            "MISSING_REQUIRED_FIELD",
            String.format("%s is required", fieldName)
        );
    }
    
    /**
     * Creates a new exception for an invalid address format.
     * 
     * @param fieldName the name of the field with invalid format
     * @param value the invalid value
     * @return a new AddressException
     */
    public static AddressException invalidFormat(String fieldName, String value) {
        return new AddressException(
            "INVALID_ADDRESS_FORMAT",
            String.format("Invalid format for %s: %s", fieldName, value)
        );
    }
    
    /**
     * Creates a new exception for an address that cannot be deleted.
     * 
     * @param addressId the ID of the address
     * @param reason the reason why the address cannot be deleted
     * @return a new AddressException
     */
    public static AddressException cannotDelete(Integer addressId, String reason) {
        return new AddressException(
            "CANNOT_DELETE_ADDRESS",
            String.format("Cannot delete address with ID %d: %s", addressId, reason)
        );
    }
}
