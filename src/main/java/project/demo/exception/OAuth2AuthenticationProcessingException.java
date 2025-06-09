package project.demo.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when OAuth2 authentication processing fails
 * Extends Spring Security's AuthenticationException for proper exception
 * handling
 */
public class OAuth2AuthenticationProcessingException extends AuthenticationException {

    /**
     * Constructor with error message
     * 
     * @param message the error message
     */
    public OAuth2AuthenticationProcessingException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause
     * 
     * @param message the error message
     * @param cause   the underlying cause
     */
    public OAuth2AuthenticationProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}