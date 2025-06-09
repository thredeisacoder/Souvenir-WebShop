package project.demo.security.oauth2;

import java.util.Map;

/**
 * Google OAuth2 user information implementation
 * Extracts user data from Google OAuth2 response
 */
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    /**
     * Constructor with Google OAuth2 attributes
     * 
     * @param attributes the user attributes from Google OAuth2
     */
    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    /**
     * Gets the Google user ID
     * Google returns the user ID in the "id" field
     * 
     * @return the Google user ID
     */
    @Override
    public String getId() {
        return getAttributeAsString("id");
    }

    /**
     * Gets the user's full name from Google
     * Google returns the name in the "name" field
     * 
     * @return the user's full name
     */
    @Override
    public String getName() {
        return getAttributeAsString("name");
    }

    /**
     * Gets the user's email from Google
     * Google returns the email in the "email" field
     * 
     * @return the user's email address
     */
    @Override
    public String getEmail() {
        return getAttributeAsString("email");
    }

    /**
     * Gets the user's profile picture URL from Google
     * Google returns the picture URL in the "picture" field
     * 
     * @return the profile picture URL
     */
    @Override
    public String getImageUrl() {
        return getAttributeAsString("picture");
    }

    /**
     * Gets the user's first name from Google
     * Google returns the first name in the "given_name" field
     * 
     * @return the user's first name
     */
    public String getFirstName() {
        return getAttributeAsString("given_name");
    }

    /**
     * Gets the user's last name from Google
     * Google returns the last name in the "family_name" field
     * 
     * @return the user's last name
     */
    public String getLastName() {
        return getAttributeAsString("family_name");
    }

    /**
     * Gets the user's locale from Google
     * Google returns the locale in the "locale" field
     * 
     * @return the user's locale
     */
    public String getLocale() {
        return getAttributeAsString("locale");
    }

    /**
     * Checks if the email is verified by Google
     * Google returns email verification status in "email_verified" field
     * 
     * @return true if email is verified
     */
    public Boolean isEmailVerified() {
        Object verified = getAttribute("email_verified");
        if (verified instanceof Boolean) {
            return (Boolean) verified;
        } else if (verified instanceof String) {
            return Boolean.parseBoolean((String) verified);
        }
        return false;
    }
}