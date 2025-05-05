package project.demo.service.implement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import project.demo.exception.OrderTimelineEventsException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.OrderTimelineEvent;
import project.demo.repository.OrderRepository;
import project.demo.repository.OrderTimelineEventRepository;

class OrderTimelineEventsServiceImplTest {

    @Mock
    private OrderTimelineEventRepository orderTimelineEventRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderTimelineEventsServiceImpl orderTimelineEventsService;

    private OrderTimelineEvent testEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test event
        testEvent = new OrderTimelineEvent();
        testEvent.setId(1L);
        testEvent.setOrderId(1);
        testEvent.setStatus("pending");
        testEvent.setTimestamp(LocalDateTime.now());
        testEvent.setIcon("fa-shopping-cart");
        testEvent.setIconBackgroundColor("bg-info");
        testEvent.setDescription("Order has been placed successfully.");
    }

    @Test
    void findById_ExistingEvent_ReturnsEvent() {
        // Arrange
        when(orderTimelineEventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act
        OrderTimelineEvent result = orderTimelineEventsService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testEvent.getId(), result.getId());
        verify(orderTimelineEventRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NonExistingEvent_ThrowsException() {
        // Arrange
        when(orderTimelineEventRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderTimelineEventsService.findById(999L));
        verify(orderTimelineEventRepository, times(1)).findById(999L);
    }

    @Test
    void findByOrderId_ExistingOrder_ReturnsEvents() {
        // Arrange
        List<OrderTimelineEvent> events = new ArrayList<>();
        events.add(testEvent);
        when(orderRepository.existsById(1)).thenReturn(true);
        when(orderTimelineEventRepository.findByOrderId(1)).thenReturn(events);

        // Act
        List<OrderTimelineEvent> result = orderTimelineEventsService.findByOrderId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent.getId(), result.get(0).getId());
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, times(1)).findByOrderId(1);
    }

    @Test
    void findByOrderId_NonExistingOrder_ThrowsException() {
        // Arrange
        when(orderRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderTimelineEventsService.findByOrderId(999));
        verify(orderRepository, times(1)).existsById(999);
        verify(orderTimelineEventRepository, never()).findByOrderId(anyInt());
    }

    @Test
    void findByOrderIdOrderByTimestampDesc_ExistingOrder_ReturnsEvents() {
        // Arrange
        List<OrderTimelineEvent> events = new ArrayList<>();
        events.add(testEvent);
        when(orderRepository.existsById(1)).thenReturn(true);
        when(orderTimelineEventRepository.findByOrderIdOrderByTimestampDesc(1)).thenReturn(events);

        // Act
        List<OrderTimelineEvent> result = orderTimelineEventsService.findByOrderIdOrderByTimestampDesc(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent.getId(), result.get(0).getId());
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, times(1)).findByOrderIdOrderByTimestampDesc(1);
    }

    @Test
    void addEvent_ValidEvent_AddsEvent() {
        // Arrange
        when(orderRepository.existsById(1)).thenReturn(true);
        when(orderTimelineEventRepository.save(any(OrderTimelineEvent.class))).thenReturn(testEvent);

        // Act
        OrderTimelineEvent result = orderTimelineEventsService.addEvent(
                1, "pending", "Order has been placed successfully.", 
                "fa-shopping-cart", "bg-info");

        // Assert
        assertNotNull(result);
        assertEquals(testEvent.getId(), result.getId());
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, times(1)).save(any(OrderTimelineEvent.class));
    }

    @Test
    void addEvent_WithTimestamp_AddsEvent() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now().minusHours(1);
        when(orderRepository.existsById(1)).thenReturn(true);
        when(orderTimelineEventRepository.save(any(OrderTimelineEvent.class))).thenReturn(testEvent);

        // Act
        OrderTimelineEvent result = orderTimelineEventsService.addEvent(
                1, "pending", "Order has been placed successfully.", 
                "fa-shopping-cart", "bg-info", timestamp);

        // Assert
        assertNotNull(result);
        assertEquals(testEvent.getId(), result.getId());
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, times(1)).save(any(OrderTimelineEvent.class));
    }

    @Test
    void addEvent_NonExistingOrder_ThrowsException() {
        // Arrange
        when(orderRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderTimelineEventsService.addEvent(
                999, "pending", "Order has been placed successfully.", 
                "fa-shopping-cart", "bg-info"));
        verify(orderRepository, times(1)).existsById(999);
        verify(orderTimelineEventRepository, never()).save(any(OrderTimelineEvent.class));
    }

    @Test
    void addEvent_MissingStatus_ThrowsException() {
        // Arrange
        when(orderRepository.existsById(1)).thenReturn(true);

        // Act & Assert
        assertThrows(OrderTimelineEventsException.class, () -> orderTimelineEventsService.addEvent(
                1, null, "Order has been placed successfully.", 
                "fa-shopping-cart", "bg-info"));
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, never()).save(any(OrderTimelineEvent.class));
    }

    @Test
    void addEvent_MissingDescription_ThrowsException() {
        // Arrange
        when(orderRepository.existsById(1)).thenReturn(true);

        // Act & Assert
        assertThrows(OrderTimelineEventsException.class, () -> orderTimelineEventsService.addEvent(
                1, "pending", null, 
                "fa-shopping-cart", "bg-info"));
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, never()).save(any(OrderTimelineEvent.class));
    }

    @Test
    void save_ValidEvent_SavesEvent() {
        // Arrange
        when(orderRepository.existsById(1)).thenReturn(true);
        when(orderTimelineEventRepository.save(testEvent)).thenReturn(testEvent);

        // Act
        OrderTimelineEvent result = orderTimelineEventsService.save(testEvent);

        // Assert
        assertNotNull(result);
        assertEquals(testEvent.getId(), result.getId());
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, times(1)).save(testEvent);
    }

    @Test
    void save_NullTimestamp_SetsCurrentTimestamp() {
        // Arrange
        testEvent.setTimestamp(null);
        when(orderRepository.existsById(1)).thenReturn(true);
        when(orderTimelineEventRepository.save(testEvent)).thenReturn(testEvent);

        // Act
        OrderTimelineEvent result = orderTimelineEventsService.save(testEvent);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTimestamp());
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, times(1)).save(testEvent);
    }

    @Test
    void save_NonExistingOrder_ThrowsException() {
        // Arrange
        when(orderRepository.existsById(1)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderTimelineEventsService.save(testEvent));
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, never()).save(any(OrderTimelineEvent.class));
    }

    @Test
    void save_MissingStatus_ThrowsException() {
        // Arrange
        testEvent.setStatus((String)null);
        when(orderRepository.existsById(1)).thenReturn(true);
        when(orderTimelineEventRepository.save(testEvent)).thenReturn(testEvent);

        // Act & Assert
        assertThrows(OrderTimelineEventsException.class, () -> orderTimelineEventsService.save(testEvent));
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, never()).save(any(OrderTimelineEvent.class));
    }

    @Test
    void delete_ExistingEvent_DeletesEvent() {
        // Arrange
        when(orderTimelineEventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        doNothing().when(orderTimelineEventRepository).deleteById(1L);

        // Act
        orderTimelineEventsService.delete(1L);

        // Assert
        verify(orderTimelineEventRepository, times(1)).findById(1L);
        verify(orderTimelineEventRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_NonExistingEvent_ThrowsException() {
        // Arrange
        when(orderTimelineEventRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderTimelineEventsService.delete(999L));
        verify(orderTimelineEventRepository, times(1)).findById(999L);
        verify(orderTimelineEventRepository, never()).deleteById(anyLong());
    }

    @Test
    void getLatestEvent_ExistingEvents_ReturnsLatestEvent() {
        // Arrange
        List<OrderTimelineEvent> events = new ArrayList<>();
        events.add(testEvent);
        when(orderRepository.existsById(1)).thenReturn(true);
        when(orderTimelineEventRepository.findFirstByOrderIdOrderByTimestampDesc(1)).thenReturn(events);

        // Act
        OrderTimelineEvent result = orderTimelineEventsService.getLatestEvent(1);

        // Assert
        assertNotNull(result);
        assertEquals(testEvent.getId(), result.getId());
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, times(1)).findFirstByOrderIdOrderByTimestampDesc(1);
    }

    @Test
    void getLatestEvent_NoEvents_ThrowsException() {
        // Arrange
        when(orderRepository.existsById(1)).thenReturn(true);
        when(orderTimelineEventRepository.findFirstByOrderIdOrderByTimestampDesc(1)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderTimelineEventsService.getLatestEvent(1));
        verify(orderRepository, times(1)).existsById(1);
        verify(orderTimelineEventRepository, times(1)).findFirstByOrderIdOrderByTimestampDesc(1);
    }

    @Test
    void getLatestEvent_NonExistingOrder_ThrowsException() {
        // Arrange
        when(orderRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderTimelineEventsService.getLatestEvent(999));
        verify(orderRepository, times(1)).existsById(999);
        verify(orderTimelineEventRepository, never()).findFirstByOrderIdOrderByTimestampDesc(anyInt());
    }
}
