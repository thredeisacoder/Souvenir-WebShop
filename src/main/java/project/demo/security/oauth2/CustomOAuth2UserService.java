package project.demo.security.oauth2;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import project.demo.enums.CustomerStatus;
import project.demo.exception.OAuth2AuthenticationProcessingException;
import project.demo.model.Customer;
import project.demo.service.ICustomerService;
import project.demo.util.CustomerOAuth2Utils;
import project.demo.util.OAuth2CustomerConstants;

/**
 * Custom OAuth2 User Service that handles OAuth2 user authentication
 * Integrates OAuth2 users with existing Customer entity without database schema
 * changes
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    @Lazy
    private ICustomerService customerService;

    /**
     * Loads OAuth2 user and processes Customer entity integration
     * 
     * @param userRequest the OAuth2 user request
     * @return CustomOAuth2User with linked Customer entity
     * @throws OAuth2AuthenticationException if authentication fails
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            logger.debug("Loading OAuth2 user for provider: {}",
                    userRequest.getClientRegistration().getRegistrationId());

            // Load OAuth2 user from provider
            OAuth2User oauth2User = super.loadUser(userRequest);

            // Extract provider information
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,
                    oauth2User.getAttributes());

            // Process OAuth2 user and link with Customer entity
            Customer customer = processOAuth2User(oauth2UserInfo, registrationId);

            logger.info("OAuth2 user successfully processed: {} ({})", customer.getEmail(), registrationId);

            return new CustomOAuth2User(oauth2User, customer);

        } catch (OAuth2AuthenticationProcessingException ex) {
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_processing_error", ex.getMessage(), null),
                    ex);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_error", ex.getMessage(), null), ex);
        }
    }

    /**
     * Processes OAuth2 user and creates/updates Customer entity
     * 
     * @param oauth2UserInfo the OAuth2 user information
     * @param provider       the OAuth2 provider name
     * @return the Customer entity
     */
    private Customer processOAuth2User(OAuth2UserInfo oauth2UserInfo, String provider) {
        String email = oauth2UserInfo.getEmail();

        if (email == null || email.trim().isEmpty()) {
            throw new OAuth2AuthenticationProcessingException(
                    "Email not found from OAuth2 provider: " + provider);
        }

        logger.debug("Processing OAuth2 user: {} from provider: {}", email, provider);

        // Check if customer exists by email
        Optional<Customer> existingCustomerOpt = customerService.findByEmail(email);

        if (existingCustomerOpt.isPresent()) {
            Customer customer = existingCustomerOpt.get();
            logger.debug("Found existing customer: {}", email);

            // Check if this is OAuth2 user trying to login
            if (CustomerOAuth2Utils.isOAuth2User(customer)) {
                // OAuth2 user - update and return
                return updateOAuth2Customer(customer, oauth2UserInfo, provider);
            } else {
                // Local user - link with OAuth2
                return linkLocalAccountWithOAuth2(customer, oauth2UserInfo, provider);
            }
        } else {
            // New user - create from OAuth2
            logger.debug("Creating new OAuth2 customer: {}", email);
            return createOAuth2Customer(oauth2UserInfo, provider);
        }
    }

    /**
     * Creates new Customer from OAuth2 user information
     * Uses existing Customer entity fields without schema changes
     * 
     * @param oauth2UserInfo the OAuth2 user information
     * @param provider       the OAuth2 provider name
     * @return new Customer entity
     */
    private Customer createOAuth2Customer(OAuth2UserInfo oauth2UserInfo, String provider) {
        Customer customer = new Customer();

        // Basic information
        customer.setEmail(oauth2UserInfo.getEmail());

        // Use fullName field to store OAuth2 info: actualName[provider_id]
        String actualName = oauth2UserInfo.getName() != null ? oauth2UserInfo.getName() : "OAuth2 User";
        String fullNameWithProvider = CustomerOAuth2Utils.generateOAuth2FullName(
                actualName, provider, oauth2UserInfo.getId());
        customer.setFullName(fullNameWithProvider);

        // Use phoneNumber field for a default value (will be updated in profile
        // completion)
        customer.setPhoneNumber("0000000000"); // Placeholder - 10 digits format

        // OAuth2 specific fields using existing schema
        customer.setPassword(OAuth2CustomerConstants.OAUTH2_PASSWORD_PLACEHOLDER);

        // Set default values
        customer.setStatus(CustomerStatus.ACTIVE.getValue());
        customer.setJoinDate(LocalDate.now());

        try {
            Customer savedCustomer = customerService.register(customer);
            logger.info("Created new OAuth2 customer: {} (provider: {})", savedCustomer.getEmail(), provider);
            return savedCustomer;
        } catch (Exception e) {
            logger.error("Failed to create OAuth2 customer: {}", e.getMessage(), e);
            // Try alternative method - direct save instead of register
            try {
                logger.info("Attempting direct save for OAuth2 customer: {}", customer.getEmail());
                // Ensure all required fields are set before direct save
                if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
                    customer.setEmail(oauth2UserInfo.getEmail());
                }
                if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
                    String fallbackName = oauth2UserInfo.getName() != null ? oauth2UserInfo.getName() : "OAuth2 User";
                    customer.setFullName(
                            CustomerOAuth2Utils.generateOAuth2FullName(fallbackName, provider, oauth2UserInfo.getId()));
                }
                if (customer.getStatus() == null || customer.getStatus().trim().isEmpty()) {
                    customer.setStatus(CustomerStatus.ACTIVE.getValue());
                }
                logger.debug("Customer data before save - Email: {}, FullName: {}, Phone: {}, Status: {}",
                        customer.getEmail(), customer.getFullName(), customer.getPhoneNumber(), customer.getStatus());

                Customer savedCustomer = customerService.save(customer);
                logger.info("Direct save successful for OAuth2 customer: {}", savedCustomer.getEmail());
                return savedCustomer;
            } catch (Exception e2) {
                logger.error("Direct save also failed for OAuth2 customer: {}", e2.getMessage(), e2);
                throw new OAuth2AuthenticationProcessingException(
                        "Failed to create customer account: " + e.getMessage());
            }
        }
    }

    /**
     * Updates existing OAuth2 customer with latest information
     * 
     * @param customer       the existing Customer entity
     * @param oauth2UserInfo the OAuth2 user information
     * @param provider       the OAuth2 provider name
     * @return updated Customer entity
     */
    private Customer updateOAuth2Customer(Customer customer, OAuth2UserInfo oauth2UserInfo, String provider) {
        boolean updated = false;

        // Verify provider matches
        String existingProvider = CustomerOAuth2Utils.getOAuth2Provider(customer);
        if (!provider.equals(existingProvider)) {
            throw new OAuth2AuthenticationProcessingException(
                    "OAuth2 provider mismatch. Expected: " + existingProvider + ", Got: " + provider);
        }

        // Update name if improved information available from OAuth2
        String newActualName = oauth2UserInfo.getName();
        String currentActualName = CustomerOAuth2Utils.extractActualName(customer);

        if (newActualName != null && !newActualName.equals(currentActualName)) {
            String updatedFullName = CustomerOAuth2Utils.generateOAuth2FullName(
                    newActualName, provider, oauth2UserInfo.getId());
            customer.setFullName(updatedFullName);
            updated = true;
        }

        if (updated) {
            try {
                customer = customerService.save(customer);
                logger.debug("Updated OAuth2 customer information: {}", customer.getEmail());
            } catch (Exception e) {
                logger.warn("Failed to update OAuth2 customer: {}", e.getMessage());
            }
        }

        return customer;
    }

    /**
     * Links local account with OAuth2 provider
     * Allows existing local users to login via OAuth2
     * 
     * @param customer       the existing local Customer entity
     * @param oauth2UserInfo the OAuth2 user information
     * @param provider       the OAuth2 provider name
     * @return updated Customer entity
     */
    private Customer linkLocalAccountWithOAuth2(Customer customer, OAuth2UserInfo oauth2UserInfo, String provider) {
        logger.info("Linking local account with OAuth2: {} (provider: {})", customer.getEmail(), provider);

        // For existing local users, we allow OAuth2 login without changing their data
        // This enables hybrid authentication - they can use both email/password and
        // OAuth2

        // Optionally store OAuth2 provider info in a comment field or separate table
        // For now, we just allow the login to proceed with existing data

        logger.info("Successfully linked local account with OAuth2: {}", customer.getEmail());
        return customer;
    }
}