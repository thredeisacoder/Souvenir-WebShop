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
        if (!errorCode) {
            return 'Vui lòng kiểm tra lại thông tin';
        }
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
            return this.getMessage('INVALID_PASSWORD_LENGTH');
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

    /**
     * Validates an address form
     * @param {HTMLElement} form - The form element
     * @param {boolean} isEditForm - Whether this is an edit form
     * @returns {Object} Object containing validation errors
     */
    validateAddressForm(form, isEditForm = false) {
        const prefix = isEditForm ? 'edit' : '';
        const errors = {};
        
        // Validate street address
        const streetAddress = document.getElementById(prefix + (prefix ? 'S' : 's') + 'treetAddress')?.value.trim();
        if (!streetAddress) {
            errors.streetAddress = this.getMessage('INVALID_ADDRESSLINE');
        }
        
        // Validate ward/commune
        const wardCommune = document.getElementById(prefix + 'wardCommune')?.value.trim();
        if (!wardCommune) {
            errors.wardCommune = this.getMessage('INVALID_WARD_COMMUNE');
        }
        
        // Validate district
        const district = document.getElementById(prefix + 'district')?.value.trim();
        if (!district) {
            errors.district = this.getMessage('INVALID_DISTRICT');
        }
        
        // Validate province/city
        const provinceCity = document.getElementById(prefix + 'provinceCity')?.value;
        if (!provinceCity) {
            errors.provinceCity = this.getMessage('INVALID_PROVINCE_CITY');
        }
        
        // Validate country
        const country = document.getElementById(prefix + 'country')?.value;
        if (!country) {
            errors.country = this.getMessage('INVALID_COUNTRY');
        }
        
        // Validate zip code
        const zipCode = document.getElementById(prefix + 'zipCode')?.value.trim();
        if (!zipCode) {
            errors.zipCode = this.getMessage('INVALID_ZIPCODE');
        }
        
        return errors;
    }

    /**
     * Clears all error messages from a form
     * @param {HTMLElement} form - The form element
     */
    clearFormErrors(form) {
        // Remove is-invalid class from all fields
        form.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
        });
        
        // Clear all invalid-feedback elements
        form.querySelectorAll('.invalid-feedback').forEach(el => {
            el.textContent = '';
            el.style.display = 'none';
        });
    }

    /**
     * Displays validation errors in a form
     * @param {HTMLElement} form - The form element
     * @param {Object} errors - Object containing validation errors
     * @param {boolean} isEditForm - Whether this is an edit form
     */
    displayFormErrors(form, errors, isEditForm = false) {
        // Clear existing errors
        this.clearFormErrors(form);
        
        // Add prefix for edit form fields
        const prefix = isEditForm ? 'edit' : '';
        
        // Display new errors
        for (const [fieldName, errorMessage] of Object.entries(errors)) {
            // Determine field ID based on field name and form type
            let fieldId;
            if (fieldName === 'streetAddress') {
                fieldId = prefix + (prefix ? 'S' : 's') + 'treetAddress';
            } else {
                fieldId = prefix + fieldName;
            }
            
            const field = form.querySelector(`#${fieldId}`);
            if (field) {
                // Add error class to input field
                field.classList.add('is-invalid');
                
                // Find and display invalid-feedback element
                const errorElement = document.getElementById(`${fieldId}-error`) || 
                                   field.nextElementSibling;
                
                if (errorElement && errorElement.classList.contains('invalid-feedback')) {
                    errorElement.textContent = errorMessage;
                    errorElement.style.display = 'block';
                } else {
                    // Create new element if not found
                    const newErrorElement = document.createElement('div');
                    newErrorElement.className = 'invalid-feedback';
                    newErrorElement.textContent = errorMessage;
                    newErrorElement.style.display = 'block';
                    field.parentNode.insertBefore(newErrorElement, field.nextSibling);
                }
            } else {
                console.warn(`Field ${fieldId} not found for error display`);
            }
        }
        
        // Scroll to first error field
        const firstErrorField = form.querySelector('.is-invalid');
        if (firstErrorField) {
            firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' });
            firstErrorField.focus();
        }
    }
}

// Create a global instance
const validationErrors = new ValidationErrorMessages();

// Export for use in other files
window.validationErrors = validationErrors;

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
        validationErrors.displayFormErrors(form, errors);
    }
    
    return isValid;
}

// Validate quantity input
function validateQuantity(input) {
    if (!input.value || input.value.trim() === '') {
        return {
            isValid: false,
            messageCode: 'EMPTY_FIELD'
        };
    }
    
    const value = parseInt(input.value);
    if (isNaN(value) || value < 1) {
        return {
            isValid: false,
            messageCode: 'INVALID_INPUT'
        };
    }
    
    const max = parseInt(input.getAttribute('max'));
    if (value > max) {
        return {
            isValid: false,
            messageCode: 'PRODUCT_OUT_OF_STOCK'
        };
    }
    
    return {
        isValid: true
    };
} 