package project.demo.security.oauth2;

import project.demo.exception.OAuth2AuthenticationProcessingException;
import project.demo.util.OAuth2CustomerConstants;

import java.util.Map;

/**
 * Factory class for creating OAuth2UserInfo instances
 * Creates appropriate OAuth2UserInfo implementation based on the provider
 */
public class OAuth2UserInfoFactory {

    /**
     * Creates OAuth2UserInfo instance based on provider
     * 
     * @param registrationId the OAuth2 provider registration ID
     * @param attributes     the user attributes from OAuth2 provider
     * @return the appropriate OAuth2UserInfo implementation
     * @throws OAuth2AuthenticationProcessingException if provider is not supported
     */
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId == null) {
            throw new OAuth2AuthenticationProcessingException("Registration ID cannot be null");
        }

        switch (registrationId.toLowerCase()) {
            case OAuth2CustomerConstants.GOOGLE_PROVIDER:
                return new GoogleOAuth2UserInfo(attributes);

            default:
                throw new OAuth2AuthenticationProcessingException(
                        "Login with " + registrationId + " is not supported. Supported providers: Google");
        }
    }

    /**
     * Checks if a provider is supported
     * 
     * @param registrationId the provider registration ID
     * @return true if provider is supported
     */
    public static boolean isProviderSupported(String registrationId) {
        if (registrationId == null) {
            return false;
        }

        String provider = registrationId.toLowerCase();
        return OAuth2CustomerConstants.GOOGLE_PROVIDER.equals(provider);
    }

    /**
     * Gets the list of supported providers
     * 
     * @return array of supported provider names
     */
    public static String[] getSupportedProviders() {
        return new String[] {
                OAuth2CustomerConstants.GOOGLE_PROVIDER
        };
    }

    private OAuth2UserInfoFactory() {
        // Utility class - prevent instantiation
    }
}