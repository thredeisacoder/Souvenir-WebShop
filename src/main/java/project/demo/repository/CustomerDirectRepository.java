package project.demo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import project.demo.model.Customer;

@Repository
public class CustomerDirectRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Insert a customer directly using SQL statement
     * 
     * @param customer the customer to insert
     * @return the ID of the inserted customer
     */
    @Transactional
    public Integer insertCustomerDirectly(Customer customer) {
        try {
            System.out.println("=== DIRECT SQL INSERT ===");
            System.out.println("Attempting direct SQL insert for customer: " + customer.getEmail());
            
            String sql = "INSERT INTO Customer (full_name, email, phone_number, password, status) " +
                        "VALUES (?, ?, ?, ?, ?)";
            
            // Execute the insert
            jdbcTemplate.update(
                sql,
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getPassword(),
                customer.getStatus()
            );
            
            // Get the ID of the newly inserted customer
            String idSql = "SELECT customer_id FROM Customer WHERE email = ?";
            Integer customerId = jdbcTemplate.queryForObject(idSql, Integer.class, customer.getEmail());
            
            System.out.println("Customer inserted successfully with ID: " + customerId);
            System.out.println("==========================");
            
            return customerId;
        } catch (Exception e) {
            System.err.println("=== DIRECT SQL INSERT ERROR ===");
            System.err.println("Failed to insert customer directly!");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("==============================");
            throw e;
        }
    }
    
    /**
     * Check if a customer with the given email exists
     * 
     * @param email the email to check
     * @return true if a customer with the email exists
     */
    public boolean existsByEmailDirect(String email) {
        String sql = "SELECT COUNT(*) FROM Customer WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
    
    /**
     * Check if a customer with the given phone number exists
     * 
     * @param phoneNumber the phone number to check
     * @return true if a customer with the phone number exists
     */
    public boolean existsByPhoneNumberDirect(String phoneNumber) {
        String sql = "SELECT COUNT(*) FROM Customer WHERE phone_number = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, phoneNumber);
        return count != null && count > 0;
    }
}