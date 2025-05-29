package project.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Custom Error Controller để xử lý các trang lỗi
 */
@Controller
public class CustomErrorController implements ErrorController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);
    
    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        // Debug logging
        String acceptHeader = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        String userAgent = request.getHeader("User-Agent");
        
        logger.error("Error occurred - Status: {}, URI: {}, Message: {}", status, requestUri, errorMessage);
        logger.debug("Accept Header: {}", acceptHeader);
        logger.debug("X-Requested-With: {}", requestedWith);
        logger.debug("User-Agent: {}", userAgent);
        
        // Check if this is an AJAX/API request
        boolean isApiRequest = (requestUri != null && requestUri.startsWith("/api/"));
        
        // Temporarily force browser handling for all other requests
        // boolean isApiRequest = (acceptHeader != null && acceptHeader.contains("application/json")) ||
        //                       (requestedWith != null && "XMLHttpRequest".equals(requestedWith)) ||
        //                       (requestUri != null && requestUri.startsWith("/api/"));
        
        logger.debug("Is API Request: {}", isApiRequest);
        
        if (isApiRequest) {
            logger.debug("Handling as API request");
            return handleApiError(status, requestUri, errorMessage);
        } else {
            logger.debug("Handling as Browser request");
            return handleBrowserError(status, requestUri, errorMessage, model);
        }
    }
    
    private ResponseEntity<Map<String, Object>> handleApiError(Object status, String requestUri, Object errorMessage) {
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("timestamp", LocalDateTime.now());
        errorAttributes.put("path", requestUri);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            errorAttributes.put("status", statusCode);
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                errorAttributes.put("error", "Not Found");
                errorAttributes.put("message", "The requested endpoint does not exist");
                errorAttributes.put("errorCode", "RESOURCE_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorAttributes);
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                errorAttributes.put("error", "Internal Server Error");
                errorAttributes.put("message", "An error occurred");
                errorAttributes.put("errorCode", "INTERNAL_SERVER_ERROR");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorAttributes);
            }
        }
        
        // Default error
        errorAttributes.put("status", 500);
        errorAttributes.put("error", "Unknown Error");
        errorAttributes.put("message", "An unexpected error occurred");
        errorAttributes.put("errorCode", "INTERNAL_SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorAttributes);
    }
    
    private String handleBrowserError(Object status, String requestUri, Object errorMessage, Model model) {
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                logger.warn("404 error for URI: {}", requestUri);
                model.addAttribute("requestUrl", requestUri);
                model.addAttribute("timestamp", LocalDateTime.now());
                return "error/404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                logger.error("500 error for URI: {}", requestUri);
                model.addAttribute("status", statusCode);
                model.addAttribute("error", "Internal Server Error");
                model.addAttribute("message", "Đã xảy ra lỗi server nội bộ");
                return "error/generic-error";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                logger.warn("403 error for URI: {}", requestUri);
                model.addAttribute("status", statusCode);
                model.addAttribute("error", "Forbidden");
                model.addAttribute("message", "Bạn không có quyền truy cập trang này");
                return "error/generic-error";
            }
        }
        
        // Default error handling
        model.addAttribute("status", status != null ? status : 500);
        model.addAttribute("error", "Unknown Error");
        model.addAttribute("message", "Đã xảy ra lỗi không xác định");
        return "error/generic-error";
    }
} 