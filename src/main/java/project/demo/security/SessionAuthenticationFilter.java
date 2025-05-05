package project.demo.security;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import project.demo.model.Customer;

/**
 * Filter to authenticate users based on session attributes
 * This bridges the gap between our custom session-based authentication
 * and Spring Security's authentication context
 */
public class SessionAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        logger.debug("SessionAuthenticationFilter running for URL: {}", request.getRequestURI());
        
        try {
            // Check if user is already authenticated in Spring Security context
            if (SecurityContextHolder.getContext().getAuthentication() == null ||
                !SecurityContextHolder.getContext().getAuthentication().isAuthenticated() ||
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
                
                logger.debug("No active authentication found in SecurityContext");
                
                // Check if user is authenticated in session
                HttpSession session = request.getSession(false);
                if (session != null) {
                    logger.debug("Session found with ID: {}", session.getId());
                    
                    Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
                    logger.debug("Session isLoggedIn attribute: {}", isLoggedIn);
                    
                    if (isLoggedIn != null && isLoggedIn) {
                        Customer customer = (Customer) session.getAttribute("customer");
                        if (customer != null) {
                            logger.debug("Customer found in session: {}", customer.getEmail());
                            
                            // Create authentication token with both USER and CUSTOMER roles
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                customer.getEmail(),
                                null, // credentials are not needed here
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                            
                            // Add customer object as details to the authentication token
                            auth.setDetails(customer);
                            
                            // Set authentication in context
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            logger.debug("Authentication set in SecurityContext for user: {}", customer.getEmail());
                        } else {
                            logger.warn("Customer object is null in session despite isLoggedIn=true - Session may be corrupted");
                            // Reset session attributes to force re-login
                            session.setAttribute("isLoggedIn", false);
                            logger.debug("Reset isLoggedIn attribute to false");
                        }
                    } else {
                        logger.debug("User is not logged in according to session");
                    }
                } else {
                    logger.debug("No session found or session has expired");
                }
            } else {
                logger.debug("User already authenticated in SecurityContext: {}", 
                    SecurityContextHolder.getContext().getAuthentication().getName());
            }
        } catch (Exception e) {
            // Log any exception but don't block the request
            logger.error("Error in SessionAuthenticationFilter", e);
        }
        
        filterChain.doFilter(request, response);
    }
}
