package project.demo.util;

import project.demo.model.Customer;

/**
 * Utility class for OAuth2 customer management
 * Provides methods to detect and manage OAuth2 users using existing Customer
 * entity fields
 */
public class CustomerOAuth2Utils {

    /**
     * Determines if a customer is an OAuth2 user
     * OAuth2 users have the special password placeholder
     * 
     * @param customer the customer to check
     * @return true if the customer is an OAuth2 user
     */
    public static boolean isOAuth2User(Customer customer) {
        return customer != null &&
                customer.getPassword() != null &&
                customer.getPassword().equals(OAuth2CustomerConstants.OAUTH2_PASSWORD_PLACEHOLDER);
    }

    /**
     * Gets the OAuth2 provider for a customer
     * Extracts provider from fullName field format:
     * {actualName}[{provider}_{providerId}]
     * 
     * @param customer the customer to check
     * @return the OAuth2 provider name (google) or null if not OAuth2 user
     */
    public static String getOAuth2Provider(Customer customer) {
        if (customer == null || customer.getFullName() == null) {
            return null;
        }

        String fullName = customer.getFullName();
        if (fullName.contains("[google_")) {
            return OAuth2CustomerConstants.GOOGLE_PROVIDER;
        }

        return null;
    }

    /**
     * Gets the OAuth2 provider ID for a customer
     * Extracts provider ID from fullName field format:
     * {actualName}[{provider}_{providerId}]
     * 
     * @param customer the customer to check
     * @return the OAuth2 provider ID or null if not OAuth2 user
     */
    public static String getOAuth2ProviderId(Customer customer) {
        String provider = getOAuth2Provider(customer);
        if (provider != null && customer.getFullName() != null) {
            String fullName = customer.getFullName();
            String providerMarker = "[" + provider + "_";
            int start = fullName.indexOf(providerMarker);
            if (start != -1) {
                start += providerMarker.length();
                int end = fullName.indexOf("]", start);
                if (end != -1) {
                    return fullName.substring(start, end);
                }
            }
        }
        return null;
    }

    /**
     * Determines if a customer can use local (email/password) login
     * OAuth2-only users cannot use local login unless they set a password
     * 
     * @param customer the customer to check
     * @return true if the customer can use local login
     */
    public static boolean canUseLocalLogin(Customer customer) {
        return customer != null &&
                (!isOAuth2User(customer) || hasLocalPassword(customer));
    }

    /**
     * Checks if an OAuth2 user has set up a local password
     * This allows hybrid authentication (both OAuth2 and local)
     * 
     * @param customer the customer to check
     * @return true if OAuth2 user has local password set
     */
    public static boolean hasLocalPassword(Customer customer) {
        return customer != null &&
                customer.getPassword() != null &&
                !customer.getPassword().equals(OAuth2CustomerConstants.OAUTH2_PASSWORD_PLACEHOLDER);
    }

    /**
     * Generates fullName with OAuth2 info embedded
     * Format: {actualName}[{provider}_{providerId}]
     * 
     * @param actualName the user's actual name
     * @param provider   the OAuth2 provider (google)
     * @param providerId the provider's user ID
     * @return formatted fullName with OAuth2 info
     */
    public static String generateOAuth2FullName(String actualName, String provider, String providerId) {
        return actualName + "[" + provider + "_" + providerId + "]";
    }

    /**
     * Extracts actual name from OAuth2 fullName
     * Format: {actualName}[{provider}_{providerId}]
     * 
     * @param customer the customer to check
     * @return just the actual name part
     */
    public static String extractActualName(Customer customer) {
        String fullName = customer.getFullName();
        if (fullName != null && fullName.contains("[")) {
            return fullName.substring(0, fullName.indexOf("["));
        }
        return fullName;
    }

    /**
     * Checks if a customer is linked to a specific OAuth2 provider
     * 
     * @param customer the customer to check
     * @param provider the provider to check for
     * @return true if customer is linked to the provider
     */
    public static boolean isLinkedToProvider(Customer customer, String provider) {
        String customerProvider = getOAuth2Provider(customer);
        return customerProvider != null && customerProvider.equals(provider);
    }

    /**
     * Determines if a customer can link to an OAuth2 provider
     * Local users can always link, OAuth2 users can only link if different provider
     * 
     * @param customer the customer to check
     * @param provider the provider to link to
     * @return true if linking is possible
     */
    public static boolean canLinkToProvider(Customer customer, String provider) {
        if (customer == null || provider == null) {
            return false;
        }

        // Local users can always link
        if (!isOAuth2User(customer)) {
            return true;
        }

        // OAuth2 users can link if it's a different provider
        String currentProvider = getOAuth2Provider(customer);
        return !provider.equals(currentProvider);
    }

    /**
     * Extracts first name from full name
     * Used when creating OAuth2 users from provider data
     * 
     * @param fullName the full name from OAuth2 provider
     * @return the first name
     */
    public static String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }

        String[] nameParts = fullName.trim().split("\\s+");
        return nameParts[0];
    }

    /**
     * Extracts last name from full name
     * Used when creating OAuth2 users from provider data
     * 
     * @param fullName the full name from OAuth2 provider
     * @return the last name
     */
    public static String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }

        String[] nameParts = fullName.trim().split("\\s+");
        if (nameParts.length > 1) {
            return String.join(" ", java.util.Arrays.copyOfRange(nameParts, 1, nameParts.length));
        }
        return "";
    }

    /**
     * Extracts provider information from fullName
     * 
     * @param customer the customer to check
     * @return the provider information or null if not found
     */
    public static String extractProviderInfo(Customer customer) {
        String fullName = customer.getFullName();
        if (fullName != null && fullName.contains("[") && fullName.contains("]")) {
            return fullName.substring(fullName.indexOf("[") + 1, fullName.indexOf("]"));
        }
        return null;
    }

    /**
     * Encodes OAuth2 fullName
     * 
     * @param actualName the user's actual name
     * @param providerId the provider's user ID
     * @return the encoded OAuth2 fullName
     */
    public static String encodeOAuth2FullName(String actualName, String providerId) {
        if (actualName == null || actualName.trim().isEmpty()) {
            actualName = "User";
        }
        return actualName + "[" + providerId + "]";
    }

    /**
     * Decodes OAuth2 fullName
     * 
     * @param fullName the encoded OAuth2 fullName
     * @return the actual name
     */
    public static String decodeActualName(String fullName) {
        if (fullName == null || !fullName.contains("[")) {
            return fullName;
        }
        return fullName.substring(0, fullName.indexOf("["));
    }

    /**
     * Extracts provider ID from Customer's fullName
     * 
     * @param customer the customer to check
     * @return the provider ID or null if not found
     */
    public static String extractProviderId(Customer customer) {
        return getOAuth2ProviderId(customer);
    }

    /**
     * Extracts actual name from fullName string
     * 
     * @param fullName the fullName with OAuth2 info
     * @return just the actual name part
     */
    public static String extractActualName(String fullName) {
        if (fullName != null && fullName.contains("[")) {
            return fullName.substring(0, fullName.indexOf("["));
        }
        return fullName;
    }

    private CustomerOAuth2Utils() {
        // Utility class - prevent instantiation
    }
}