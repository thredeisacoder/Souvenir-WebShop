package project.demo.service;

import java.util.List;
import java.util.Optional;

import project.demo.model.Customer;

public interface ICustomerService {
    Optional<Customer> findById(Integer customerId);
    Optional<Customer> findByEmail(String email);
    List<Customer> findAll();
    Customer save(Customer customer);
    Customer update(Customer customer);
    void delete(Integer customerId);
    
    // Thêm các phương thức còn thiếu
    boolean authenticate(String email, String password);
    Customer register(Customer customer);
    boolean changePassword(Integer customerId, String currentPassword, String newPassword);
}
