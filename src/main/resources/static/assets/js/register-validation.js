/**
 * Register form validation script
 * Uses the ValidationErrorMessages utility to validate the registration form
 * Performs only client-side validation without modifying the DOM directly
 */
document.addEventListener('DOMContentLoaded', function() {
    // Create an instance of the ValidationErrorMessages utility
    const validator = new ValidationErrorMessages();
    
    // Wait for error messages to load
    validator.loading.then(() => {
        const registerForm = document.querySelector('form.auth-form');
        if (!registerForm) return;
        
        // Get form elements
        const fullNameInput = document.getElementById('fullName');
        const emailInput = document.getElementById('email');
        const phoneNumberInput = document.getElementById('phoneNumber');
        const passwordInput = document.getElementById('password');
        const confirmPasswordInput = document.getElementById('confirmPassword');
        
        // Validate form on submit (client-side validation only)
        registerForm.addEventListener('submit', function(event) {
            let isValid = true;
            
            // Validate full name
            if (validator.validateName(fullNameInput.value)) {
                isValid = false;
            }
            
            // Validate email
            if (validator.validateEmail(emailInput.value)) {
                isValid = false;
            }
            
            // Validate phone number
            if (validator.validatePhone(phoneNumberInput.value)) {
                isValid = false;
            }
            
            // Validate password
            if (validator.validatePassword(passwordInput.value)) {
                isValid = false;
            }
            
            // Validate confirm password
            if (validator.validatePasswordMatch(
                passwordInput.value, confirmPasswordInput.value
            )) {
                isValid = false;
            }
            
            // Prevent form submission if there are errors
            if (!isValid) {
                event.preventDefault();
            }
        });
    });
}); 