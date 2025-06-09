package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an issue with a customer.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomerException extends BusinessLogicException {

    private static final String DEFAULT_ERROR_CODE = "CUSTOMER_ERROR";

    /**
     * Constructs a new exception with a default error code and the specified detail
     * message.
     *
     * @param message the detail message
     */
    public CustomerException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * Constructs a new exception with the specified error code and detail message.
     *
     * @param errorCode the error code
     * @param message   the detail message
     */
    public CustomerException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Creates a new exception for a duplicate email.
     *
     * @param email the email
     * @return a new CustomerException
     */
    public static CustomerException duplicateEmail(String email) {
        return new CustomerException(
                "DUPLICATE_EMAIL",
                String.format("Email '%s' is already registered", email));
    }

    /**
     * Creates a new exception for a duplicate phone number.
     *
     * @param phoneNumber the phone number
     * @return a new CustomerException
     */
    public static CustomerException duplicatePhoneNumber(String phoneNumber) {
        return new CustomerException(
                "DUPLICATE_PHONE_NUMBER",
                String.format("Phone number '%s' is already registered", phoneNumber));
    }

    /**
     * Creates a new exception for an inactive customer.
     *
     * @param customerId the customer ID
     * @return a new CustomerException
     */
    public static CustomerException inactive(Integer customerId) {
        return new CustomerException(
                "INACTIVE_CUSTOMER",
                String.format("Customer with ID %d is inactive", customerId));
    }

    /**
     * Creates a new exception for invalid credentials.
     *
     * @return a new CustomerException
     */
    public static CustomerException invalidCredentials() {
        return new CustomerException(
                "INVALID_CREDENTIALS",
                "Invalid email or password");
    }

    /**
     * Creates a new exception for email not found.
     *
     * @param email the email that was not found
     * @return a new CustomerException
     */
    public static CustomerException emailNotFound(String email) {
        return new CustomerException(
                "EMAIL_NOT_FOUND",
                String.format("No account found with email: %s", email));
    }

    /**
     * Creates a new exception for incorrect password.
     *
     * @return a new CustomerException
     */
    public static CustomerException incorrectPassword() {
        return new CustomerException(
                "INCORRECT_PASSWORD",
                "The password you entered is incorrect");
    }

    /**
     * Creates a new exception for a missing required field.
     *
     * @param fieldName the name of the missing field
     * @return a new CustomerException
     */
    public static CustomerException missingRequiredField(String fieldName) {
        return new CustomerException(
                "MISSING_REQUIRED_FIELD",
                String.format("%s is required", fieldName));
    }

    /**
     * Creates a new exception for an invalid email format.
     *
     * @param email the invalid email
     * @return a new CustomerException
     */
    public static CustomerException invalidEmailFormat(String email) {
        return new CustomerException(
                "INVALID_EMAIL_FORMAT",
                String.format("Invalid email format: %s", email));
    }

    /**
     * Creates a new exception for an invalid phone number format.
     *
     * @param phoneNumber the invalid phone number
     * @return a new CustomerException
     */
    public static CustomerException invalidPhoneNumberFormat(String phoneNumber) {
        return new CustomerException(
                "INVALID_PHONE_NUMBER_FORMAT",
                String.format("Invalid phone number format: %s", phoneNumber));
    }

    /**
     * Creates a new exception for a weak password.
     *
     * @return a new CustomerException
     */
    public static CustomerException weakPassword() {
        return new CustomerException(
                "WEAK_PASSWORD",
                "Password is too weak. It should contain at least 8 characters, including uppercase, lowercase, numbers, and special characters.");
    }

    /**
     * Creates a new exception for incorrect old password during password change.
     *
     * @return a new CustomerException
     */
    public static CustomerException incorrectOldPassword() {
        return new CustomerException(
                "INCORRECT_OLD_PASSWORD",
                "The old password is incorrect");
    }

    /**
     * Creates a new exception for account data errors.
     *
     * @return a new CustomerException
     */
    public static CustomerException accountDataError() {
        return new CustomerException(
                "ACCOUNT_DATA_ERROR",
                "Account data is corrupted. Please contact support");
    }

    /**
     * Creates a new exception for password verification errors.
     *
     * @return a new CustomerException
     */
    public static CustomerException passwordVerificationError() {
        return new CustomerException(
                "PASSWORD_VERIFICATION_ERROR",
                "Error occurred during password verification");
    }

    /**
     * Creates a new exception for authentication system errors.
     *
     * @return a new CustomerException
     */
    public static CustomerException authenticationSystemError() {
        return new CustomerException(
                "AUTHENTICATION_SYSTEM_ERROR",
                "System error during authentication");
    }
}
