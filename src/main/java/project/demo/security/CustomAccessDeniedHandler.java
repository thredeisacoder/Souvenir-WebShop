package project.demo.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Custom handler for Access Denied (403) errors
 * Redirects the user to the login page with an appropriate message
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        logger.warn("Access denied to URL: {}", request.getRequestURI());
        
        // Lưu URL hiện tại để có thể quay lại sau khi đăng nhập
        String requestedUrl = request.getRequestURI();
        if (request.getQueryString() != null) {
            requestedUrl += "?" + request.getQueryString();
        }
        
        // Lấy thông tin phiên hiện tại
        HttpSession session = request.getSession(false);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Kiểm tra trạng thái đăng nhập
        boolean isLoggedIn = false;
        if (session != null) {
            Boolean sessionLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
            isLoggedIn = sessionLoggedIn != null && sessionLoggedIn;
            logger.debug("Session isLoggedIn status: {}", isLoggedIn);
        }
        
        boolean securityLoggedIn = auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser");
        logger.debug("Security context authentication status: {}", securityLoggedIn);
        
        if (!isLoggedIn || !securityLoggedIn) {
            // Người dùng không đăng nhập - làm mới phiên để xóa mọi thông tin phiên lỗi
            if (session != null) {
                logger.debug("Invalidating problematic session: {}", session.getId());
                session.invalidate();
            }
            
            // Tạo phiên mới và thiết lập thông báo đăng nhập
            session = request.getSession(true);
            session.setAttribute("loginMessage", "Vui lòng đăng nhập để tiếp tục");
            session.setAttribute("requestedUrl", requestedUrl);
            
            // Chuyển hướng đến trang đăng nhập
            logger.debug("Redirecting to login page");
            response.sendRedirect(request.getContextPath() + "/auth/login?denied=true&redirect=" 
                    + requestedUrl.replaceAll("^/+", ""));
        } else {
            // Người dùng đã đăng nhập nhưng vẫn bị từ chối - có thể do phiên không đồng bộ
            logger.warn("User already logged in but access denied: {}", auth.getName());
            
            // Làm mới phiên đăng nhập
            session.setAttribute("errorMessage", "Phiên làm việc của bạn có vấn đề. Vui lòng đăng nhập lại để tiếp tục.");
            
            // Hủy xác thực hiện tại
            SecurityContextHolder.clearContext();
            session.invalidate();
            
            // Chuyển hướng đến trang đăng nhập
            response.sendRedirect(request.getContextPath() + "/auth/login?expired=true&redirect=" 
                    + requestedUrl.replaceAll("^/+", ""));
        }
    }
}