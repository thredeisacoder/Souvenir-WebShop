package project.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import project.demo.exception.AddressException;
import project.demo.exception.CustomerException;
import project.demo.exception.DuplicateAddressException;
import project.demo.model.Address;
import project.demo.model.Customer;
import project.demo.service.IAddressService;
import project.demo.service.ICustomerService;
import project.demo.util.ValidationErrorMessages;

/**
 * Controller for managing customer addresses
 */
@Controller
@RequestMapping("/account/addresses")
public class AddressController {

    private final IAddressService addressService;
    private final ICustomerService customerService;
    
    @Autowired
    private ValidationErrorMessages validationErrorMessages;

    @Autowired
    public AddressController(IAddressService addressService, ICustomerService customerService) {
        this.addressService = addressService;
        this.customerService = customerService;
    }

    /**
     * Display all addresses for the current customer
     */
    @GetMapping
    public String viewAddresses(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to view your addresses");
            return "redirect:/auth/login?redirect=/account/addresses";
        }

        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            List<Address> addresses = addressService.findByCustomerId(customer.getCustomerId());
            model.addAttribute("addresses", addresses);
            model.addAttribute("customer", customer);
            model.addAttribute("newAddress", new Address());
            return "account/addresses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account";
        }
    }

    /**
     * Process the add address form 
     */
    @PostMapping("/add")
    public String addAddress(@ModelAttribute Address address, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Vui lòng đăng nhập để thêm địa chỉ mới");
            return "redirect:/auth/login?redirect=/account/addresses";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Không tìm thấy thông tin khách hàng trong phiên");
            }

            // Set the customer ID on the address
            address.setCustomerId(customer.getCustomerId());

            // Validate required fields
            Map<String, String> errors = new HashMap<>();
            
            if (address.getAddressLine() == null || address.getAddressLine().trim().isEmpty()) {
                errors.put("addressLine", validationErrorMessages.getMessage("INVALID_ADDRESSLINE"));
            }
            
            if (address.getCity() == null || address.getCity().trim().isEmpty()) {
                errors.put("city", validationErrorMessages.getMessage("INVALID_PROVINCE_CITY"));
            }
            
            if (address.getCountry() == null || address.getCountry().trim().isEmpty()) {
                errors.put("country", validationErrorMessages.getMessage("INVALID_COUNTRY"));
            }
            
            if (!errors.isEmpty()) {
                for (Map.Entry<String, String> error : errors.entrySet()) {
                    redirectAttributes.addFlashAttribute(error.getKey() + "_error", error.getValue());
                }
                return "redirect:/account/addresses";
            }

            // Save the address
            address = addressService.save(address);
            
            // Handle default setting
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                addressService.setAsDefault(address.getAddressId(), customer.getCustomerId());
            }

            redirectAttributes.addFlashAttribute("successMessage", "Địa chỉ đã được thêm thành công");
            return "redirect:/account/addresses";
        } catch (DuplicateAddressException e) {
            // Handle duplicate address specifically
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/addresses";
        } catch (AddressException e) {
            // Handle AddressException specifically to get the proper error message
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/addresses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/addresses";
        }
    }

    /**
     * Process the edit address form
     */
    @PostMapping("/{id}/update")
    public String updateAddress(@PathVariable("id") Integer addressId, @ModelAttribute Address address,
            HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to update your address");
            return "redirect:/auth/login?redirect=/account/addresses";
        }

        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            // Get the existing address to verify ownership
            Address existingAddress = addressService.findById(addressId);

            // Verify address belongs to the current customer
            if (!existingAddress.getCustomerId().equals(customer.getCustomerId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Quyền truy cập bị từ chối");
                return "redirect:/account/addresses";
            }

            // Ensure the ID in path matches the address object and set customer ID
            address.setAddressId(addressId);
            address.setCustomerId(customer.getCustomerId());

            // If this address is being set as default, update it through the service
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                addressService.setAsDefault(addressId, customer.getCustomerId());
            } else if (Boolean.TRUE.equals(existingAddress.getIsDefault())) {
                // If this was the default address and is being unset, don't allow it
                // There must always be a default address if any addresses exist
                List<Address> addresses = addressService.findByCustomerId(customer.getCustomerId());
                if (!addresses.isEmpty()) {
                    address.setIsDefault(true);
                    redirectAttributes.addFlashAttribute("warningMessage",
                            "Ít nhất một địa chỉ phải được đặt làm mặc định");
                }
            }

            addressService.update(address);
            redirectAttributes.addFlashAttribute("successMessage", "Địa chỉ đã được cập nhật thành công");
            return "redirect:/account/addresses";
        } catch (DuplicateAddressException e) {
            // Handle duplicate address specifically
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/addresses";
        } catch (AddressException e) {
            // Handle AddressException specifically to get the proper error message
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/addresses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/addresses";
        }
    }

    /**
     * Delete an address
     */
    @PostMapping("/{id}/delete")
    public String deleteAddress(@PathVariable("id") Integer addressId,
            HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to delete your address");
            return "redirect:/auth/login?redirect=/account/addresses";
        }

        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            // Get address to verify ownership before deleting
            Address address = addressService.findById(addressId);

            if (!address.getCustomerId().equals(customer.getCustomerId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Quyền truy cập bị từ chối");
                return "redirect:/account/addresses";
            }

            // Check if this is the default address
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                // Get all other addresses
                List<Address> addresses = addressService.findByCustomerId(customer.getCustomerId());
                if (addresses.size() > 1) {
                    // Cannot delete the default address if it's not the only one
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Không thể xóa địa chỉ mặc định. Vui lòng đặt một địa chỉ khác làm mặc định trước.");
                    return "redirect:/account/addresses";
                }
            }

            addressService.delete(addressId);
            redirectAttributes.addFlashAttribute("successMessage", "Địa chỉ đã được xóa thành công");
            return "redirect:/account/addresses";
        } catch (AddressException e) {
            // Handle AddressException specifically to get the proper error message
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/addresses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/addresses";
        }
    }

    /**
     * Set an address as default
     */
    @PostMapping("/{id}/default")
    public String setDefaultAddress(@PathVariable("id") Integer addressId,
            HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to update your address");
            return "redirect:/auth/login?redirect=/account/addresses";
        }

        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            // Verify address belongs to the current customer
            Address address = addressService.findById(addressId);
            if (!address.getCustomerId().equals(customer.getCustomerId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Quyền truy cập bị từ chối");
                return "redirect:/account/addresses";
            }

            addressService.setAsDefault(addressId, customer.getCustomerId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật địa chỉ mặc định thành công");
            return "redirect:/account/addresses";
        } catch (AddressException e) {
            // Handle AddressException specifically to get the proper error message
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/addresses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/addresses";
        }
    }
}