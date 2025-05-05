package project.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.demo.model.Shipment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Shipment entities
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {
    
    /**
     * Find a shipment by order ID
     * 
     * @param orderId the ID of the order
     * @return an Optional containing the shipment if found
     */
    Optional<Shipment> findByOrderId(Integer orderId);
    
    /**
     * Find shipments by status
     * 
     * @param shipmentStatus the status to search for
     * @return a list of shipments with the specified status
     */
    List<Shipment> findByShipmentStatus(String shipmentStatus);
    
    /**
     * Find shipments by tracking number
     * 
     * @param trackingNumber the tracking number to search for
     * @return a list of shipments with the specified tracking number
     */
    List<Shipment> findByTrackingNumber(String trackingNumber);
    
    /**
     * Find shipments by shipping provider
     * 
     * @param shippingProvider the shipping provider to search for
     * @return a list of shipments with the specified shipping provider
     */
    List<Shipment> findByShippingProvider(String shippingProvider);
    
    /**
     * Find shipments with shipping dates between two dates
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of shipments with shipping dates between the given dates
     */
    List<Shipment> findByShippingDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find shipments with estimated delivery dates between two dates
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of shipments with estimated delivery dates between the given dates
     */
    List<Shipment> findByEstimatedDeliveryDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find shipments with delivery dates between two dates
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of shipments with delivery dates between the given dates
     */
    List<Shipment> findByDeliveryDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Count shipments by status
     * 
     * @param shipmentStatus the status to count
     * @return the number of shipments with the specified status
     */
    long countByShipmentStatus(String shipmentStatus);
}
