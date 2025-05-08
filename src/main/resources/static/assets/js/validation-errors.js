/**
 * Validation error messages utility
 * Loads error messages from the server and provides functions for validating forms
 */
class ValidationErrorMessages { 
    constructor() {
        this.errorMessages = {};
        this.loaded = false;
        this.loading = this.loadErrorMessages();
    }

    /**
     * Loads error messages from the server
     */
    async loadErrorMessages() {
        try {
            const response = await fetch('/error-messages.json');
            if (!response.ok) {
                console.error('Failed to load error messages:', response.status);
                return;
            }
            
            const data = await response.json();
            this.errorMessages = {};
            
            // Convert the array of errors to a map for easier lookup
            data.errors.forEach(error => {
                this.errorMessages[error.messageCode] = error.description;
            });
            
            this.loaded = true;
            console.log('Error messages loaded successfully');
        } catch (error) {
            console.error('Error loading error messages:', error);
        }
    }

    /**
     * Gets an error message by its code
     * @param {string} errorCode - The error code
     * @returns {string} The error message, or the error code if not found
     */
    getMessage(errorCode) {
        return this.errorMessages[errorCode] || errorCode;
    }
    
    /**
     * Validates that a field is not empty
     * @param {string} value - The field value
     * @param {string} errorCode - The error code to use if validation fails
     * @returns {string|null} The error message, or null if valid
     */
    validateRequired(value, errorCode) {
        if (!value || value.trim() === '') {
            return this.getMessage(errorCode);
        }
        return null;
    }
    
    /**
     * Validates a name field
     * @param {string} name - The name to validate
     * @returns {string|null} The error message, or null if valid
     */
    validateName(name) {
        if (!name || name.trim() === '') {
            return this.getMessage('INVALID_NAME');
        }
        
        // Check if name contains digits
        if (/\d/.test(name)) {
            return this.getMessage('INVALID_NAME_WITH_DIGITS');
        }
        
        return null;
    }
    
    /**
     * Validates a phone number
     * @param {string} phone - The phone number to validate
     * @returns {string|null} The error message, or null if valid
     */
    validatePhone(phone) {
        if (!phone || phone.trim() === '') {
            return this.getMessage('INVALID_PHONE');
        }
        
        // Remove any non-digit characters
        const digits = phone.replace(/\D/g, '');
        
        // Check if the phone number has 10 or 11 digits
        if (digits.length !== 10 && digits.length !== 11) {
            return this.getMessage('NUMBER_PHONE_ERROR');
        }
        
        return null;
    }
    
    /**
     * Validates an email address
     * @param {string} email - The email to validate
     * @returns {string|null} The error message, or null if valid
     */
    validateEmail(email) {
        if (!email || email.trim() === '') {
            return this.getMessage('INVALID_EMAIL');
        }
        
        // Basic email validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            return this.getMessage('INVALID_EMAIL_FORMAT');
        }
        
        return null;
    }
    
    /**
     * Validates a password
     * @param {string} password - The password to validate
     * @returns {string|null} The error message, or null if valid
     */
    validatePassword(password) {
        if (!password || password.trim() === '') {
            return this.getMessage('INVALID_PASSWORD');
        }
        
        // Basic password validation - at least 8 characters
        if (password.length < 8) {
            return "Mật khẩu phải có ít nhất 8 ký tự";
        }
        
        return null;
    }
    
    /**
     * Validates that passwords match
     * @param {string} password - The password
     * @param {string} confirmPassword - The confirmed password
     * @returns {string|null} The error message, or null if valid
     */
    validatePasswordMatch(password, confirmPassword) {
        if (password !== confirmPassword) {
            return "Mật khẩu xác nhận không khớp";
        }
        return null;
    }
    
    /**
     * Validates an address
     * @param {string} address - The address to validate
     * @returns {string|null} The error message, or null if valid
     */
    validateAddress(address) {
        if (!address || address.trim() === '') {
            return this.getMessage('INVALID_ADDRESS');
        }
        return null;
    }
    
    /**
     * Validates a credit card number
     * @param {string} cardNumber - The card number to validate
     * @returns {string|null} The error message, or null if valid
     */
    validateCardNumber(cardNumber) {
        if (!cardNumber || cardNumber.trim() === '') {
            return this.getMessage('INVALID_CARD_NUMBER');
        }
        
        // Remove spaces and dashes
        const digits = cardNumber.replace(/[\s-]/g, '');
        
        // Check length (13-19 digits are valid)
        if (digits.length < 13 || digits.length > 19) {
            return this.getMessage('INVALID_CARD_NUMBER');
        }
        
        // Check if all characters are digits
        if (!/^\d+$/.test(digits)) {
            return this.getMessage('INVALID_CARD_NUMBER');
        }
        
        // Optional: Implement Luhn algorithm for additional validation
        // For simplicity, we'll skip it here
        
        return null;
    }
    
    /**
     * Validates a credit card expiry date
     * @param {string} expiryDate - The expiry date to validate (MM/YY format)
     * @returns {string|null} The error message, or null if valid
     */
    validateExpiryDate(expiryDate) {
        if (!expiryDate || expiryDate.trim() === '') {
            return this.getMessage('INVALID_EXPIRATION_DATE');
        }
        
        // Check format (MM/YY)
        if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(expiryDate)) {
            return this.getMessage('INVALID_EXPIRATION_DATE');
        }
        
        // Check if the expiry date is in the future
        const [month, yearShort] = expiryDate.split('/');
        const year = 2000 + parseInt(yearShort, 10);
        const expiryDateObj = new Date(year, parseInt(month, 10) - 1, 1);
        const today = new Date();
        
        if (expiryDateObj < today) {
            return this.getMessage('INVALID_EXPIRATION_DATE');
        }
        
        return null;
    }
    
    /**
     * Validates a credit card security code (CVV/CVC)
     * @param {string} securityCode - The security code to validate
     * @returns {string|null} The error message, or null if valid
     */
    validateSecurityCode(securityCode) {
        if (!securityCode || securityCode.trim() === '') {
            return this.getMessage('INVALID_SECURITY_CODE');
        }
        
        // Check if the security code is 3 or 4 digits
        if (!/^\d{3,4}$/.test(securityCode)) {
            return this.getMessage('INVALID_SECURITY_CODE');
        }
        
        return null;
    }
}

