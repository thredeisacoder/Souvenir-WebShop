package project.demo.service;

import project.demo.model.Customer;

/**
 * Service interface for managing authentication operations
 */
public interface AuthenticationService {
    /**
     * Login with email and password
     * 
     * @param email user's email
     * @param password user's password 
     * @param rememberMe whether to remember the login
     * @return authenticated customer if successful
     */
    Customer login(String email, String password, boolean rememberMe);

    /**
     * Login with email and password (without remember me)
     * 
     * @param email user's email
     * @param password user's password 
     * @return authenticated customer if successful
     */
    default Customer login(String email, String password) {
        return login(email, password, false);
    }

    /**
     * Logout current user
     */
    void logout();

    /**
     * Check if user is authenticated
     * 
     * @return true if user is authenticated
     */
    boolean isAuthenticated();

    /**
     * Get current authenticated customer
     * 
     * @return authenticated customer or null if not authenticated
     */
    Customer getCurrentCustomer();
}
