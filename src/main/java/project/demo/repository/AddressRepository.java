package project.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.demo.model.Address;

import java.util.List;

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
}
