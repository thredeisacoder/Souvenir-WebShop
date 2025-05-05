package project.demo.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.demo.enums.OrderStatus;
import project.demo.enums.ShipmentStatus;
import project.demo.exception.ResourceNotFoundException;
import project.demo.exception.ShipmentException;
import project.demo.model.Order;
import project.demo.model.Shipment;
import project.demo.repository.OrderRepository;
import project.demo.repository.ShipmentRepository;
import project.demo.service.IOrderService;
import project.demo.service.IShipmentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ShipmentServiceImpl implements IShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final IOrderService orderService;

    public ShipmentServiceImpl(
            ShipmentRepository shipmentRepository,
            OrderRepository orderRepository,
            IOrderService orderService) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @Override
    public Shipment findById(Integer shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("SHIPMENT_NOT_FOUND",
                        "Shipment not found with ID: " + shipmentId));
    }

    @Override
    public Shipment findByOrderId(Integer orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("SHIPMENT_NOT_FOUND",
                        "Shipment not found for Order ID: " + orderId));
    }

    @Override
    @Transactional
    public Shipment createShipment(Integer orderId, String shippingProvider, String trackingNumber,
            BigDecimal shippingCost) {
        // Check if order exists
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND",
                        "Order not found with ID: " + orderId));

        // Check if shipment already exists for this order
        if (shipmentRepository.findByOrderId(orderId).isPresent()) {
            throw new ShipmentException("SHIPMENT_EXISTS",
                    "Shipment already exists for Order ID: " + orderId);
        }

        // Create new shipment
        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        shipment.setShippingProvider(shippingProvider);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setShippingCost(shippingCost);
        shipment.setShipmentStatus(ShipmentStatus.PENDING.getValue());
        shipment.setShippingDate(LocalDate.now());
        shipment.setEstimatedDeliveryDate(calculateEstimatedDeliveryDate(order.getShippingMethod()));

        // Save shipment
        Shipment savedShipment = shipmentRepository.save(shipment);

        // Update order status
        orderService.updateStatus(orderId, OrderStatus.SHIPPED.getValue());

        return savedShipment;
    }

    @Override
    @Transactional
    public Shipment updateStatus(Integer shipmentId, String status) {
        // Validate status
        validateShipmentStatus(status);

        // Get existing shipment
        Shipment shipment = findById(shipmentId);

        // Check if status transition is valid
        if (!isValidStatusTransition(shipment.getShipmentStatus(), status)) {
            throw new ShipmentException("INVALID_STATUS_TRANSITION",
                    "Invalid status transition from " + shipment.getShipmentStatus() + " to " + status);
        }

        // Update status
        shipment.setShipmentStatus(status);

        // If status is DELIVERED, update delivery date
        if (ShipmentStatus.DELIVERED.getValue().equals(status)) {
            shipment.setDeliveryDate(LocalDate.now());
            // Update order status
            orderService.updateStatus(shipment.getOrderId(), OrderStatus.DELIVERED.getValue());
        }

        return shipmentRepository.save(shipment);
    }

    @Override
    @Transactional
    public Shipment updateTrackingNumber(Integer shipmentId, String trackingNumber) {
        Shipment shipment = findById(shipmentId);
        shipment.setTrackingNumber(trackingNumber);
        return shipmentRepository.save(shipment);
    }

    @Override
    @Transactional
    public Shipment markAsDelivered(Integer shipmentId, LocalDate deliveryDate) {
        Shipment shipment = findById(shipmentId);

        if (ShipmentStatus.DELIVERED.getValue().equals(shipment.getShipmentStatus())) {
            throw new ShipmentException("ALREADY_DELIVERED",
                    "Shipment is already marked as delivered");
        }

        shipment.setShipmentStatus(ShipmentStatus.DELIVERED.getValue());
        shipment.setDeliveryDate(deliveryDate);

        // Update order status
        orderService.updateStatus(shipment.getOrderId(), OrderStatus.DELIVERED.getValue());

        return shipmentRepository.save(shipment);
    }

    @Override
    public BigDecimal calculateShippingCost(String shippingMethod) {
        if (shippingMethod == null) {
            return BigDecimal.ZERO;
        }

        switch (shippingMethod.toLowerCase()) {
            case "standard":
                return new BigDecimal("15.00");
            case "express":
                return new BigDecimal("25.00");
            case "overnight":
                return new BigDecimal("35.00");
            default:
                return new BigDecimal("15.00");
        }
    }

    @Override
    public List<Shipment> findByStatus(String status) {
        validateShipmentStatus(status);
        return shipmentRepository.findByShipmentStatus(status);
    }

    @Override
    @Transactional
    public Shipment updateEstimatedDeliveryDate(Integer shipmentId, LocalDate estimatedDeliveryDate) {
        Shipment shipment = findById(shipmentId);

        if (ShipmentStatus.DELIVERED.getValue().equals(shipment.getShipmentStatus())) {
            throw new ShipmentException("ALREADY_DELIVERED",
                    "Cannot update estimated delivery date for delivered shipment");
        }

        shipment.setEstimatedDeliveryDate(estimatedDeliveryDate);
        return shipmentRepository.save(shipment);
    }

    private void validateShipmentStatus(String status) {
        try {
            ShipmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ShipmentException("INVALID_STATUS",
                    "Invalid shipment status: " + status);
        }
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return true;
        }

        switch (ShipmentStatus.valueOf(currentStatus.toUpperCase())) {
            case PENDING:
                return ShipmentStatus.IN_TRANSIT.getValue().equals(newStatus) ||
                        ShipmentStatus.FAILED.getValue().equals(newStatus);
            case IN_TRANSIT:
                return ShipmentStatus.DELIVERED.getValue().equals(newStatus) ||
                        ShipmentStatus.FAILED.getValue().equals(newStatus);
            case DELIVERED:
                return false; // Cannot transition from DELIVERED
            case FAILED:
                return ShipmentStatus.PENDING.getValue().equals(newStatus);
            default:
                return false;
        }
    }

    private LocalDate calculateEstimatedDeliveryDate(String shippingMethod) {
        LocalDate today = LocalDate.now();

        if (shippingMethod == null) {
            return today.plusDays(5); // Default
        }

        switch (shippingMethod.toLowerCase()) {
            case "standard":
                return today.plusDays(5);
            case "express":
                return today.plusDays(3);
            case "overnight":
                return today.plusDays(1);
            default:
                return today.plusDays(5);
        }
    }
}
