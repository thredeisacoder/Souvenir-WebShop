package project.demo.service;

import project.demo.model.Shipment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for managing Shipment entities
 */
public interface IShipmentService {
    
    /**
     * Find a shipment by its ID
     * 
     * @param shipmentId the ID of the shipment to find
     * @return the shipment if found
     */
    Shipment findById(Integer shipmentId);
    
    /**
     * Find a shipment by order ID
     * 
     * @param orderId the ID of the order
     * @return the shipment if found
     */
    Shipment findByOrderId(Integer orderId);
    
    /**
     * Create a new shipment for an order
     * 
     * @param orderId the ID of the order
     * @param shippingProvider the shipping provider
     * @param trackingNumber the tracking number
     * @param shippingCost the shipping cost
     * @return the created shipment
     */
    Shipment createShipment(Integer orderId, String shippingProvider, String trackingNumber, BigDecimal shippingCost);
    
    /**
     * Update the status of a shipment
     * 
     * @param shipmentId the ID of the shipment
     * @param status the new status
     * @return the updated shipment
     */
    Shipment updateStatus(Integer shipmentId, String status);
    
    /**
     * Update the tracking number of a shipment
     * 
     * @param shipmentId the ID of the shipment
     * @param trackingNumber the new tracking number
     * @return the updated shipment
     */
    Shipment updateTrackingNumber(Integer shipmentId, String trackingNumber);
    
    /**
     * Mark a shipment as delivered
     * 
     * @param shipmentId the ID of the shipment
     * @param deliveryDate the delivery date
     * @return the updated shipment
     */
    Shipment markAsDelivered(Integer shipmentId, LocalDate deliveryDate);
    
    /**
     * Calculate shipping cost based on shipping method
     * 
     * @param shippingMethod the shipping method
     * @return the calculated shipping cost
     */
    BigDecimal calculateShippingCost(String shippingMethod);
    
    /**
     * Find shipments by status
     * 
     * @param status the status to search for
     * @return a list of shipments with the specified status
     */
    List<Shipment> findByStatus(String status);
    
    /**
     * Update the estimated delivery date of a shipment
     * 
     * @param shipmentId the ID of the shipment
     * @param estimatedDeliveryDate the new estimated delivery date
     * @return the updated shipment
     */
    Shipment updateEstimatedDeliveryDate(Integer shipmentId, LocalDate estimatedDeliveryDate);
}
