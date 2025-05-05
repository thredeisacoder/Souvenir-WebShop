package project.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import project.demo.exception.CustomerException;
import project.demo.model.Customer;
import project.demo.service.IAddressService;
import project.demo.service.ICustomerService;

/**
 * Controller for handling customer account operations
 */
@Controller
@RequestMapping("/account")
public class CustomerController {

    private final ICustomerService customerService;
    private final IAddressService addressService;

    public CustomerController(ICustomerService customerService, IAddressService addressService) {
        this.customerService = customerService;
        this.addressService = addressService;
    }

    /**
     * Display main account page
     */
    @GetMapping
    public String viewAccount(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to view your account");
            return "redirect:/auth/login?redirect=account";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            model.addAttribute("customer", customer);
            return "account/index";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * Display customer profile page
     */
    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to view your profile");
            return "redirect:/auth/login?redirect=account/profile";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            // Get customer addresses with proper error handling
            var addresses = addressService.findByCustomerId(customer.getCustomerId());
            // Ensure the addresses list is not null even if the service returns null
            if (addresses == null) {
                addresses = java.util.Collections.emptyList();
                System.out.println("Warning: Address service returned null for customer ID: " + customer.getCustomerId());
            } else {
                System.out.println("Found " + addresses.size() + " addresses for customer ID: " + customer.getCustomerId());
            }
            
            model.addAttribute("addresses", addresses);
            model.addAttribute("customer", customer);
            return "account/profile";
        } catch (Exception e) {
            System.err.println("Error in viewProfile: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account";
        }
    }

    /**
     * Update customer profile
     */
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute Customer customer, HttpSession session,
            RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to update your profile");
            return "redirect:/auth/login?redirect=account/profile";
        }

        try {
            // Get customer from session
            Customer currentCustomer = (Customer) session.getAttribute("customer");
            if (currentCustomer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            // Update only allowed fields (prevent ID tampering)
            customer.setCustomerId(currentCustomer.getCustomerId());
            customer.setStatus(currentCustomer.getStatus());

            // Update customer
            customerService.update(customer);

            // Update customer in session
            session.setAttribute("customer", customer);

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
            return "redirect:/account/profile";
        } catch (CustomerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/profile";
        }
    }

    /**
     * Show change password form
     */
    @GetMapping("/profile/change-password")
    public String showChangePasswordForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to change your password");
            return "redirect:/auth/login?redirect=account/profile/change-password";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            model.addAttribute("customer", customer);
            return "account/change-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/profile";
        }
    }

    /**
     * Process change password request
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to change your password");
            return "redirect:/auth/login?redirect=account/profile/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
            return "redirect:/account/profile/change-password";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            customerService.changePassword(customer.getCustomerId(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully");
            return "redirect:/account/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/profile/change-password";
        }
    }
}