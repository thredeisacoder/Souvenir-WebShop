package project.demo.security.oauth2;

import java.util.Map;

/**
 * Abstract class for OAuth2 user information
 * Provides a common interface for extracting user data from different OAuth2
 * providers
 */
public abstract class OAuth2UserInfo {

    protected Map<String, Object> attributes;

    /**
     * Constructor with OAuth2 user attributes
     * 
     * @param attributes the user attributes from OAuth2 provider
     */
    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Gets the user ID from OAuth2 provider
     * 
     * @return the provider-specific user ID
     */
    public abstract String getId();

    /**
     * Gets the user's full name from OAuth2 provider
     * 
     * @return the user's full name
     */
    public abstract String getName();

    /**
     * Gets the user's email from OAuth2 provider
     * 
     * @return the user's email address
     */
    public abstract String getEmail();

    /**
     * Gets the user's profile image URL from OAuth2 provider
     * 
     * @return the profile image URL or null if not available
     */
    public abstract String getImageUrl();

    /**
     * Gets the raw attributes from OAuth2 provider
     * 
     * @return the complete attributes map
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Gets a specific attribute value
     * 
     * @param key the attribute key
     * @return the attribute value or null if not found
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Gets a specific attribute value as String
     * 
     * @param key the attribute key
     * @return the attribute value as String or null if not found
     */
    public String getAttributeAsString(String key) {
        Object value = getAttribute(key);
        return value != null ? value.toString() : null;
    }
}