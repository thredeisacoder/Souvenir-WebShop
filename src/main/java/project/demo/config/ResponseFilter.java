package project.demo.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Bộ lọc để xử lý và ngăn chặn lỗi ERR_INCOMPLETE_CHUNKED_ENCODING
 * bằng cách đảm bảo mọi phản hồi được đóng đúng cách
 */
@Component
public class ResponseFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponseFilter.class);
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Khởi tạo Response Filter");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Ghi log URI cho mục đích debug
        String requestURI = httpRequest.getRequestURI();
        logger.debug("Đang xử lý request: {}", requestURI);
        
        try {
            // Đặt header để tránh buffering
            httpResponse.setHeader("X-Accel-Buffering", "no");
            
            // Tiếp tục chuỗi lọc
            chain.doFilter(request, response);
        } catch (Exception e) {
            // Ghi log lỗi
            logger.error("Lỗi khi xử lý request {}: {}", requestURI, e.getMessage(), e);
            
            // Kiểm tra xem response đã được gửi chưa
            if (!httpResponse.isCommitted()) {
                // Nếu chưa gửi, đặt lại response và trả về lỗi
                httpResponse.resetBuffer();
                httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpResponse.setContentType("text/html;charset=UTF-8");
                httpResponse.getWriter().write("<html><body><h1>Đã xảy ra lỗi</h1><p>Vui lòng thử lại sau.</p></body></html>");
                httpResponse.flushBuffer();
            }
        } finally {
            // Đảm bảo response được đóng đúng cách
            if (response.getBufferSize() > 0 && !response.isCommitted()) {
                response.flushBuffer();
            }
            logger.debug("Đã xử lý xong request: {}", requestURI);
        }
    }
    
    @Override
    public void destroy() {
        logger.info("Hủy Response Filter");
    }
}