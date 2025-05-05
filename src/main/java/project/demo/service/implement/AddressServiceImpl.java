package project.demo.service.implement;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.demo.exception.AddressException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Address;
import project.demo.repository.AddressRepository;
import project.demo.service.IAddressService;

/**
 * Implementation of the IAddressService interface for managing Address entities
 */
@Service
public class AddressServiceImpl implements IAddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Address findById(Integer addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("ADDRESS_NOT_FOUND",
                        "Address not found with ID: " + addressId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Address> findByCustomerId(Integer customerId) {
        return addressRepository.findByCustomerId(customerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Address save(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }

        // Validate required fields
        if (address.getCustomerId() == null) {
            throw AddressException.missingRequiredField("Customer ID");
        }
        if (address.getAddressLine() == null || address.getAddressLine().trim().isEmpty()) {
            throw AddressException.missingRequiredField("Address line");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            throw AddressException.missingRequiredField("City");
        }
        if (address.getCountry() == null || address.getCountry().trim().isEmpty()) {
            throw AddressException.missingRequiredField("Country");
        }

        // Đảm bảo isDefault không bị null
        if (address.getIsDefault() == null) {
            address.setIsDefault(false);
        }

        // Handle default address
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            // Get all customer addresses
            List<Address> customerAddresses = addressRepository.findByCustomerId(address.getCustomerId());

            // Update all existing addresses to not be default
            for (Address existingAddress : customerAddresses) {
                if (Boolean.TRUE.equals(existingAddress.getIsDefault())) {
                    existingAddress.setIsDefault(false);
                    addressRepository.save(existingAddress);
                }
            }
        }

        return addressRepository.save(address);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Address update(Address address) {
        if (address == null || address.getAddressId() == null) {
            throw AddressException.missingRequiredField("Address ID");
        }

        // Check if address exists
        Address existingAddress = addressRepository.findById(address.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("ADDRESS_NOT_FOUND",
                        "Address not found with ID: " + address.getAddressId()));

        // Check if address belongs to the customer
        if (!existingAddress.getCustomerId().equals(address.getCustomerId())) {
            throw AddressException.notOwnedByCustomer(address.getAddressId(), address.getCustomerId());
        }

        // Validate required fields
        if (address.getCustomerId() == null) {
            throw AddressException.missingRequiredField("Customer ID");
        }
        if (address.getAddressLine() == null || address.getAddressLine().trim().isEmpty()) {
            throw AddressException.missingRequiredField("Address line");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            throw AddressException.missingRequiredField("City");
        }
        if (address.getCountry() == null || address.getCountry().trim().isEmpty()) {
            throw AddressException.missingRequiredField("Country");
        }

        // Handle default address
        if (Boolean.TRUE.equals(address.getIsDefault()) && !Boolean.TRUE.equals(existingAddress.getIsDefault())) {
            // Get all customer addresses
            List<Address> customerAddresses = addressRepository.findByCustomerId(address.getCustomerId());

            // Update all existing addresses to not be default
            for (Address otherAddress : customerAddresses) {
                if (!otherAddress.getAddressId().equals(address.getAddressId()) &&
                        Boolean.TRUE.equals(otherAddress.getIsDefault())) {
                    otherAddress.setIsDefault(false);
                    addressRepository.save(otherAddress);
                }
            }
        }

        return addressRepository.save(address);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(Integer addressId) {
        // Check if address exists
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("ADDRESS_NOT_FOUND",
                        "Address not found with ID: " + addressId));

        // Check if address is used in any orders
        if (address.getOrders() != null && !address.getOrders().isEmpty()) {
            throw AddressException.cannotDelete(addressId, "Address is used in orders");
        }

        // Check if address is the default address
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            throw AddressException.cannotDelete(addressId, "Cannot delete default address");
        }

        addressRepository.deleteById(addressId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Address setAsDefault(Integer addressId, Integer customerId) {
        // Check if address exists
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("ADDRESS_NOT_FOUND",
                        "Address not found with ID: " + addressId));

        // Check if address belongs to the customer
        if (!address.getCustomerId().equals(customerId)) {
            throw AddressException.notOwnedByCustomer(addressId, customerId);
        }

        // Get all customer addresses
        List<Address> customerAddresses = addressRepository.findByCustomerId(customerId);

        // Update all addresses to not be default
        for (Address customerAddress : customerAddresses) {
            if (Boolean.TRUE.equals(customerAddress.getIsDefault())) {
                customerAddress.setIsDefault(false);
                addressRepository.save(customerAddress);
            }
        }

        // Set the specified address as default
        address.setIsDefault(true);
        return addressRepository.save(address);
    }
}
