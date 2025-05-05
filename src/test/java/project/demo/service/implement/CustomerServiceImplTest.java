package project.demo.service.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import project.demo.enums.CustomerStatus;
import project.demo.exception.CustomerException;
import project.demo.model.Customer;
import project.demo.repository.CustomerRepository;

class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test customer
        testCustomer = new Customer();
        testCustomer.setCustomerId(1);
        testCustomer.setFullName("Test User");
        testCustomer.setEmail("test@example.com");
        testCustomer.setPhoneNumber("+1234567890");
        testCustomer.setPassword("hashedPassword");
        testCustomer.setStatus(CustomerStatus.ACTIVE.getValue());
    }

    @Test
    void findById_ExistingCustomer_ReturnsCustomer() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));

        // Act
        Optional<Customer> result = customerService.findById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCustomer.getCustomerId(), result.get().getCustomerId());
        verify(customerRepository, times(1)).findById(1);
    }

    @Test
    void findById_NonExistingCustomer_ReturnsEmptyOptional() {
        // Arrange
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<Customer> result = customerService.findById(999);

        // Assert
        assertTrue(result.isEmpty());
        verify(customerRepository, times(1)).findById(999);
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsCustomer() {
        // Arrange
        when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testCustomer));

        // Act
        Optional<Customer> result = customerService.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCustomer.getEmail(), result.get().getEmail());
        verify(customerRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        // Arrange
        when(customerRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<Customer> result = customerService.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
        verify(customerRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void register_ValidCustomer_RegistersCustomer() {
        // Arrange
        Customer newCustomer = new Customer();
        newCustomer.setFullName("New User");
        newCustomer.setEmail("new@example.com");
        newCustomer.setPhoneNumber("+9876543210");
        newCustomer.setPassword("StrongP@ss1");
        
        when(customerRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(customerRepository.existsByPhoneNumber("+9876543210")).thenReturn(false);
        when(passwordEncoder.encode("StrongP@ss1")).thenReturn("encodedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(newCustomer);

        // Act
        Customer result = customerService.register(newCustomer);

        // Assert
        assertNotNull(result);
        assertEquals(newCustomer.getEmail(), result.getEmail());
        assertEquals(CustomerStatus.ACTIVE.getValue(), result.getStatus());
        verify(customerRepository, times(1)).existsByEmail("new@example.com");
        verify(customerRepository, times(1)).existsByPhoneNumber("+9876543210");
        verify(passwordEncoder, times(1)).encode("StrongP@ss1");
        verify(customerRepository, times(1)).save(newCustomer);
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        // Arrange
        Customer newCustomer = new Customer();
        newCustomer.setFullName("New User");
        newCustomer.setEmail("existing@example.com");
        newCustomer.setPhoneNumber("+9876543210");
        newCustomer.setPassword("StrongP@ss1");
        
        when(customerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(CustomerException.class, () -> customerService.register(newCustomer));
        verify(customerRepository, times(1)).existsByEmail("existing@example.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void register_DuplicatePhoneNumber_ThrowsException() {
        // Arrange
        Customer newCustomer = new Customer();
        newCustomer.setFullName("New User");
        newCustomer.setEmail("new@example.com");
        newCustomer.setPhoneNumber("+1234567890"); // Existing phone number
        newCustomer.setPassword("StrongP@ss1");
        
        when(customerRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(customerRepository.existsByPhoneNumber("+1234567890")).thenReturn(true);

        // Act & Assert
        assertThrows(CustomerException.class, () -> customerService.register(newCustomer));
        verify(customerRepository, times(1)).existsByEmail("new@example.com");
        verify(customerRepository, times(1)).existsByPhoneNumber("+1234567890");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void register_WeakPassword_ThrowsException() {
        // Arrange
        Customer newCustomer = new Customer();
        newCustomer.setFullName("New User");
        newCustomer.setEmail("new@example.com");
        newCustomer.setPhoneNumber("+9876543210");
        newCustomer.setPassword("weak"); // Weak password
        
        when(customerRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(customerRepository.existsByPhoneNumber("+9876543210")).thenReturn(false);

        // Act & Assert
        assertThrows(CustomerException.class, () -> customerService.register(newCustomer));
        verify(customerRepository, times(1)).existsByEmail("new@example.com");
        verify(customerRepository, times(1)).existsByPhoneNumber("+9876543210");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void update_ValidCustomer_UpdatesCustomer() {
        // Arrange
        Customer updatedCustomer = new Customer();
        updatedCustomer.setCustomerId(1);
        updatedCustomer.setFullName("Updated User");
        updatedCustomer.setEmail("test@example.com"); // Same email
        updatedCustomer.setPhoneNumber("+1234567890"); // Same phone
        updatedCustomer.setPassword("irrelevant"); // Should be ignored
        
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        // Act
        Customer result = customerService.update(updatedCustomer);

        // Assert
        assertNotNull(result);
        assertEquals("Updated User", result.getFullName());
        assertEquals("hashedPassword", updatedCustomer.getPassword()); // Password should not change
        verify(customerRepository, times(1)).findById(1);
        verify(customerRepository, times(1)).save(updatedCustomer);
    }

    @Test
    void update_ChangedEmail_UpdatesCustomer() {
        // Arrange
        Customer updatedCustomer = new Customer();
        updatedCustomer.setCustomerId(1);
        updatedCustomer.setFullName("Updated User");
        updatedCustomer.setEmail("updated@example.com"); // New email
        updatedCustomer.setPhoneNumber("+1234567890"); // Same phone
        updatedCustomer.setPassword("irrelevant"); // Should be ignored
        
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        // Act
        Customer result = customerService.update(updatedCustomer);

        // Assert
        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());
        verify(customerRepository, times(1)).findById(1);
        verify(customerRepository, times(1)).existsByEmail("updated@example.com");
        verify(customerRepository, times(1)).save(updatedCustomer);
    }

    @Test
    void update_DuplicateEmail_ThrowsException() {
        // Arrange
        Customer updatedCustomer = new Customer();
        updatedCustomer.setCustomerId(1);
        updatedCustomer.setFullName("Updated User");
        updatedCustomer.setEmail("duplicate@example.com"); // New email that already exists
        updatedCustomer.setPhoneNumber("+1234567890"); // Same phone
        
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(CustomerException.class, () -> customerService.update(updatedCustomer));
        verify(customerRepository, times(1)).findById(1);
        verify(customerRepository, times(1)).existsByEmail("duplicate@example.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void authenticate_ValidCredentials_ReturnsTrue() {
        // Arrange
        when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);

        // Act
        boolean result = customerService.authenticate("test@example.com", "correctPassword");

        // Assert
        assertTrue(result);
        verify(customerRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("correctPassword", "hashedPassword");
    }

    @Test
    void authenticate_InvalidEmail_ReturnsFalse() {
        // Arrange
        when(customerRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        // Act
        boolean result = customerService.authenticate("wrong@example.com", "anyPassword");

        // Assert
        assertFalse(result);
        verify(customerRepository, times(1)).findByEmail("wrong@example.com");
    }

    @Test
    void authenticate_InvalidPassword_ReturnsFalse() {
        // Arrange
        when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        // Act
        boolean result = customerService.authenticate("test@example.com", "wrongPassword");

        // Assert
        assertFalse(result);
        verify(customerRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("wrongPassword", "hashedPassword");
    }

    @Test
    void authenticate_InactiveCustomer_ReturnsFalse() {
        // Arrange
        Customer inactiveCustomer = new Customer();
        inactiveCustomer.setCustomerId(2);
        inactiveCustomer.setEmail("inactive@example.com");
        inactiveCustomer.setPassword("hashedPassword");
        inactiveCustomer.setStatus(CustomerStatus.INACTIVE.getValue());
        
        when(customerRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactiveCustomer));

        // Act
        boolean result = customerService.authenticate("inactive@example.com", "anyPassword");
        
        // Assert
        assertFalse(result);
        verify(customerRepository, times(1)).findByEmail("inactive@example.com");
    }

    @Test
    void changePassword_ValidOldPassword_ReturnsTrue() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewP@ssw0rd")).thenReturn("newHashedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        boolean result = customerService.changePassword(1, "oldPassword", "NewP@ssw0rd");

        // Assert
        assertTrue(result);
        assertEquals("newHashedPassword", testCustomer.getPassword());
        verify(customerRepository, times(1)).findById(1);
        verify(passwordEncoder, times(1)).matches("oldPassword", "hashedPassword");
        verify(passwordEncoder, times(1)).encode("NewP@ssw0rd");
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void changePassword_InvalidOldPassword_ThrowsException() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("wrongOldPassword", "hashedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(CustomerException.class, () -> customerService.changePassword(1, "wrongOldPassword", "NewP@ssw0rd"));
        verify(customerRepository, times(1)).findById(1);
        verify(passwordEncoder, times(1)).matches("wrongOldPassword", "hashedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void changePassword_WeakNewPassword_ThrowsException() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);

        // Act & Assert
        assertThrows(CustomerException.class, () -> customerService.changePassword(1, "oldPassword", "weak"));
        verify(customerRepository, times(1)).findById(1);
        verify(passwordEncoder, times(1)).matches("oldPassword", "hashedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void findAll_ReturnsAllCustomers() {
        // Arrange
        List<Customer> customers = new ArrayList<>();
        customers.add(testCustomer);
        when(customerRepository.findAll()).thenReturn(customers);

        // Act
        List<Customer> result = customerService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCustomer.getCustomerId(), result.get(0).getCustomerId());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void deactivateAccount_ExistingCustomer_DeactivatesAccount() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        customerService.deactivateAccount(1);

        // Assert
        assertEquals(CustomerStatus.INACTIVE.getValue(), testCustomer.getStatus());
        verify(customerRepository, times(1)).findById(1);
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void activateAccount_ExistingCustomer_ActivatesAccount() {
        // Arrange
        testCustomer.setStatus(CustomerStatus.INACTIVE.getValue());
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        customerService.activateAccount(1);

        // Assert
        assertEquals(CustomerStatus.ACTIVE.getValue(), testCustomer.getStatus());
        verify(customerRepository, times(1)).findById(1);
        verify(customerRepository, times(1)).save(testCustomer);
    }
}
