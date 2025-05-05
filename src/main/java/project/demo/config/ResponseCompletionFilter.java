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
import jakarta.servlet.http.HttpServletResponse;

/**
 * Bộ lọc để đảm bảo tất cả phản hồi HTTP được đóng đúng cách,
 * giúp ngăn chặn lỗi ERR_INCOMPLETE_CHUNKED_ENCODING
 */
@Component
public class ResponseCompletionFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponseCompletionFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Khởi tạo ResponseCompletionFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Thiết lập buffer size phù hợp để tránh chunked encoding nếu có thể
            httpResponse.setBufferSize(8192);
            
            // Tiếp tục chuỗi filter
            chain.doFilter(request, response);
            
            // Đảm bảo response đã được flush
            if (!httpResponse.isCommitted()) {
                httpResponse.flushBuffer();
            }
        } catch (Exception e) {
            logger.error("Lỗi xảy ra trong ResponseCompletionFilter", e);
            throw e;
        } finally {
            // Không cần đóng response ở đây vì container sẽ tự làm
        }
    }

    @Override
    public void destroy() {
        logger.info("Hủy ResponseCompletionFilter");
    }
}