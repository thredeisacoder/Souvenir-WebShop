package project.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import project.demo.exception.CustomerException;
import project.demo.exception.PaymentMethodException;
import project.demo.model.Customer;
import project.demo.model.PaymentMethod;
import project.demo.service.IPaymentMethodService;

/**
 * Controller for managing customer payment methods
 */
@Controller
@RequestMapping("/account/payment-methods")
public class PaymentMethodController {

    private final IPaymentMethodService paymentMethodService;

    public PaymentMethodController(IPaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    /**
     * Display all payment methods for the current customer
     */
    @GetMapping
    public String viewPaymentMethods(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to view your payment methods");
            return "redirect:/auth/login?redirect=account/payment-methods";
        }
        
        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }
            
            List<PaymentMethod> paymentMethods = paymentMethodService.findByCustomerId(customer.getCustomerId());
            model.addAttribute("paymentMethods", paymentMethods);
            return "account/payment-methods";
        } catch (CustomerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session error: " + e.getMessage());
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred");
            return "redirect:/account";
        }
    }

    /**
     * Show form to add a new payment method
     */
    @GetMapping("/add")
    public String showAddPaymentMethodForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to add a payment method");
            return "redirect:/auth/login?redirect=account/payment-methods/add";
        }
        
        PaymentMethod paymentMethod = new PaymentMethod();
        // Set default values
        paymentMethod.setIsDefault(false);
        model.addAttribute("paymentMethod", paymentMethod);
        return "account/payment-methods-form";
    }

    /**
     * Process the add payment method form
     */
    @PostMapping("/add")
    public String addPaymentMethod(@ModelAttribute PaymentMethod paymentMethod, HttpSession session, 
            RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to add a payment method");
            return "redirect:/auth/login?redirect=account/payment-methods/add";
        }
        
        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }
            
            paymentMethod.setCustomerId(customer.getCustomerId());
            
            // If this is the first payment method, set it as default
            List<PaymentMethod> existingPaymentMethods = paymentMethodService.findByCustomerId(customer.getCustomerId());
            if (existingPaymentMethods.isEmpty()) {
                paymentMethod.setIsDefault(true);
            }
            
            paymentMethodService.save(paymentMethod);
            redirectAttributes.addFlashAttribute("successMessage", "Payment method added successfully");
            return "redirect:/account/payment-methods";
        } catch (CustomerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/payment-methods/add";
        } catch (PaymentMethodException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/payment-methods/add";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid payment method details: " + e.getMessage());
            return "redirect:/account/payment-methods/add";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while adding the payment method");
            return "redirect:/account/payment-methods/add";
        }
    }

    /**
     * Show edit payment method form
     */
    @GetMapping("/{id}/edit")
    public String showEditPaymentMethodForm(@PathVariable("id") Integer paymentMethodId, HttpSession session, 
            Model model, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to edit your payment method");
            return "redirect:/auth/login?redirect=account/payment-methods/" + paymentMethodId + "/edit";
        }
        
        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }
            
            PaymentMethod paymentMethod = paymentMethodService.findById(paymentMethodId);

            // Verify payment method belongs to the current customer
            if (!paymentMethod.getCustomerId().equals(customer.getCustomerId())) {
                throw new PaymentMethodException("ACCESS_DENIED", "You do not have permission to edit this payment method");
            }

            model.addAttribute("paymentMethod", paymentMethod);
            return "account/payment-methods-form";
        } catch (CustomerException | PaymentMethodException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/payment-methods";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid payment method ID");
            return "redirect:/account/payment-methods";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred");
            return "redirect:/account/payment-methods";
        }
    }

    /**
     * Process the edit payment method form
     */
    @PostMapping("/{id}/update")
    public String updatePaymentMethod(@PathVariable("id") Integer paymentMethodId, 
            @ModelAttribute PaymentMethod paymentMethod, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to update your payment method");
            return "redirect:/auth/login?redirect=account/payment-methods/" + paymentMethodId + "/edit";
        }
        
        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }
            
            // Get the existing payment method to verify ownership
            PaymentMethod existingPaymentMethod = paymentMethodService.findById(paymentMethodId);
            
            // Verify payment method belongs to the current customer
            if (!existingPaymentMethod.getCustomerId().equals(customer.getCustomerId())) {
                throw new PaymentMethodException("ACCESS_DENIED", "You do not have permission to update this payment method");
            }

            // Ensure the ID in path matches the payment method object and set customer ID
            paymentMethod.setPaymentMethodId(paymentMethodId);
            paymentMethod.setCustomerId(customer.getCustomerId());
            
            // If this payment method is being set as default, update it through the service
            if (paymentMethod.getIsDefault() != null && paymentMethod.getIsDefault()) {
                paymentMethodService.setAsDefault(paymentMethodId, customer.getCustomerId());
            } else if (existingPaymentMethod.getIsDefault() && (paymentMethod.getIsDefault() == null || !paymentMethod.getIsDefault())) {
                // If this was the default payment method and is being unset, don't allow it
                // There must always be a default payment method if any payment methods exist
                List<PaymentMethod> paymentMethods = paymentMethodService.findByCustomerId(customer.getCustomerId());
                if (!paymentMethods.isEmpty()) {
                    paymentMethod.setIsDefault(true);
                    redirectAttributes.addFlashAttribute("warningMessage", "At least one payment method must be set as default");
                }
            }

            paymentMethodService.update(paymentMethod);
            redirectAttributes.addFlashAttribute("successMessage", "Payment method updated successfully");
            return "redirect:/account/payment-methods";
        } catch (CustomerException | PaymentMethodException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/payment-methods/" + paymentMethodId + "/edit";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid payment method details: " + e.getMessage());
            return "redirect:/account/payment-methods/" + paymentMethodId + "/edit";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred");
            return "redirect:/account/payment-methods/" + paymentMethodId + "/edit";
        }
    }

    /**
     * Delete a payment method
     */
    @PostMapping("/{id}/delete")
    public String deletePaymentMethod(@PathVariable("id") Integer paymentMethodId, 
            HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to delete your payment method");
            return "redirect:/auth/login?redirect=account/payment-methods";
        }
        
        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }
            
            // Get payment method to verify ownership before deleting
            PaymentMethod paymentMethod = paymentMethodService.findById(paymentMethodId);

            if (!paymentMethod.getCustomerId().equals(customer.getCustomerId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied");
                return "redirect:/account/payment-methods";
            }
            
            // Check if this is the default payment method
            if (paymentMethod.getIsDefault()) {
                // Get all other payment methods
                List<PaymentMethod> paymentMethods = paymentMethodService.findByCustomerId(customer.getCustomerId());
                if (!paymentMethods.isEmpty()) {
                    // Set another payment method as default
                    for (PaymentMethod pm : paymentMethods) {
                        if (pm.getPaymentMethodId() != paymentMethodId) {
                            paymentMethodService.setAsDefault(pm.getPaymentMethodId(), customer.getCustomerId());
                            break;
                        }
                    }
                } else {
                    // Cannot delete the default payment method if it's the only one
                    redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete the default payment method. Please add another payment method first.");
                    return "redirect:/account/payment-methods";
                }
            }

            paymentMethodService.delete(paymentMethodId);
            redirectAttributes.addFlashAttribute("successMessage", "Payment method deleted successfully");
            return "redirect:/account/payment-methods";
        } catch (CustomerException | PaymentMethodException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/payment-methods";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session error: " + e.getMessage());
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred");
            return "redirect:/account/payment-methods";
        }
    }

    /**
     * Set a payment method as default
     */
    @PostMapping("/{id}/default")
    public String setDefaultPaymentMethod(@PathVariable("id") Integer paymentMethodId, 
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to update your payment method");
            return "redirect:/auth/login?redirect=account/payment-methods";
        }
        
        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }
            
            // Verify payment method belongs to the current customer
            PaymentMethod paymentMethod = paymentMethodService.findById(paymentMethodId);
            if (!paymentMethod.getCustomerId().equals(customer.getCustomerId())) {
                throw new PaymentMethodException("ACCESS_DENIED", "You do not have permission to update this payment method");
            }
            
            paymentMethodService.setAsDefault(paymentMethodId, customer.getCustomerId());
            redirectAttributes.addFlashAttribute("successMessage", "Default payment method updated successfully");
            return "redirect:/account/payment-methods";
        } catch (CustomerException | PaymentMethodException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/payment-methods";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session error: " + e.getMessage());
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while updating the default payment method");
            return "redirect:/account/payment-methods";
        }
    }
}
