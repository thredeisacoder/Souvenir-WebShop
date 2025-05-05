package project.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import project.demo.exception.CustomerException;
import project.demo.exception.OrderException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Customer;
import project.demo.model.Order;
import project.demo.model.OrderDetail;
import project.demo.model.OrderTimelineEvent;
import project.demo.service.ICustomerService;
import project.demo.service.IOrderService;

/**
 * Controller for managing customer orders
 */
@Controller
@RequestMapping("/account/orders")
public class OrderController {

    private final IOrderService orderService;
    private final ICustomerService customerService;

    @Autowired
    public OrderController(IOrderService orderService, ICustomerService customerService) {
        this.orderService = orderService;
        this.customerService = customerService;
    }

    /**
     * Display all orders for the current customer
     */
    @GetMapping
    public String viewOrders(Model model) {
        Customer customer = getCurrentCustomer();
        List<Order> orders = orderService.findByCustomerId(customer.getCustomerId());
        model.addAttribute("orders", orders);
        return "account/orders";
    }

    /**
     * Display details of a specific order
     */
    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable("id") Integer orderId, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Customer customer = getCurrentCustomer();
            Optional<Order> orderOpt = orderService.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                throw new ResourceNotFoundException("Order not found with ID: " + orderId);
            }
            
            Order order = orderOpt.get();

            // Verify order belongs to the current customer
            if (!order.getCustomerId().equals(customer.getCustomerId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied");
                return "redirect:/account/orders";
            }

            List<OrderDetail> orderDetails = orderService.getOrderDetails(orderId);
            List<OrderTimelineEvent> timelineEvents = orderService.getOrderTimeline(orderId);

            model.addAttribute("order", order);
            model.addAttribute("orderDetails", orderDetails);
            model.addAttribute("timelineEvents", timelineEvents);

            return "account/order-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/orders";
        }
    }

    /**
     * Cancel an order
     */
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Integer orderId, RedirectAttributes redirectAttributes) {
        try {
            Customer customer = getCurrentCustomer();
            Optional<Order> orderOpt = orderService.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                throw new ResourceNotFoundException("Order not found with ID: " + orderId);
            }
            
            Order order = orderOpt.get();

            // Verify order belongs to the current customer
            if (!order.getCustomerId().equals(customer.getCustomerId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied");
                return "redirect:/account/orders";
            }

            orderService.cancelOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Order cancelled successfully");
            return "redirect:/account/orders/" + orderId;
        } catch (OrderException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/orders/" + orderId;
        }
    }

    /**
     * Get the current authenticated customer
     */
    private Customer getCurrentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return customerService.findByEmail(email)
                .orElseThrow(() -> new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found"));
    }
}