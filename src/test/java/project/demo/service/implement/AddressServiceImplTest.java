package project.demo.service.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import project.demo.exception.AddressException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Address;
import project.demo.model.Order;
import project.demo.repository.AddressRepository;
import project.demo.util.ValidationErrorMessages;

class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    private ValidationErrorMessages validationErrorMessages;
    private AddressServiceImpl addressService;

    private Address testAddress;
    private List<Order> orders;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create real instance of ValidationErrorMessages
        validationErrorMessages = new ValidationErrorMessages();
        
        // Create AddressServiceImpl with real ValidationErrorMessages
        addressService = new AddressServiceImpl(addressRepository, validationErrorMessages);

        // Setup test address
        testAddress = new Address();
        testAddress.setAddressId(1);
        testAddress.setCustomerId(1);
        testAddress.setAddressLine("123 Test Street");
        testAddress.setCity("Test City");
        testAddress.setCountry("Test Country");
        testAddress.setZipCode("12345");

        // Setup empty orders list
        orders = new ArrayList<>();
        testAddress.setOrders(orders);
    }

    @Test
    void findById_ExistingAddress_ReturnsAddress() {
        // Arrange
        when(addressRepository.findById(1)).thenReturn(Optional.of(testAddress));

        // Act
        Address result = addressService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testAddress.getAddressId(), result.getAddressId());
        verify(addressRepository, times(1)).findById(1);
    }

    @Test
    void findById_NonExistingAddress_ThrowsException() {
        // Arrange
        when(addressRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> addressService.findById(999));
        verify(addressRepository, times(1)).findById(999);
    }

    @Test
    void findByCustomerId_ExistingCustomer_ReturnsAddresses() {
        // Arrange
        List<Address> addresses = new ArrayList<>();
        addresses.add(testAddress);
        when(addressRepository.findByCustomerId(1)).thenReturn(addresses);

        // Act
        List<Address> result = addressService.findByCustomerId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAddress.getAddressId(), result.get(0).getAddressId());
        verify(addressRepository, times(1)).findByCustomerId(1);
    }

    @Test
    void save_ValidAddress_SavesAddress() {
        // Arrange
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // Act
        Address result = addressService.save(testAddress);

        // Assert
        assertNotNull(result);
        assertEquals(testAddress.getAddressId(), result.getAddressId());
        verify(addressRepository, times(1)).save(testAddress);
    }

    @Test
    void save_NullAddress_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> addressService.save(null));
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void save_MissingCustomerId_ThrowsException() {
        // Arrange
        Address address = new Address();
        address.setAddressLine("123 Test Street");
        address.setCity("Test City");
        address.setCountry("Test Country");

        // Act & Assert
        assertThrows(AddressException.class, () -> addressService.save(address));
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void save_MissingAddressLine_ThrowsException() {
        // Arrange
        Address address = new Address();
        address.setCustomerId(1);
        address.setCity("Test City");
        address.setCountry("Test Country");

        // Act & Assert
        assertThrows(AddressException.class, () -> addressService.save(address));
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void save_MissingCity_ThrowsException() {
        // Arrange
        Address address = new Address();
        address.setCustomerId(1);
        address.setAddressLine("123 Test Street");
        address.setCountry("Test Country");

        // Act & Assert
        assertThrows(AddressException.class, () -> addressService.save(address));
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void save_MissingCountry_ThrowsException() {
        // Arrange
        Address address = new Address();
        address.setCustomerId(1);
        address.setAddressLine("123 Test Street");
        address.setCity("Test City");

        // Act & Assert
        assertThrows(AddressException.class, () -> addressService.save(address));
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void save_DuplicateAddress_ThrowsException() {
        // Arrange
        Address newAddress = new Address();
        newAddress.setCustomerId(1);
        newAddress.setAddressLine("123 Test Street");
        newAddress.setCity("Test City");
        newAddress.setCountry("Test Country");

        // Mock repository to return existing address when checking for duplicates
        when(addressRepository.findByCustomerIdAndAddressLineIgnoreCaseAndCityIgnoreCaseAndCountryIgnoreCase(
                1, "123 Test Street", "Test City", "Test Country"))
                .thenReturn(Optional.of(testAddress));

        // Act & Assert
        AddressException exception = assertThrows(AddressException.class, () -> addressService.save(newAddress));
        assertEquals("DUPLICATE_ADDRESS", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Địa chỉ đã tồn tại"));
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void update_ValidAddress_UpdatesAddress() {
        // Arrange
        when(addressRepository.findById(1)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // Act
        Address result = addressService.update(testAddress);

        // Assert
        assertNotNull(result);
        assertEquals(testAddress.getAddressId(), result.getAddressId());
        verify(addressRepository, times(1)).findById(1);
        verify(addressRepository, times(1)).save(testAddress);
    }

    @Test
    void update_NonExistingAddress_ThrowsException() {
        // Arrange
        when(addressRepository.findById(999)).thenReturn(Optional.empty());

        // Create address with non-existing ID
        Address address = new Address();
        address.setAddressId(999);
        address.setCustomerId(1);
        address.setAddressLine("123 Test Street");
        address.setCity("Test City");
        address.setCountry("Test Country");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> addressService.update(address));
        verify(addressRepository, times(1)).findById(999);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void update_AddressBelongingToDifferentCustomer_ThrowsException() {
        // Arrange
        when(addressRepository.findById(1)).thenReturn(Optional.of(testAddress));

        // Create address with different customer ID
        Address address = new Address();
        address.setAddressId(1);
        address.setCustomerId(2); // Different customer ID
        address.setAddressLine("123 Test Street");
        address.setCity("Test City");
        address.setCountry("Test Country");

        // Act & Assert
        assertThrows(AddressException.class, () -> addressService.update(address));
        verify(addressRepository, times(1)).findById(1);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void update_DuplicateAddress_ThrowsException() {
        // Arrange
        when(addressRepository.findById(1)).thenReturn(Optional.of(testAddress));

        // Create another existing address with different ID but same details
        Address existingDuplicateAddress = new Address();
        existingDuplicateAddress.setAddressId(2); // Different ID
        existingDuplicateAddress.setCustomerId(1);
        existingDuplicateAddress.setAddressLine("456 Different Street");
        existingDuplicateAddress.setCity("Different City");
        existingDuplicateAddress.setCountry("Different Country");

        // Update address with same details as another existing address
        Address updateAddress = new Address();
        updateAddress.setAddressId(1);
        updateAddress.setCustomerId(1);
        updateAddress.setAddressLine("456 Different Street");
        updateAddress.setCity("Different City");
        updateAddress.setCountry("Different Country");

        // Mock repository to return existing duplicate address
        when(addressRepository.findByCustomerIdAndAddressLineIgnoreCaseAndCityIgnoreCaseAndCountryIgnoreCase(
                1, "456 Different Street", "Different City", "Different Country"))
                .thenReturn(Optional.of(existingDuplicateAddress));

        // Act & Assert
        AddressException exception = assertThrows(AddressException.class, () -> addressService.update(updateAddress));
        assertEquals("DUPLICATE_ADDRESS", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Địa chỉ đã tồn tại"));
        verify(addressRepository, times(1)).findById(1);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void delete_ExistingAddress_DeletesAddress() {
        // Arrange
        when(addressRepository.findById(1)).thenReturn(Optional.of(testAddress));
        doNothing().when(addressRepository).deleteById(1);

        // Act
        addressService.delete(1);

        // Assert
        verify(addressRepository, times(1)).findById(1);
        verify(addressRepository, times(1)).deleteById(1);
    }

    @Test
    void delete_NonExistingAddress_ThrowsException() {
        // Arrange
        when(addressRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> addressService.delete(999));
        verify(addressRepository, times(1)).findById(999);
        verify(addressRepository, never()).deleteById(anyInt());
    }

    @Test
    void delete_AddressWithOrders_ThrowsException() {
        // Arrange
        // Add an order to the address
        Order order = new Order();
        order.setOrderId(1);
        orders.add(order);
        testAddress.setOrders(orders);

        when(addressRepository.findById(1)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        assertThrows(AddressException.class, () -> addressService.delete(1));
        verify(addressRepository, times(1)).findById(1);
        verify(addressRepository, never()).deleteById(anyInt());
    }

    @Test
    void delete_DefaultAddress_ThrowsException() {
        // Arrange
        testAddress.setIsDefault(true);
        when(addressRepository.findById(1)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        assertThrows(AddressException.class, () -> addressService.delete(1));
        verify(addressRepository, times(1)).findById(1);
        verify(addressRepository, never()).deleteById(anyInt());
    }

    @Test
    void setAsDefault_ExistingAddress_SetsAsDefault() {
        // Arrange
        testAddress.setIsDefault(false);

        // Create another address that is currently the default
        Address defaultAddress = new Address();
        defaultAddress.setAddressId(2);
        defaultAddress.setCustomerId(1);
        defaultAddress.setAddressLine("456 Default Street");
        defaultAddress.setCity("Default City");
        defaultAddress.setCountry("Default Country");
        defaultAddress.setIsDefault(true);

        List<Address> addresses = new ArrayList<>();
        addresses.add(defaultAddress);

        when(addressRepository.findById(1)).thenReturn(Optional.of(testAddress));
        when(addressRepository.findByCustomerId(1)).thenReturn(addresses);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Address result = addressService.setAsDefault(1, 1);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsDefault());
        verify(addressRepository, times(1)).findById(1);
        verify(addressRepository, times(1)).findByCustomerId(1);
        verify(addressRepository, times(2)).save(any(Address.class)); // Once for defaultAddress, once for testAddress
    }

    @Test
    void setAsDefault_AddressBelongingToDifferentCustomer_ThrowsException() {
        // Arrange
        when(addressRepository.findById(1)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        assertThrows(AddressException.class, () -> addressService.setAsDefault(1, 2)); // Different customer ID
        verify(addressRepository, times(1)).findById(1);
        verify(addressRepository, never()).findByCustomerId(anyInt());
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void save_DefaultAddress_UpdatesOtherAddresses() {
        // Arrange
        testAddress.setIsDefault(true);

        // Create another address that is currently the default
        Address existingAddress = new Address();
        existingAddress.setAddressId(2);
        existingAddress.setCustomerId(1);
        existingAddress.setAddressLine("456 Existing Street");
        existingAddress.setCity("Existing City");
        existingAddress.setCountry("Existing Country");
        existingAddress.setIsDefault(true);

        List<Address> addresses = new ArrayList<>();
        addresses.add(existingAddress);

        when(addressRepository.findByCustomerId(1)).thenReturn(addresses);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Address result = addressService.save(testAddress);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsDefault());
        verify(addressRepository, times(1)).findByCustomerId(1);
        verify(addressRepository, times(2)).save(any(Address.class)); // Once for existingAddress, once for testAddress
    }

    @Test
    void update_DefaultAddress_UpdatesOtherAddresses() {
        // Arrange
        testAddress.setIsDefault(false); // Existing address is not default

        // Updated address with isDefault = true
        Address updatedAddress = new Address();
        updatedAddress.setAddressId(1);
        updatedAddress.setCustomerId(1);
        updatedAddress.setAddressLine("123 Updated Street");
        updatedAddress.setCity("Updated City");
        updatedAddress.setCountry("Updated Country");
        updatedAddress.setIsDefault(true);

        // Another address that is currently the default
        Address defaultAddress = new Address();
        defaultAddress.setAddressId(2);
        defaultAddress.setCustomerId(1);
        defaultAddress.setAddressLine("456 Default Street");
        defaultAddress.setCity("Default City");
        defaultAddress.setCountry("Default Country");
        defaultAddress.setIsDefault(true);

        List<Address> addresses = new ArrayList<>();
        addresses.add(defaultAddress);

        when(addressRepository.findById(1)).thenReturn(Optional.of(testAddress));
        when(addressRepository.findByCustomerId(1)).thenReturn(addresses);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Address result = addressService.update(updatedAddress);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsDefault());
        verify(addressRepository, times(1)).findById(1);
        verify(addressRepository, times(1)).findByCustomerId(1);
        verify(addressRepository, atLeast(2)).save(any(Address.class)); // At least once for defaultAddress, once for
                                                                        // updatedAddress
    }
}
