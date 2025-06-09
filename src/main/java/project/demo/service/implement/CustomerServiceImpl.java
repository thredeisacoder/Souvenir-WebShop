package project.demo.service.implement;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.demo.enums.CustomerStatus;
import project.demo.exception.CustomerException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Customer;
import project.demo.repository.CustomerDirectRepository;
import project.demo.repository.CustomerRepository;
import project.demo.service.ICustomerService;

/**
 * Implementation of the ICustomerService interface for managing Customer
 * entities
 */
@Service("customerService")
public class CustomerServiceImpl implements ICustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerDirectRepository customerDirectRepository;

    private final PasswordEncoder passwordEncoder;

    // Regular expressions for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");
    // Simplified password pattern for testing - just require at least 6 characters
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,}$");

    public CustomerServiceImpl(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Customer> findById(Integer customerId) {
        return customerRepository.findById(customerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Customer register(Customer customer) {
        try {
            // Validate customer
            validateCustomer(customer);
            // Thiết lập ngày tham gia TRƯỚC các bước kiểm tra khác
            customer.setJoinDate(java.time.LocalDate.now());
            // Check for duplicate email
            boolean emailExists = customerRepository.existsByEmail(customer.getEmail());
            System.out.println("DEBUG: Email exists check: " + emailExists + " for email: " + customer.getEmail());

            if (emailExists) {
                System.out.println("DEBUG: Duplicate email detected: " + customer.getEmail());
                throw CustomerException.duplicateEmail(customer.getEmail());
            }

            // Check for duplicate phone number
            boolean phoneExists = customerRepository.existsByPhoneNumber(customer.getPhoneNumber());
            System.out
                    .println("DEBUG: Phone exists check: " + phoneExists + " for phone: " + customer.getPhoneNumber());

            if (phoneExists) {
                System.out.println("DEBUG: Duplicate phone number detected: " + customer.getPhoneNumber());
                throw CustomerException.duplicatePhoneNumber(customer.getPhoneNumber());
            }

            // Validate password strength
            if (!PASSWORD_PATTERN.matcher(customer.getPassword()).matches()) {
                System.out.println("DEBUG: Weak password detected: " + customer.getPassword().length() + " characters");
                throw CustomerException.weakPassword();
            }

            // Encode the password before storing
            String rawPassword = customer.getPassword();
            String encodedPassword = passwordEncoder.encode(rawPassword);
            customer.setPassword(encodedPassword);
            System.out.println("DEBUG - Storing encoded password: " + encodedPassword);

            // Set default status
            customer.setStatus(CustomerStatus.ACTIVE.getValue());

            // Manually set database column values
            System.out.println("DEBUG: Preparing customer fields for database insertion");
            System.out.println("full_name: " + customer.getFullName());
            System.out.println("email: " + customer.getEmail());
            System.out.println("phone_number: " + customer.getPhoneNumber());
            System.out.println("status: " + customer.getStatus());

            Customer savedCustomer = null;

            try {
                System.out.println("DEBUG: Attempting to save customer to database using JPA");
                savedCustomer = customerRepository.save(customer);
                System.out.println("DEBUG: Customer saved successfully with ID: " + savedCustomer.getCustomerId());
            } catch (Exception e) {
                System.err.println("DEBUG: Error saving customer using JPA: " + e.getMessage());
                e.printStackTrace();

                // Try alternative method using direct SQL
                System.out.println("DEBUG: Attempting to save customer using direct SQL");
                Integer customerId = customerDirectRepository.insertCustomerDirectly(customer);
                if (customerId != null) {
                    customer.setCustomerId(customerId);
                    savedCustomer = customer;
                    System.out.println("DEBUG: Customer saved successfully using direct SQL with ID: " + customerId);
                } else {
                    throw new RuntimeException("Failed to save customer using both JPA and direct SQL");
                }
            }

            return savedCustomer;
        } catch (Exception e) {
            System.err.println("DEBUG: Unexpected error in register method: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Customer update(Customer customer) {
        // Check if customer exists
        if (customer.getCustomerId() == null) {
            throw CustomerException.missingRequiredField("Customer ID");
        }

        Customer existingCustomer = findById(customer.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND",
                        "Customer not found with ID: " + customer.getCustomerId()));

        // Validate customer
        validateCustomerForUpdate(customer, existingCustomer);

        // Keep the existing password (don't update password through this method)
        customer.setPassword(existingCustomer.getPassword());

        return customerRepository.save(customer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authenticate(String email, String password) {
        try {
            // Find customer by email
            System.out.println("DEBUG-AUTH-FLOW - ===== Bắt đầu quá trình xác thực =====");
            System.out.println("DEBUG-AUTH-FLOW - Email đang xác thực: '" + email + "'");
            
            if (email == null || email.trim().isEmpty()) {
                System.out.println("DEBUG-AUTH-FLOW - [LỖI] Email trống hoặc null");
                throw new CustomerException("Email không được để trống", "EMAIL_NOT_FOUND");
            }
            
            if (password == null || password.trim().isEmpty()) {
                System.out.println("DEBUG-AUTH-FLOW - [LỖI] Mật khẩu trống hoặc null");
                throw new CustomerException("Mật khẩu không được để trống", "INCORRECT_PASSWORD");
            }
            
            Optional<Customer> customerOpt = customerRepository.findByEmail(email.trim().toLowerCase());
            if (!customerOpt.isPresent()) {
                System.out.println("DEBUG-AUTH-FLOW - [LỖI] Không tìm thấy tài khoản với email: " + email);
                throw new CustomerException("Không tìm thấy tài khoản với email này", "EMAIL_NOT_FOUND");
            }

            Customer customer = customerOpt.get();
            System.out.println("DEBUG-AUTH-FLOW - Đã tìm thấy tài khoản: ID=" + customer.getCustomerId() + ", Status="
                    + customer.getStatus());

            // Check if customer is active
            if (!CustomerStatus.ACTIVE.getValue().equals(customer.getStatus())) {
                System.out.println(
                        "DEBUG-AUTH-FLOW - [LỖI] Tài khoản không hoạt động. Status hiện tại: " + customer.getStatus());
                throw new CustomerException("Tài khoản của bạn đã bị vô hiệu hóa", "INACTIVE_CUSTOMER");
            }

            // Verify password using PasswordEncoder
            System.out.println("DEBUG-AUTH-FLOW - Kiểm tra mật khẩu...");
            System.out.println("DEBUG-AUTH-FLOW - Độ dài mật khẩu nhập vào: " + password.length());
            System.out.println("DEBUG-AUTH-FLOW - Mật khẩu đã mã hóa: " + (customer.getPassword() != null ? "Có" : "Null"));

            // Additional null check for stored password
            if (customer.getPassword() == null || customer.getPassword().trim().isEmpty()) {
                System.out.println("DEBUG-AUTH-FLOW - [LỖI] Mật khẩu trong database bị null hoặc trống");
                throw new CustomerException("Lỗi dữ liệu tài khoản. Vui lòng liên hệ hỗ trợ", "ACCOUNT_DATA_ERROR");
            }

            boolean passwordMatches = false;
            try {
                passwordMatches = passwordEncoder.matches(password, customer.getPassword());
                System.out.println(
                        "DEBUG-AUTH-FLOW - Kết quả so sánh mật khẩu: " + (passwordMatches ? "KHỚP" : "KHÔNG KHỚP"));
            } catch (Exception passwordError) {
                System.out.println("DEBUG-AUTH-FLOW - [LỖI] Lỗi khi so sánh mật khẩu: " + passwordError.getMessage());
                throw new CustomerException("Lỗi xác thực mật khẩu", "PASSWORD_VERIFICATION_ERROR");
            }

            if (!passwordMatches) {
                System.out.println("DEBUG-AUTH-FLOW - [LỖI] Mật khẩu không khớp");
                throw new CustomerException("Mật khẩu không chính xác", "INCORRECT_PASSWORD");
            }

            System.out.println("DEBUG-AUTH-FLOW - Xác thực thành công!");
            return true;
        } catch (CustomerException e) {
            // Re-throw CustomerException as-is
            throw e;
        } catch (Exception e) {
            System.err
                    .println("DEBUG-AUTH-FLOW - [LỖI] Lỗi không mong đợi trong quá trình xác thực: " + e.getMessage());
            e.printStackTrace();
            throw new CustomerException("Lỗi hệ thống khi xác thực", "AUTHENTICATION_SYSTEM_ERROR");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean changePassword(Integer customerId, String oldPassword, String newPassword) {
        // Find customer
        Optional<Customer> customerOpt = findById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();

            // Verify old password using PasswordEncoder
            System.out.println(
                    "DEBUG - Comparing old password with stored encoded password");
            if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
                throw CustomerException.incorrectOldPassword();
            }

            // Validate new password strength
            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                throw CustomerException.weakPassword();
            }

            // Encode and update the new password
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            System.out.println("DEBUG - Updating password to encoded value");
            customer.setPassword(encodedNewPassword);
            customerRepository.save(customer);

            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    /**
     * Deactivates a customer's account
     *
     * @param customerId the ID of the customer to deactivate
     */
    @Transactional
    public void deactivateAccount(Integer customerId) {
        // Find customer
        Optional<Customer> customerOpt = findById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();

            // Update status
            customer.setStatus(CustomerStatus.INACTIVE.getValue());
            customerRepository.save(customer);
        }
    }

    /**
     * Activates a customer's account
     *
     * @param customerId the ID of the customer to activate
     */
    @Transactional
    public void activateAccount(Integer customerId) {
        // Find customer
        Optional<Customer> customerOpt = findById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();

            // Update status
            customer.setStatus(CustomerStatus.ACTIVE.getValue());
            customerRepository.save(customer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(Integer customerId) {
        customerRepository.deleteById(customerId);
    }

    /**
     * Validates a customer for registration
     *
     * @param customer the customer to validate
     * @throws CustomerException if validation fails
     */
    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        // Validate required fields
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            throw CustomerException.missingRequiredField("Full name");
        }

        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw CustomerException.missingRequiredField("Email");
        }

        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            throw CustomerException.missingRequiredField("Phone number");
        }

        if (customer.getPassword() == null || customer.getPassword().trim().isEmpty()) {
            throw CustomerException.missingRequiredField("Password");
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
            throw CustomerException.invalidEmailFormat(customer.getEmail());
        }

        // Validate phone number format
        if (!PHONE_PATTERN.matcher(customer.getPhoneNumber()).matches()) {
            throw CustomerException.invalidPhoneNumberFormat(customer.getPhoneNumber());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOriginalPassword(Integer customerId) {
        // Note: This is for demonstration only. In production, you should:
        // 1. Generate a secure reset token instead of returning the actual password
        // 2. Store the token temporarily with expiration
        // 3. Send the reset link, not the actual password

        Optional<Customer> customerOpt = findById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();

            // For this demo, we'll generate a temporary password
            // In a real system, you should generate a secure reset token instead
            String temporaryPassword = "TempPass" + customerId + "123";

            // Update customer with the new temporary password
            String encodedTempPassword = passwordEncoder.encode(temporaryPassword);
            customer.setPassword(encodedTempPassword);
            customerRepository.save(customer);

            System.out.println(
                    "DEBUG - Generated temporary password: " + temporaryPassword + " for customer: " + customerId);

            return temporaryPassword;
        }
        throw new RuntimeException("Customer not found");
    }

    /**
     * Validates a customer for update
     *
     * @param customer         the customer to validate
     * @param existingCustomer the existing customer
     * @throws CustomerException if validation fails
     */
    private void validateCustomerForUpdate(Customer customer, Customer existingCustomer) {
        validateCustomer(customer);

        // Check for duplicate email (if changed)
        if (!customer.getEmail().equals(existingCustomer.getEmail()) &&
                customerRepository.existsByEmail(customer.getEmail())) {
            throw CustomerException.duplicateEmail(customer.getEmail());
        }

        // Check for duplicate phone number (if changed)
        if (!customer.getPhoneNumber().equals(existingCustomer.getPhoneNumber()) &&
                customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
            throw CustomerException.duplicatePhoneNumber(customer.getPhoneNumber());
        }
    }
}
