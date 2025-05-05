package project.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.demo.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Customer entities
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    
    /**
     * Find a customer by email
     * 
     * @param email the email to search for
     * @return an Optional containing the customer if found
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Find a customer by phone number
     * 
     * @param phoneNumber the phone number to search for
     * @return an Optional containing the customer if found
     */
    Optional<Customer> findByPhoneNumber(String phoneNumber);
    
    /**
     * Find customers by status
     * 
     * @param status the status to search for
     * @return a list of customers with the specified status
     */
    List<Customer> findByStatus(String status);
    
    /**
     * Find customers by name containing the given keyword
     * 
     * @param keyword the keyword to search for
     * @return a list of customers with names containing the keyword
     */
    List<Customer> findByFullNameContainingIgnoreCase(String keyword);
    
    /**
     * Check if a customer with the given email exists
     * 
     * @param email the email to check
     * @return true if a customer with the email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if a customer with the given phone number exists
     * 
     * @param phoneNumber the phone number to check
     * @return true if a customer with the phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);
}
