package project.demo.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

/**
 * Utility class for handling validation error messages.
 * Loads error messages from a JSON file and provides methods to access them.
 */
@Component
public class ValidationErrorMessages {
    
    private final Map<String, String> errorMessages = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Loads error messages from the JSON file when the application starts.
     */
    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("error-messages.json");
            InputStream inputStream = resource.getInputStream();
            
            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode errorsNode = rootNode.get("errors");
            
            if (errorsNode != null && errorsNode.isArray()) {
                for (JsonNode errorNode : errorsNode) {
                    String messageCode = errorNode.get("messageCode").asText();
                    String description = errorNode.get("description").asText();
                    errorMessages.put(messageCode, description);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading validation error messages", e);
        }
    }
    
    /**
     * Gets the error message for the specified error code.
     *
     * @param errorCode the error code
     * @return the error message, or the error code if not found
     */
    public String getMessage(String errorCode) {
        return errorMessages.getOrDefault(errorCode, errorCode);
    }
    
    /**
     * Checks if an error message exists for the specified error code.
     *
     * @param errorCode the error code
     * @return true if an error message exists, false otherwise
     */
    public boolean hasMessage(String errorCode) {
        return errorMessages.containsKey(errorCode);
    }
    
    /**
     * Gets all error messages as a map.
     *
     * @return a map of error codes to error messages
     */
    public Map<String, String> getAllMessages() {
        return new HashMap<>(errorMessages);
    }
} 