// Create a global instance
const validationErrors = new ValidationErrorMessages();

// Example usage in a form validation function
function validateCheckoutForm(form) {
    // Wait for error messages to be loaded
    if (!validationErrors.loaded) {
        return true; // Allow form submission if messages aren't loaded
    }
    
    let isValid = true;
    const errors = {};
    
    // Get form fields
    const address = form.querySelector('[name="address"]')?.value;
    const wardCommune = form.querySelector('[name="wardCommune"]')?.value;
    const district = form.querySelector('[name="district"]')?.value;
    const provinceCity = form.querySelector('[name="provinceCity"]')?.value;
    const country = form.querySelector('[name="country"]')?.value;
    const phone = form.querySelector('[name="phone"]')?.value;
    const email = form.querySelector('[name="email"]')?.value;
    
    // Get payment method
    const paymentMethod = document.getElementById('selectedPaymentMethod')?.value;
    
    // Validate required fields
    const addressError = validationErrors.validateRequired(address, 'INVALID_ADDRESSLINE');
    if (addressError) {
        errors.address = addressError;
        isValid = false;
    }
    
    const wardError = validationErrors.validateRequired(wardCommune, 'INVALID_WARD_COMMUNE');
    if (wardError) {
        errors.wardCommune = wardError;
        isValid = false;
    }
    
    const districtError = validationErrors.validateRequired(district, 'INVALID_DISTRICT');
    if (districtError) {
        errors.district = districtError;
        isValid = false;
    }
    
    const provinceError = validationErrors.validateRequired(provinceCity, 'INVALID_PROVINCE_CITY');
    if (provinceError) {
        errors.provinceCity = provinceError;
        isValid = false;
    }
    
    const countryError = validationErrors.validateRequired(country, 'INVALID_COUNTRY');
    if (countryError) {
        errors.country = countryError;
        isValid = false;
    }
    
    // Validate phone and email
    const phoneError = validationErrors.validatePhone(phone);
    if (phoneError) {
        errors.phone = phoneError;
        isValid = false;
    }
    
    const emailError = validationErrors.validateEmail(email);
    if (emailError) {
        errors.email = emailError;
        isValid = false;
    }
    
    // Validate credit card details if credit card payment is selected
    if (paymentMethod === 'credit') {
        const cardNumber = form.querySelector('#cardNumber')?.value;
        const expiryDate = form.querySelector('#expiryDate')?.value;
        const cvv = form.querySelector('#cvv')?.value;
        
        const cardNumberError = validationErrors.validateCardNumber(cardNumber);
        if (cardNumberError) {
            errors.cardNumber = cardNumberError;
            isValid = false;
        }
        
        const expiryDateError = validationErrors.validateExpiryDate(expiryDate);
        if (expiryDateError) {
            errors.expiryDate = expiryDateError;
            isValid = false;
        }
        
        const securityCodeError = validationErrors.validateSecurityCode(cvv);
        if (securityCodeError) {
            errors.cvv = securityCodeError;
            isValid = false;
        }
    }
    
    // Display errors if any
    if (!isValid) {
        displayFormErrors(form, errors);
    }
    
    return isValid;
}

/**
 * Displays form errors next to the corresponding fields
 * @param {HTMLElement} form - The form element
 * @param {Object} errors - An object mapping field names to error messages
 */
function displayFormErrors(form, errors) {
    // Clear existing error messages
    form.querySelectorAll('.error-message').forEach(el => el.remove());
    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    
    // Add new error messages
    for (const [fieldName, errorMessage] of Object.entries(errors)) {
        const field = form.querySelector(`[name="${fieldName}"]`);
        if (field) {
            // Add error class to the field
            field.classList.add('is-invalid');
            
            // Create error element
            const errorElement = document.createElement('div');
            errorElement.className = 'error-message invalid-feedback';
            errorElement.textContent = errorMessage;
            
            // Add error message after the field
            field.parentNode.insertBefore(errorElement, field.nextSibling);
        }
    }
    
    // Scroll to the first error
    const firstErrorField = form.querySelector('.is-invalid');
    if (firstErrorField) {
        firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' });
        firstErrorField.focus();
    }
} 