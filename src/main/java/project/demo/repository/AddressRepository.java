package project.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import project.demo.model.Address;

/**
 * Repository interface for Address entities
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    
    /**
     * Find all addresses for a specific customer
     * 
     * @param customerId the ID of the customer
     * @return a list of addresses belonging to the customer
     */
    List<Address> findByCustomerId(Integer customerId);
    
    /**
     * Find an address with the same customer ID, address line, city, and country
     * 
     * @param customerId the ID of the customer
     * @param addressLine the address line
     * @param city the city
     * @param country the country
     * @return optional address if found
     */
    Optional<Address> findByCustomerIdAndAddressLineIgnoreCaseAndCityIgnoreCaseAndCountryIgnoreCase(
            Integer customerId, String addressLine, String city, String country);
}
