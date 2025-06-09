package project.demo.util;

/**
 * Constants for OAuth2 customer management
 * These constants help identify OAuth2 users without modifying the database
 * schema
 */
public class OAuth2CustomerConstants {

    /**
     * Special password placeholder for OAuth2-only users
     * Used in the password field to indicate this user authenticates via OAuth2
     */
    public static final String OAUTH2_PASSWORD_PLACEHOLDER = "OAUTH2_USER";

    /**
     * Username prefix for Google OAuth2 users
     * Format: google_{providerId}
     */
    public static final String GOOGLE_USERNAME_PREFIX = "google_";

    /**
     * Provider name constants
     */
    public static final String GOOGLE_PROVIDER = "google";

    /**
     * OAuth2 scope constants
     */
    public static final String GOOGLE_SCOPE_PROFILE = "profile";
    public static final String GOOGLE_SCOPE_EMAIL = "email";

    private OAuth2CustomerConstants() {
        // Utility class - prevent instantiation
    }
}