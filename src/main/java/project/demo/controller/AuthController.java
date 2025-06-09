package project.demo.controller;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import project.demo.enums.CustomerStatus;
import project.demo.exception.CustomerException;
import project.demo.model.Customer;
import project.demo.security.oauth2.CustomOAuth2User;
import project.demo.service.AuthenticationService;
import project.demo.service.IAddressService;
import project.demo.service.ICartService;
import project.demo.service.ICustomerService;
import project.demo.service.IEmailService;
import project.demo.util.CustomerOAuth2Utils;

/**
 * Controller for handling authentication operations
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private final ICustomerService customerService;
    private final ICartService cartService;
    private final AuthenticationService authenticationService;
    private final IAddressService addressService;
    private final IEmailService emailService;

    // Regular expression for email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"); // email
                                                                                                                        // phải
                                                                                                                        // có
                                                                                                                        // @
                                                                                                                        // và
                                                                                                                        // .

    // Regular expression for phone number validation
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$"); // số điện thoại phải có 10-15
                                                                                        // số

    // Regular expression for password strength validation (simplified for easier
    // testing)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{8,}$"); // mật khẩu phải có ít nhất 8 ký tự

    public AuthController(ICustomerService customerService, ICartService cartService,
            AuthenticationService authenticationService, IAddressService addressService,
            IEmailService emailService) {
        this.customerService = customerService;
        this.cartService = cartService;
        this.authenticationService = authenticationService;
        this.addressService = addressService;
        this.emailService = emailService;
    }

    /**
     * Show login form
     */
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String redirect,
            @RequestParam(required = false) String denied,
            HttpSession session,
            Model model) {
        if (error != null) {
            if ("oauth2".equals(error)) {
                model.addAttribute("oauth2Error", "Đăng nhập qua mạng xã hội thất bại. Vui lòng thử lại.");
            } else {
                model.addAttribute("errorMessage", "Invalid email or password");
            }
        }

        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }

        if (denied != null) {
            model.addAttribute("errorMessage", "Bạn cần đăng nhập để truy cập trang này");
        }

        // Lấy thông báo đăng nhập từ session nếu có
        String loginMessage = (String) session.getAttribute("loginMessage");
        if (loginMessage != null) {
            model.addAttribute("loginMessage", loginMessage);
            session.removeAttribute("loginMessage");
        }

        // Store the redirect URL in session if provided
        if (redirect != null) {
            if (redirect.equals("cart")) {
                if (!model.containsAttribute("loginMessage")) {
                    model.addAttribute("loginMessage", "Please log in to view your cart");
                }
                session.setAttribute("redirectUrl", "/cart");
            } else {
                session.setAttribute("redirectUrl", "/" + redirect);
            }
        }

        // Add OAuth2 providers for template
        model.addAttribute("oauth2Providers", new String[] { "google" });

        return "auth/login";
    }

    /**
     * Show registration form
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "auth/register";
    }

    /**
     * Process registration form
     */
    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute Customer customer,
            BindingResult bindingResult,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validate customer data
        validateCustomer(customer, bindingResult);

        // Check if password matches confirmPassword
        if (!customer.getPassword().equals(confirmPassword)) {
            model.addAttribute("confirmPasswordError", "Passwords do not match");
        }

        if (bindingResult.hasErrors() || model.containsAttribute("confirmPasswordError")) {
            model.addAttribute("customer", customer);
            System.out.println("Registration validation failed: " + bindingResult.getAllErrors());
            return "auth/register";
        }

        try {
            // Log customer data for debugging
            System.out.println("Attempting to register customer: " +
                    "Email=" + customer.getEmail() +
                    ", FullName=" + customer.getFullName() +
                    ", PhoneNumber=" + customer.getPhoneNumber());

            // Set default status for new customers
            customer.setStatus("ACTIVE");

            // Register the customer
            Customer registeredCustomer = customerService.register(customer);

            System.out.println("Customer registered successfully with ID: " + registeredCustomer.getCustomerId());

            // Create a cart for the customer and save it
            cartService.getOrCreateCart(registeredCustomer.getCustomerId());

            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");

            return "redirect:/auth/login";
        } catch (CustomerException e) {
            System.err.println(
                    "CustomerException during registration: " + e.getMessage() + " - Error code: " + e.getErrorCode());
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalArgumentException during registration: " + e.getMessage());
            model.addAttribute("errorMessage", "Invalid input: " + e.getMessage());
            return "auth/register";
        } catch (RuntimeException e) {
            System.err.println("RuntimeException during registration: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "auth/register";
        } catch (Exception e) {
            System.err.println("Unexpected exception during registration: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "A system error occurred. Please contact support.");
            return "auth/register";
        }
    }

    /**
     * Process login form submission
     */
    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String redirect,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            // Authenticate user using AuthenticationService
            authenticationService.login(email, password);

            // Check if there's a pending cart item to add
            Integer pendingProductId = (Integer) session.getAttribute("pendingProductId");
            Integer pendingQuantity = (Integer) session.getAttribute("pendingQuantity");

            if (pendingProductId != null && pendingQuantity != null) {
                // Clear the pending attributes
                session.removeAttribute("pendingProductId");
                session.removeAttribute("pendingQuantity");

                // Redirect to add the item to cart
                return "redirect:/cart/add?productId=" + pendingProductId + "&quantity=" + pendingQuantity;
            }

            // Redirect to home page or previous page
            String redirectUrl = (String) session.getAttribute("redirectUrl");
            if (redirectUrl != null) {
                session.removeAttribute("redirectUrl");
                return "redirect:" + redirectUrl;
            }

            return "redirect:/";
        } catch (CustomerException e) {
            // Add specific error message based on the exception type
            String errorCode = e.getErrorCode();

            // Preserve the entered email for user convenience
            model.addAttribute("email", email);
            if (redirect != null && !redirect.isEmpty()) {
                model.addAttribute("redirect", redirect);
            }

            // Log the error for debugging
            System.out.println("DEBUG-LOGIN - CustomerException: " + errorCode + " - " + e.getMessage());

            switch (errorCode) {
                case "EMAIL_NOT_FOUND" -> {
                    model.addAttribute("emailError", true);
                    model.addAttribute("emailErrorMessage", "Không tìm thấy tài khoản với email này");
                    model.addAttribute("errorMessage", "Email không tồn tại trong hệ thống");
                }
                case "INCORRECT_PASSWORD" -> {
                    model.addAttribute("passwordError", true);
                    model.addAttribute("passwordErrorMessage", "Mật khẩu không chính xác");
                    model.addAttribute("errorMessage", "Mật khẩu không đúng, vui lòng thử lại");
                }
                case "INACTIVE_CUSTOMER" -> {
                    model.addAttribute("accountError", true);
                    model.addAttribute("accountErrorMessage", "Tài khoản đã bị vô hiệu hóa");
                    model.addAttribute("errorMessage", "Tài khoản của bạn đã bị tạm khóa. Vui lòng liên hệ hỗ trợ");
                }
                case "SESSION_ERROR" -> {
                    model.addAttribute("errorMessage", "Lỗi hệ thống trong quá trình đăng nhập. Vui lòng thử lại");
                }
                case "ACCOUNT_DATA_ERROR" -> {
                    model.addAttribute("errorMessage", "Dữ liệu tài khoản có vấn đề. Vui lòng liên hệ hỗ trợ");
                }
                case "PASSWORD_VERIFICATION_ERROR" -> {
                    model.addAttribute("errorMessage", "Lỗi xác thực mật khẩu. Vui lòng thử lại");
                }
                case "AUTHENTICATION_SYSTEM_ERROR" -> {
                    model.addAttribute("errorMessage", "Lỗi hệ thống xác thực. Vui lòng thử lại sau");
                }
                default -> {
                    model.addAttribute("errorMessage", "Đăng nhập không thành công. Vui lòng kiểm tra thông tin và thử lại");
                }
            }

            return "auth/login";
        } catch (Exception e) {
            System.err.println("DEBUG-LOGIN - Unexpected error during login: " + e.getMessage());
            e.printStackTrace();

            // Clear any sensitive session data
            try {
                session.removeAttribute("customer");
                session.removeAttribute("isLoggedIn");
            } catch (Exception sessionError) {
                System.err.println("Error clearing session: " + sessionError.getMessage());
            }

            // Show user-friendly error message
            model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau ít phút");
            model.addAttribute("email", email);
            if (redirect != null && !redirect.isEmpty()) {
                model.addAttribute("redirect", redirect);
            }

            return "auth/login";
        }
    }

    /**
     * Logout user
     */
    @GetMapping("/logout")
    public String logout(RedirectAttributes redirectAttributes) {
        authenticationService.logout();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
        return "redirect:/";
    }

    /**
     * Show profile page
     */
    @GetMapping("/profile")
    public String showProfile(Model model, RedirectAttributes redirectAttributes) {
        if (!authenticationService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to view your profile");
            return "redirect:/auth/login?redirect=profile";
        }

        Customer customer = authenticationService.getCurrentCustomer();

        // Get customer addresses
        var addresses = addressService.findByCustomerId(customer.getCustomerId());
        // Ensure the addresses list is not null even if the service returns null
        if (addresses == null) {
            addresses = java.util.Collections.emptyList();
        }

        model.addAttribute("addresses", addresses);
        model.addAttribute("customer", customer);
        return "auth/profile";
    }

    /**
     * Process profile update
     */
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute Customer customer,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!authenticationService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to update your profile");
            return "redirect:/auth/login?redirect=profile";
        }

        Customer currentCustomer = authenticationService.getCurrentCustomer();
        customer.setCustomerId(currentCustomer.getCustomerId());

        validateProfileUpdate(customer, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("customer", customer);
            model.addAttribute("errorMessage", "Please correct the errors below");
            return "auth/profile";
        }

        try {
            customer.setPassword(currentCustomer.getPassword());
            customer.setStatus(currentCustomer.getStatus());

            Customer updatedCustomer = customerService.update(customer);

            // Update customer in session via AuthenticationService
            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest().getSession();
            session.setAttribute("customer", updatedCustomer);
            session.setAttribute("customerName", updatedCustomer.getFullName());

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
            return "redirect:/auth/profile";
        } catch (CustomerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "auth/profile";
        }
    }

    /**
     * Process password change
     */
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!authenticationService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to change your password");
            return "redirect:/auth/login?redirect=profile";
        }

        Customer customer = authenticationService.getCurrentCustomer();

        if (newPassword == null || newPassword.trim().isEmpty()) {
            model.addAttribute("passwordError", "New password is required");
            model.addAttribute("customer", customer);
            return "auth/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("passwordError", "New password and confirmation do not match");
            model.addAttribute("customer", customer);
            return "auth/profile";
        }

        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            model.addAttribute("passwordError", "Password must be at least 8 characters");
            model.addAttribute("customer", customer);
            return "auth/profile";
        }

        try {
            boolean success = customerService.changePassword(customer.getCustomerId(), currentPassword, newPassword);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully");
            } else {
                model.addAttribute("passwordError", "Failed to change password");
                model.addAttribute("customer", customer);
                return "auth/profile";
            }

            return "redirect:/auth/profile";
        } catch (CustomerException e) {
            model.addAttribute("passwordError", e.getMessage());
            model.addAttribute("customer", customer);
            return "auth/profile";
        } catch (Exception e) {
            model.addAttribute("passwordError", "An unexpected error occurred. Please try again.");
            model.addAttribute("customer", customer);
            return "auth/profile";
        }
    }

    /**
     * Show forgot password form
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    /**
     * Process forgot password request
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            // Validate email format
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                model.addAttribute("errorMessage", "Định dạng email không hợp lệ.");
                model.addAttribute("email", email);
                return "auth/forgot-password";
            }

            // Xác thực email tồn tại
            Optional<Customer> customerOptional = customerService.findByEmail(email);
            if (customerOptional.isEmpty()) {
                model.addAttribute("errorMessage", "Email không tồn tại trong hệ thống.");
                model.addAttribute("email", email);
                return "auth/forgot-password";
            }

            Customer customer = customerOptional.get();

            // Debug log to check customer status
            System.out.println("DEBUG - Customer status from DB: '" + customer.getStatus() + "'");
            System.out.println("DEBUG - Expected active status: '" + CustomerStatus.ACTIVE.getValue() + "'");
            System.out.println(
                    "DEBUG - Status comparison: " + CustomerStatus.ACTIVE.getValue().equals(customer.getStatus()));

            // Check if customer is active (handle null and trim whitespace)
            String customerStatus = customer.getStatus();
            if (customerStatus == null ||
                    !CustomerStatus.ACTIVE.getValue().equals(customerStatus.trim())) {
                model.addAttribute("errorMessage", "Tài khoản của bạn hiện không hoạt động. Vui lòng liên hệ hỗ trợ.");
                return "auth/forgot-password";
            }

            // Send forgot password email with current password
            try {
                // Get the original password (this is for demonstration, in real apps you should
                // generate a reset token)
                String originalPassword = customerService.getOriginalPassword(customer.getCustomerId());

                emailService.sendForgotPasswordEmail(
                        customer.getEmail(),
                        customer.getFullName(),
                        originalPassword);

                System.out.println("Forgot password email sent to: " + customer.getEmail());
                model.addAttribute("successMessage",
                        "Mật khẩu đã được gửi đến email " + email + ". Vui lòng kiểm tra hộp thư của bạn.");

            } catch (Exception emailException) {
                System.err.println("Failed to send email: " + emailException.getMessage());
                emailException.printStackTrace();
                model.addAttribute("errorMessage",
                        "Không thể gửi email. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.");
                model.addAttribute("email", email);
            }

            return "auth/forgot-password";
        } catch (CustomerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", email);
            return "auth/forgot-password";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.");
            model.addAttribute("email", email);
            return "auth/forgot-password";
        }
    }

    // /**
    // * Show orders page - duplicate of /account/orders
    // */
    // @GetMapping("/orders")
    // public String showOrders(Model model, RedirectAttributes redirectAttributes)
    // {
    // if (!authenticationService.isAuthenticated()) {
    // redirectAttributes.addFlashAttribute("loginMessage", "Vui lòng đăng nhập để
    // xem đơn hàng của bạn");
    // return "redirect:/auth/login?redirect=account/orders";
    // }

    // try {
    // Customer customer = authenticationService.getCurrentCustomer();

    // // Lấy danh sách đơn hàng của khách hàng
    // var orders = customerService.getCustomerOrders(customer.getCustomerId());

    // model.addAttribute("orders", orders);
    // return "account/orders"; // Sử dụng template giống với /account/orders
    // } catch (Exception e) {
    // model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải đơn hàng: " +
    // e.getMessage());
    // return "account/orders";
    // }
    // }

    // /**
    // * Show order details - duplicate of /account/orders/{id}
    // */
    // @GetMapping("/orders/{orderId}")
    // public String showOrderDetails(@PathVariable Integer orderId, Model model,
    // RedirectAttributes redirectAttributes) {
    // if (!authenticationService.isAuthenticated()) {
    // redirectAttributes.addFlashAttribute("loginMessage", "Vui lòng đăng nhập để
    // xem chi tiết đơn hàng");
    // return "redirect:/auth/login?redirect=account/orders/" + orderId;
    // }

    // try {
    // Customer customer = authenticationService.getCurrentCustomer();

    // // Lấy thông tin đơn hàng từ service
    // var order = customerService.getOrderById(orderId, customer.getCustomerId());

    // if (order == null) {
    // model.addAttribute("errorMessage", "Không tìm thấy đơn hàng hoặc đơn hàng
    // không thuộc về bạn");
    // return "account/orders";
    // }

    // model.addAttribute("order", order);
    // return "account/order-details"; // Sử dụng template giống với
    // /account/orders/{id}
    // } catch (Exception e) {
    // model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải chi tiết đơn hàng:
    // " + e.getMessage());
    // return "account/orders";
    // }
    // }

    /**
     * Validate customer data for registration
     */
    private void validateCustomer(Customer customer, BindingResult result) {
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            result.rejectValue("fullName", "INVALID_NAME", "Trường hợp chưa nhập họ và tên");
        } else if (customer.getFullName().matches(".*\\d.*")) {
            // Kiểm tra nếu tên chứa số
            result.rejectValue("fullName", "INVALID_NAME_WITH_DIGITS", "Họ và tên không được chứa số");
        }

        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            result.rejectValue("email", "INVALID_EMAIL", "Trường hợp chưa nhập Email");
        } else if (!EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
            result.rejectValue("email", "INVALID_EMAIL_FORMAT", "Trường hợp nhập Email không đúng định dạng");
        }

        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            result.rejectValue("phoneNumber", "INVALID_PHONE", "Trường hợp chưa nhập số điện thoại");
        } else if (!PHONE_PATTERN.matcher(customer.getPhoneNumber()).matches()) {
            result.rejectValue("phoneNumber", "NUMBER_PHONE_ERROR",
                    "Trường hợp số điện thoại không phải là 10 hoặc 11 số");
        }

        if (customer.getPassword() == null || customer.getPassword().trim().isEmpty()) {
            result.rejectValue("password", "INVALID_PASSWORD", "Trường hợp chưa nhập mật khẩu");
        } else if (!PASSWORD_PATTERN.matcher(customer.getPassword()).matches()) {
            result.rejectValue("password", "INVALID_PASSWORD_LENGTH", "Mật khẩu phải có ít nhất 8 ký tự");
        }
    }

    /**
     * Validate customer data for profile update (excluding password)
     */
    private void validateProfileUpdate(Customer customer, BindingResult result) {
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            result.rejectValue("fullName", "INVALID_NAME", "Trường hợp chưa nhập họ và tên");
        } else if (customer.getFullName().matches(".*\\d.*")) {
            // Kiểm tra nếu tên chứa số
            result.rejectValue("fullName", "INVALID_NAME_WITH_DIGITS", "Họ và tên không được chứa số");
        }

        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            result.rejectValue("email", "INVALID_EMAIL", "Trường hợp chưa nhập Email");
        } else if (!EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
            result.rejectValue("email", "INVALID_EMAIL_FORMAT", "Trường hợp nhập Email không đúng định dạng");
        }

        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            result.rejectValue("phoneNumber", "INVALID_PHONE", "Trường hợp chưa nhập số điện thoại");
        } else if (!PHONE_PATTERN.matcher(customer.getPhoneNumber()).matches()) {
            result.rejectValue("phoneNumber", "NUMBER_PHONE_ERROR",
                    "Trường hợp số điện thoại không phải là 10 hoặc 11 số");
        }
    }

    /**
     * OAuth2 Complete Profile Form
     * For users who logged in via OAuth2 but need to complete their profile
     */
    @GetMapping("/complete-profile")
    public String showCompleteProfileForm(Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        // Get customer from OAuth2 authentication
        Customer customer = null;
        if (auth.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User oauth2User = (CustomOAuth2User) auth.getPrincipal();
            customer = oauth2User.getCustomer();
        } else {
            // Non-OAuth2 users don't need to complete profile here
            return "redirect:/account/profile";
        }

        if (customer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tải thông tin tài khoản.");
            return "redirect:/auth/login";
        }

        String provider = CustomerOAuth2Utils.getOAuth2Provider(customer);
        model.addAttribute("customer", customer);
        model.addAttribute("provider", provider);
        model.addAttribute("providerDisplayName", getProviderDisplayName(provider));

        return "auth/complete-profile";
    }

    /**
     * Process OAuth2 Complete Profile Form
     */
    @PostMapping("/complete-profile")
    public String processCompleteProfile(@ModelAttribute Customer profileData,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        // Get current customer from OAuth2 authentication
        Customer currentCustomer = null;
        if (auth.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User oauth2User = (CustomOAuth2User) auth.getPrincipal();
            currentCustomer = oauth2User.getCustomer();
        } else {
            return "redirect:/account/profile";
        }

        if (currentCustomer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tải thông tin tài khoản.");
            return "redirect:/auth/login";
        }

        try {
            // Validate required profile data
            validateOAuth2ProfileCompletion(profileData, bindingResult);

            if (bindingResult.hasErrors()) {
                String provider = CustomerOAuth2Utils.getOAuth2Provider(currentCustomer);
                model.addAttribute("customer", profileData);
                model.addAttribute("provider", provider);
                model.addAttribute("providerDisplayName", getProviderDisplayName(provider));
                return "auth/complete-profile";
            }

            // Update current customer with profile completion data
            boolean hasChanges = false;

            // Update phone number if provided and different
            if (profileData.getPhoneNumber() != null && !profileData.getPhoneNumber().trim().isEmpty()) {
                if (!profileData.getPhoneNumber().equals(currentCustomer.getPhoneNumber())) {
                    currentCustomer.setPhoneNumber(profileData.getPhoneNumber());
                    hasChanges = true;
                }
            }

            // Update name if provided
            if (profileData.getFullName() != null && !profileData.getFullName().trim().isEmpty()) {
                String newActualName = profileData.getFullName().trim();
                String currentActualName = CustomerOAuth2Utils.extractActualName(currentCustomer);

                if (!newActualName.equals(currentActualName)) {
                    // Update the fullName with OAuth2 encoding preserved
                    String providerId = CustomerOAuth2Utils.extractProviderId(currentCustomer);
                    String provider = CustomerOAuth2Utils.getOAuth2Provider(currentCustomer);
                    currentCustomer.setFullName(
                            CustomerOAuth2Utils.generateOAuth2FullName(newActualName, provider, providerId));
                    hasChanges = true;
                }
            }

            // Save updated customer if there are changes
            if (hasChanges) {
                customerService.save(currentCustomer);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Hoàn tất thông tin tài khoản thành công!");
            return "redirect:/";

        } catch (Exception e) {
            System.err.println("Error completing OAuth2 profile: " + e.getMessage());
            e.printStackTrace();

            String provider = CustomerOAuth2Utils.getOAuth2Provider(currentCustomer);
            model.addAttribute("customer", profileData);
            model.addAttribute("provider", provider);
            model.addAttribute("providerDisplayName", getProviderDisplayName(provider));
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật thông tin. Vui lòng thử lại.");
            return "auth/complete-profile";
        }
    }

    /**
     * Validate OAuth2 profile completion data
     * 
     * @param customer the customer data to validate
     * @param result   the binding result for validation errors
     */
    private void validateOAuth2ProfileCompletion(Customer customer, BindingResult result) {
        // Phone number is required for profile completion
        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            result.rejectValue("phoneNumber", "PHONE_REQUIRED", "Số điện thoại là bắt buộc");
        } else if (!PHONE_PATTERN.matcher(customer.getPhoneNumber()).matches()) {
            result.rejectValue("phoneNumber", "INVALID_PHONE_FORMAT", "Số điện thoại không đúng định dạng");
        }

        // Full name is optional but if provided should be valid
        if (customer.getFullName() != null && !customer.getFullName().trim().isEmpty()) {
            if (customer.getFullName().matches(".*\\d.*")) {
                result.rejectValue("fullName", "INVALID_FULLNAME", "Họ tên không được chứa số");
            }
        }
    }

    /**
     * Get display name for OAuth2 provider
     * 
     * @param provider the provider name
     * @return the display name
     */
    private String getProviderDisplayName(String provider) {
        return switch (provider != null ? provider.toLowerCase() : "") {
            case "google" -> "Google";
            default -> "Mạng xã hội";
        };
    }
}