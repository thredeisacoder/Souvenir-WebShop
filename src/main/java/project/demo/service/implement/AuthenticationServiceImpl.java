package project.demo.service.implement;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import project.demo.exception.CustomerException;
import project.demo.model.Customer;
import project.demo.service.AuthenticationService;
import project.demo.service.ICustomerService;

/**
 * Implementation of the AuthenticationService interface
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final ICustomerService customerService;
    
    // Secret key for remember-me token generation
    private static final String REMEMBER_ME_KEY = "souvenirShopRememberMeKey";

    public AuthenticationServiceImpl(ICustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public Customer login(String email, String password, boolean rememberMe) {
        System.out.println("DEBUG-AUTH-FLOW - Bắt đầu quá trình đăng nhập cho email: " + email + 
                           (rememberMe ? " với tùy chọn ghi nhớ đăng nhập" : ""));
        
        try {
            // Authenticate using customer service (now throws exceptions instead of returning boolean)
            customerService.authenticate(email, password);
            
            System.out.println("DEBUG-AUTH-FLOW - Xác thực thành công, đang lấy thông tin khách hàng...");
            
            // Get customer by email
            Optional<Customer> customerOpt = customerService.findByEmail(email);
            if (customerOpt.isEmpty()) {
                System.out.println("DEBUG-AUTH-FLOW - Không tìm thấy khách hàng với email: " + email);
                throw new CustomerException("Không tìm thấy email trong hệ thống", "EMAIL_NOT_FOUND");
            }
            
            Customer customer = customerOpt.get();
            System.out.println("DEBUG-AUTH-FLOW - Đã lấy thông tin khách hàng: ID=" + customer.getCustomerId());
            
            // Check if customer is active
            if (!project.demo.enums.CustomerStatus.ACTIVE.getValue().equals(customer.getStatus())) {
                System.out.println("DEBUG-AUTH-FLOW - Tài khoản không hoạt động: " + customer.getStatus());
                throw new CustomerException("Tài khoản của bạn đã bị vô hiệu hóa", "INACTIVE_CUSTOMER");
            }

            try {
                // Get current request and response
                ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attr != null) {
                    HttpServletRequest request = attr.getRequest();
                    HttpServletResponse response = attr.getResponse();
                    HttpSession session = request.getSession(true); // Ensure session exists
                    
                    System.out.println("DEBUG-AUTH-FLOW - Thiết lập session...");
                    
                    // Set session attributes
                    session.setAttribute("customer", customer);
                    session.setAttribute("customerId", customer.getCustomerId());
                    session.setAttribute("customerName", customer.getFullName());
                    session.setAttribute("isLoggedIn", true);
                    session.setMaxInactiveInterval(30 * 60); // 30 minutes
                    
                    System.out.println("DEBUG-AUTH-FLOW - Session đã được thiết lập với ID: " + session.getId());
                    System.out.println("DEBUG-AUTH-FLOW - Session attributes: customer=" + session.getAttribute("customer") + 
                                     ", isLoggedIn=" + session.getAttribute("isLoggedIn"));

                    // Set up Spring Security context
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        customer.getEmail(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    
                    // Set details for the authentication token
                    auth.setDetails(customer);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("DEBUG-AUTH-FLOW - Đã thiết lập context bảo mật Spring Security");
                    
                    // Handle remember-me functionality if enabled
                    if (rememberMe && response != null) {
                        setupRememberMe(request, response, email);
                    }
                    
                    // Verify Spring Security Context
                    if (SecurityContextHolder.getContext().getAuthentication() != null &&
                        SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                        System.out.println("DEBUG-AUTH-FLOW - Xác nhận Spring Security Context đã được thiết lập thành công");
                    } else {
                        System.out.println("DEBUG-AUTH-FLOW - CẢNH BÁO: Spring Security Context không được thiết lập đúng");
                    }
                    
                    System.out.println("DEBUG-AUTH-FLOW - Quá trình đăng nhập hoàn tất thành công");
                    return customer;
                } else {
                    System.out.println("DEBUG-AUTH-FLOW - CẢNH BÁO: Không thể lấy RequestAttributes");
                    throw new CustomerException("Lỗi hệ thống: Không thể thiết lập phiên đăng nhập", "SESSION_ERROR");
                }
            } catch (Exception e) {
                System.out.println("DEBUG-AUTH-FLOW - LỖI trong quá trình xử lý session: " + e.getMessage());
                e.printStackTrace();
                throw new CustomerException("Lỗi xử lý phiên đăng nhập: " + e.getMessage(), "SESSION_ERROR");
            }
            
        } catch (CustomerException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("DEBUG-AUTH-FLOW - LỖI không xác định: " + e.getMessage());
            e.printStackTrace();
            throw new CustomerException("Lỗi không xác định khi đăng nhập: " + e.getMessage(), "UNKNOWN_ERROR");
        }
    }
    
    /**
     * Set up remember-me functionality for persistent logins
     */
    private void setupRememberMe(HttpServletRequest request, HttpServletResponse response, String username) {
        try {
            // Create authentication token for remember-me service
            UsernamePasswordAuthenticationToken rememberMeToken = new UsernamePasswordAuthenticationToken(
                username, 
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            
            // Use Spring's TokenBasedRememberMeServices to handle remember-me cookie
            RememberMeServices rememberMeServices = new TokenBasedRememberMeServices(REMEMBER_ME_KEY, 
                                                        (username1) -> customerService.findByEmail(username1)
                                                            .map(c -> new org.springframework.security.core.userdetails.User(
                                                                c.getEmail(), 
                                                                c.getPassword(), 
                                                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                                                            ))
                                                            .orElse(null));
            
            // Process the remember-me login
            rememberMeServices.loginSuccess(request, response, rememberMeToken);
            System.out.println("DEBUG-AUTH-FLOW - Đã thiết lập remember-me cookie để duy trì đăng nhập");
        } catch (Exception e) {
            System.out.println("DEBUG-AUTH-FLOW - Lỗi khi thiết lập remember-me: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void logout() {
        // Clear Spring Security context
        SecurityContextHolder.clearContext();

        try {
            // Get current session
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpSession session = attr.getRequest().getSession(false);
                if (session != null) {
                    session.invalidate();
                    System.out.println("DEBUG-AUTH-FLOW - Đã đăng xuất và hủy phiên làm việc");
                }
                
                // Clear remember-me cookie if present
                HttpServletResponse response = attr.getResponse();
                if (response != null) {
                    Cookie cookie = new Cookie("remember-me", null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    System.out.println("DEBUG-AUTH-FLOW - Đã xóa cookie remember-me");
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG-AUTH-FLOW - Lỗi khi đăng xuất: " + e.getMessage());
        }
    }

    @Override
    public boolean isAuthenticated() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr == null) {
                System.out.println("DEBUG-AUTH-FLOW - Không thể kiểm tra xác thực: Request attributes là null");
                return false;
            }
            
            HttpSession session = attr.getRequest().getSession(false);
            boolean sessionAuthenticated = session != null && 
                   session.getAttribute("isLoggedIn") != null && 
                   (Boolean) session.getAttribute("isLoggedIn");
            
            // Kiểm tra cả Spring Security context
            boolean securityAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null &&
                                         SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                                         !(SecurityContextHolder.getContext().getAuthentication() 
                                             instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
            
            System.out.println("DEBUG-AUTH-FLOW - Kiểm tra xác thực: " 
                + "Session: " + (sessionAuthenticated ? "Đã đăng nhập" : "Chưa đăng nhập")
                + ", Spring Security: " + (securityAuthenticated ? "Đã xác thực" : "Chưa xác thực"));
            
            // Thay đổi ở đây: Chỉ cần một trong hai điều kiện đúng thay vì cả hai
            return sessionAuthenticated || securityAuthenticated;
        } catch (Exception e) {
            System.err.println("DEBUG-AUTH-FLOW - Lỗi khi kiểm tra xác thực: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Customer getCurrentCustomer() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr == null) {
                System.out.println("DEBUG-AUTH-FLOW - Không thể lấy thông tin khách hàng: Request attributes là null");
                return null;
            }
            
            HttpSession session = attr.getRequest().getSession(false);
            if (session == null) {
                System.out.println("DEBUG-AUTH-FLOW - Không thể lấy thông tin khách hàng: Session là null");
                return null;
            }
            
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                System.out.println("DEBUG-AUTH-FLOW - Không tìm thấy thông tin khách hàng trong session");
            } else {
                System.out.println("DEBUG-AUTH-FLOW - Đã lấy thông tin khách hàng từ session: ID=" + customer.getCustomerId());
            }
            
            return customer;
        } catch (Exception e) {
            System.err.println("DEBUG-AUTH-FLOW - Lỗi khi lấy thông tin khách hàng: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
