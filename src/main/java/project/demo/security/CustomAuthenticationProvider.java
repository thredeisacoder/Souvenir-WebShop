package project.demo.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import project.demo.model.Customer;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Get the current session
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession(false);

        // Check if user is authenticated in session
        if (session != null && session.getAttribute("isLoggedIn") != null &&
                (Boolean) session.getAttribute("isLoggedIn") && session.getAttribute("customer") != null) {

            Customer customer = (Customer) session.getAttribute("customer");

            // Create authentication token
            return new UsernamePasswordAuthenticationToken(
                    customer.getEmail(),
                    null, // credentials are not needed here
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        }

        // Not authenticated
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
