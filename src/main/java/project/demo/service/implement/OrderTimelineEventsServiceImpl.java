package project.demo.service.implement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.demo.exception.OrderTimelineEventsException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.OrderTimelineEvent;
import project.demo.repository.OrderRepository;
import project.demo.repository.OrderTimelineEventRepository;
import project.demo.service.IOrderTimelineEventsService;

/**
 * Implementation of the IOrderTimelineEventsService interface for managing OrderTimelineEvent entities
 */
@Service
public class OrderTimelineEventsServiceImpl implements IOrderTimelineEventsService {

    private final OrderTimelineEventRepository orderTimelineEventRepository;
    private final OrderRepository orderRepository;

    public OrderTimelineEventsServiceImpl(OrderTimelineEventRepository orderTimelineEventRepository,
                                         OrderRepository orderRepository) {
        this.orderTimelineEventRepository = orderTimelineEventRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderTimelineEvent findById(Long id) {
        return orderTimelineEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_TIMELINE_EVENT_NOT_FOUND", 
                        "Order timeline event not found with ID: " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrderTimelineEvent> findByOrderId(Integer orderId) {
        // Check if order exists
        validateOrderExists(orderId);
        
        return orderTimelineEventRepository.findByOrderId(orderId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrderTimelineEvent> findByOrderIdOrderByTimestampDesc(Integer orderId) {
        // Check if order exists
        validateOrderExists(orderId);
        
        return orderTimelineEventRepository.findByOrderIdOrderByTimestampDesc(orderId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderTimelineEvent addEvent(Integer orderId, String status, String description, String icon, String iconBackgroundColor) {
        return addEvent(orderId, status, description, icon, iconBackgroundColor, LocalDateTime.now());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderTimelineEvent addEvent(Integer orderId, String status, String description, String icon, String iconBackgroundColor, LocalDateTime timestamp) {
        // Check if order exists
        validateOrderExists(orderId);
        
        // Validate required fields
        validateRequiredFields(orderId, status, description);
        
        // Create new event
        OrderTimelineEvent event = new OrderTimelineEvent();
        event.setOrderId(orderId);
        event.setStatus(status);
        event.setDescription(description);
        event.setIcon(icon);
        event.setIconBackgroundColor(iconBackgroundColor);
        event.setTimestamp(timestamp != null ? timestamp : LocalDateTime.now());
        
        return orderTimelineEventRepository.save(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderTimelineEvent save(OrderTimelineEvent event) {
        // Validate event
        validateEvent(event);
        
        // Check if order exists
        validateOrderExists(event.getOrderId());
        
        // Set timestamp if not provided
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        
        return orderTimelineEventRepository.save(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(Long id) {
        // Check if event exists
        OrderTimelineEvent event = findById(id);
        
        orderTimelineEventRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderTimelineEvent getLatestEvent(Integer orderId) {
        // Check if order exists
        validateOrderExists(orderId);
        
        List<OrderTimelineEvent> latestEvents = orderTimelineEventRepository.findFirstByOrderIdOrderByTimestampDesc(orderId);
        if (latestEvents.isEmpty()) {
            throw new ResourceNotFoundException("ORDER_TIMELINE_EVENT_NOT_FOUND", 
                    "No timeline events found for order with ID: " + orderId);
        }
        
        return latestEvents.get(0);
    }
    
    /**
     * Validate that an order exists
     * 
     * @param orderId the ID of the order
     * @throws ResourceNotFoundException if the order does not exist
     */
    private void validateOrderExists(Integer orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("ORDER_NOT_FOUND", 
                    "Order not found with ID: " + orderId);
        }
    }
    
    /**
     * Validate required fields for an event
     * 
     * @param orderId the ID of the order
     * @param status the status of the event
     * @param description the description of the event
     * @throws OrderTimelineEventsException if any required field is missing
     */
    private void validateRequiredFields(Integer orderId, String status, String description) {
        if (orderId == null) {
            throw OrderTimelineEventsException.missingRequiredField("Order ID");
        }
        
        if (status == null || status.trim().isEmpty()) {
            throw OrderTimelineEventsException.missingRequiredField("Status");
        }
        
        if (description == null || description.trim().isEmpty()) {
            throw OrderTimelineEventsException.missingRequiredField("Description");
        }
    }
    
    /**
     * Validate an event
     * 
     * @param event the event to validate
     * @throws OrderTimelineEventsException if the event is invalid
     */
    private void validateEvent(OrderTimelineEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        validateRequiredFields(event.getOrderId(), event.getStatus(), event.getDescription());
    }
}
