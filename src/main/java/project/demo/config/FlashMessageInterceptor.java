package project.demo.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import project.demo.util.ValidationErrorMessages;

/**
 * Interceptor to convert direct error/success messages to message codes
 * This allows us to standardize message handling across the application
 */
@Component
public class FlashMessageInterceptor implements HandlerInterceptor {
    
    @Autowired
    private ValidationErrorMessages validationErrorMessages;
    
    private static final Map<String, String> SUCCESS_MESSAGES_TO_CODES = new HashMap<>();
    private static final Map<String, String> ERROR_MESSAGES_TO_CODES = new HashMap<>();
    
    // Initialize common messages mappings
    static {
        // Success messages
        SUCCESS_MESSAGES_TO_CODES.put("Đặt hàng thành công!", "POST_SUCCESS");
        SUCCESS_MESSAGES_TO_CODES.put("Item added to cart", "CART_ITEM_ADDED");
        SUCCESS_MESSAGES_TO_CODES.put("Payment method added successfully", "PAYMENT_METHOD_ADDED");
        SUCCESS_MESSAGES_TO_CODES.put("Profile updated successfully", "PROFILE_UPDATED");
        SUCCESS_MESSAGES_TO_CODES.put("Password changed successfully", "PASSWORD_CHANGED");
        SUCCESS_MESSAGES_TO_CODES.put("Registration successful! Please log in.", "REGISTRATION_SUCCESS");
        SUCCESS_MESSAGES_TO_CODES.put("You have been logged out successfully.", "LOGOUT_SUCCESS");
        
        // Error messages
        ERROR_MESSAGES_TO_CODES.put("Không thể hoàn tất đơn hàng", "CONNECT_ERROR");
        ERROR_MESSAGES_TO_CODES.put("Lỗi khi tạo đơn hàng", "CONNECT_ERROR");
        ERROR_MESSAGES_TO_CODES.put("Lỗi khi xử lý phương thức thanh toán", "PAYMENT_ERROR");
        ERROR_MESSAGES_TO_CODES.put("Cannot add item to cart", "CART_ERROR");
        ERROR_MESSAGES_TO_CODES.put("Access denied", "ACCESS_DENIED");
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        
        if (modelAndView != null) {
            // Process direct messages in the model
            processModelMessages(modelAndView);
            
            // Process flash attributes from the session
            processFlashAttributes(request.getSession(false));
        }
    }
    
    /**
     * Process direct messages in the model and convert them to message codes
     */
    private void processModelMessages(ModelAndView modelAndView) {
        Object successMessage = modelAndView.getModel().get("successMessage");
        Object errorMessage = modelAndView.getModel().get("errorMessage");
        
        // Only process if we don't already have a messageCode
        if (!modelAndView.getModel().containsKey("messageCode")) {
            if (successMessage != null) {
                String message = successMessage.toString();
                String messageCode = findMessageCode(message, true);
                if (messageCode != null) {
                    modelAndView.addObject("messageCode", messageCode);
                }
            } else if (errorMessage != null) {
                String message = errorMessage.toString();
                String messageCode = findMessageCode(message, false);
                if (messageCode != null) {
                    modelAndView.addObject("messageCode", messageCode);
                }
            }
        }
    }
    
    /**
     * Process flash attributes from the session
     */
    private void processFlashAttributes(HttpSession session) {
        if (session == null) {
            return;
        }
        
        // Get the flash attributes map from the session
        @SuppressWarnings("unchecked")
        Map<String, Object> flashAttributes = 
                (Map<String, Object>) session.getAttribute("org.springframework.web.servlet.support.SessionFlashMapManager.FLASH_MAPS");
        
        if (flashAttributes != null && !flashAttributes.isEmpty()) {
            // Process the first flash map (most recent)
            @SuppressWarnings("unchecked")
            Map<String, Object> flashMap = ((java.util.List<Map<String, Object>>) flashAttributes).get(0);
            
            // Only process if we don't already have a messageCode
            if (!flashMap.containsKey("messageCode")) {
                Object successMessage = flashMap.get("successMessage");
                Object errorMessage = flashMap.get("errorMessage");
                
                if (successMessage != null) {
                    String message = successMessage.toString();
                    String messageCode = findMessageCode(message, true);
                    if (messageCode != null) {
                        flashMap.put("messageCode", messageCode);
                    }
                } else if (errorMessage != null) {
                    String message = errorMessage.toString();
                    String messageCode = findMessageCode(message, false);
                    if (messageCode != null) {
                        flashMap.put("messageCode", messageCode);
                    }
                }
            }
        }
    }
    
    /**
     * Find a message code for a given message
     */
    private String findMessageCode(String message, boolean isSuccess) {
        Map<String, String> messagesToCodes = isSuccess ? SUCCESS_MESSAGES_TO_CODES : ERROR_MESSAGES_TO_CODES;
        
        // Try exact match first
        String messageCode = messagesToCodes.get(message);
        if (messageCode != null) {
            return messageCode;
        }
        
        // If not exact match, check if the message starts with any of the known messages
        for (Map.Entry<String, String> entry : messagesToCodes.entrySet()) {
            if (message.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // If still not found, use a generic code
        return isSuccess ? "GENERIC_SUCCESS" : "GENERIC_ERROR";
    }
} 