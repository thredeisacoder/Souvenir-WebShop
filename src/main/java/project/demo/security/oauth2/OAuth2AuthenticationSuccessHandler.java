package project.demo.security.oauth2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import project.demo.model.Customer;
import project.demo.util.CustomerOAuth2Utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.servlet.http.HttpSession;

/**
 * OAuth2 Authentication Success Handler
 * Handles successful OAuth2 login and determine redirect paths.
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    /**
     * Handles successful OAuth2 authentication
     * 
     * @param request        the HTTP request
     * @param response       the HTTP response
     * @param authentication the successful authentication
     * @throws IOException      if redirect fails
     * @throws ServletException if servlet error occurs
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        logger.debug("OAuth2 authentication successful for: {}", authentication.getName());

        try {
            // Determine authentication type and handle accordingly
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                handleOAuth2Success(request, response, authentication);
            } else {
                // Fallback for other authentication types
                handleDefaultSuccess(request, response, authentication);
            }
        } catch (Exception ex) {
            logger.error("Error handling OAuth2 authentication success", ex);
            response.sendRedirect("/auth/login?error=oauth2");
        }
    }

    /**
     * Handles OAuth2 authentication success
     * 
     * @param request        the HTTP request
     * @param response       the HTTP response
     * @param authentication the OAuth2 authentication
     * @throws IOException if redirect fails
     */
    private void handleOAuth2Success(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
        Customer customer = oauth2User.getCustomer();

        logger.info("Processing OAuth2 success for customer: {} (ID: {})",
                customer.getEmail(), customer.getCustomerId());

        // Check if this is account linking request
        if (isAccountLinkingRequest(request)) {
            handleAccountLinking(request, response, customer);
            return;
        }

        // Check if profile completion is needed
        if (isProfileIncomplete(customer)) {
            logger.debug("Profile incomplete for customer: {}, redirecting to complete profile", customer.getEmail());
            response.sendRedirect("/auth/complete-profile");
            return;
        }

        // Normal OAuth2 login - redirect to intended destination
        String targetUrl = determineTargetUrl(request, customer);
        logger.debug("Redirecting OAuth2 user to: {}", targetUrl);
        response.sendRedirect(targetUrl);
    }

    /**
     * Handles account linking scenario
     * 
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param customer the customer entity
     * @throws IOException if redirect fails
     */
    private void handleAccountLinking(HttpServletRequest request, HttpServletResponse response,
            Customer customer) throws IOException {
        String provider = CustomerOAuth2Utils.getOAuth2Provider(customer);
        logger.info("Account linking successful for customer: {} with provider: {}",
                customer.getEmail(), provider);

        response.sendRedirect("/account/profile?linked=" + provider + "&success=true");
    }

    /**
     * Determines if this is an account linking request
     * 
     * @param request the HTTP request
     * @return true if this is account linking
     */
    private boolean isAccountLinkingRequest(HttpServletRequest request) {
        String linkParam = request.getParameter("link");
        return "true".equals(linkParam);
    }

    /**
     * Checks if customer profile is incomplete
     * 
     * @param customer the customer to check
     * @return true if profile needs completion
     */
    private boolean isProfileIncomplete(Customer customer) {
        if (customer == null) {
            return true;
        }

        // Check for essential missing information
        boolean missingPhone = customer.getPhoneNumber() == null ||
                customer.getPhoneNumber().trim().isEmpty() ||
                "0000000000".equals(customer.getPhoneNumber()) ||
                "000-000-0000".equals(customer.getPhoneNumber());

        // Check if fullName is just the OAuth2 placeholder
        String actualName = CustomerOAuth2Utils.extractActualName(customer);
        boolean missingName = actualName == null || actualName.trim().isEmpty() ||
                "OAuth2 User".equals(actualName);

        return missingPhone || missingName;
    }

    /**
     * Determines the target URL after successful authentication
     * 
     * @param request  the HTTP request
     * @param customer the authenticated customer
     * @return the target URL
     */
    private String determineTargetUrl(HttpServletRequest request, Customer customer) {
        // Check for saved request URL
        String savedRequest = getSavedRequestUrl(request);
        if (savedRequest != null) {
            return savedRequest;
        }

        // Check for redirect parameter
        String redirect = request.getParameter("redirect");
        if (redirect != null) {
            switch (redirect.toLowerCase()) {
                case "cart":
                    return "/cart";
                case "checkout":
                    return "/checkout";
                case "account":
                    return "/account/profile";
                case "products":
                    return "/products";
                default:
                    break;
            }
        }

        // Check if OAuth2-only user (first time login)
        if (CustomerOAuth2Utils.isOAuth2User(customer)) {
            // New OAuth2 user - show welcome or onboarding
            return "/?welcome=true";
        }

        // Default redirect
        return "/";
    }

    /**
     * Gets saved request URL from session
     * 
     * @param request the HTTP request
     * @return the saved URL or null
     */
    private String getSavedRequestUrl(HttpServletRequest request) {
        // This would integrate with Spring Security's saved request mechanism
        // For now, return null to use default behavior
        return null;
    }

    /**
     * Handles default authentication success (non-OAuth2)
     * 
     * @param request        the HTTP request
     * @param response       the HTTP response
     * @param authentication the authentication
     * @throws IOException if redirect fails
     */
    private void handleDefaultSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        logger.debug("Handling default authentication success for: {}", authentication.getName());
        response.sendRedirect("/");
    }
}