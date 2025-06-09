package project.demo.security.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import project.demo.model.Customer;
import project.demo.util.CustomerOAuth2Utils;

import java.util.Collection;
import java.util.Map;

/**
 * Custom OAuth2User implementation that wraps OAuth2User with Customer entity
 * Provides access to both OAuth2 user data and application's Customer entity
 */
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oauth2User;
    private final Customer customer;

    /**
     * Constructor with OAuth2User and Customer
     * 
     * @param oauth2User the OAuth2User from Spring Security
     * @param customer   the application's Customer entity
     */
    public CustomOAuth2User(OAuth2User oauth2User, Customer customer) {
        this.oauth2User = oauth2User;
        this.customer = customer;
    }

    /**
     * Gets the authorities granted to the user
     * Delegates to the wrapped OAuth2User
     * 
     * @return the user's authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    /**
     * Gets the OAuth2 user attributes
     * Delegates to the wrapped OAuth2User
     * 
     * @return the OAuth2 user attributes
     */
    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    /**
     * Gets the name attribute
     * Delegates to the wrapped OAuth2User
     * 
     * @return the user's name
     */
    @Override
    public String getName() {
        return oauth2User.getName();
    }

    /**
     * Gets the application's Customer entity
     * 
     * @return the Customer entity
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Gets the customer's email
     * Convenience method to get email from Customer entity
     * 
     * @return the customer's email
     */
    public String getEmail() {
        return customer != null ? customer.getEmail() : null;
    }

    /**
     * Gets the customer's ID
     * Convenience method to get ID from Customer entity
     * 
     * @return the customer's ID
     */
    public Long getCustomerId() {
        return this.customer.getCustomerId().longValue();
    }

    /**
     * Gets the customer's actual name (without OAuth2 provider info)
     * Extracts the actual name from the fullName field
     * 
     * @return the customer's actual name
     */
    public String getFullName() {
        if (customer == null || customer.getFullName() == null) {
            return null;
        }

        // Extract actual name from fullName field (removing OAuth2 provider info)
        return CustomerOAuth2Utils.extractActualName(customer);
    }

    /**
     * Checks if the customer account is active
     * 
     * @return true if customer is active
     */
    public boolean isActive() {
        return customer != null && customer.getStatus() != null;
        // Note: Assuming status check - adjust based on your CustomerStatus enum
    }

    /**
     * Gets a specific OAuth2 attribute
     * 
     * @param key the attribute key
     * @return the attribute value
     */
    public Object getAttribute(String key) {
        return oauth2User.getAttributes().get(key);
    }

    /**
     * Gets the customer's display name
     * 
     * @return the customer's display name
     */
    public String getDisplayName() {
        String actualName = CustomerOAuth2Utils.extractActualName(this.customer);
        String lastName = ""; // We don't store separate last names

        if (actualName != null && !actualName.trim().isEmpty()) {
            return actualName;
        }

        return this.customer.getEmail(); // Fallback to email
    }

    /**
     * String representation of the user
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "CustomOAuth2User{" +
                "customer=" + (customer != null ? customer.getEmail() : null) +
                ", name=" + getName() +
                '}';
    }
}