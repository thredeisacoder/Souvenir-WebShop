package project.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import project.demo.exception.CustomerException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Address;
import project.demo.model.CardDetails;
import project.demo.model.Cart;
import project.demo.model.CartItem;
import project.demo.model.Customer;
import project.demo.model.Order;
import project.demo.model.OrderDetail;
import project.demo.model.OrderTimelineEvent;
import project.demo.model.Payment;
import project.demo.model.PaymentMethod;
import project.demo.service.IAddressService;
import project.demo.service.ICartService;
import project.demo.service.ICustomerService;
import project.demo.service.IOrderService;
import project.demo.service.IOrderTimelineEventsService;
import project.demo.service.IPaymentMethodService;
import project.demo.service.IPaymentService;
import project.demo.service.IRevenueReportService;
import project.demo.util.ValidationErrorMessages;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private ICartService cartService;

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private IPaymentMethodService paymentMethodService;
    
    @Autowired
    private IPaymentService paymentService;
    
    @Autowired
    private IOrderTimelineEventsService orderTimelineEventsService;

    private final IRevenueReportService revenueReportService;

    @Autowired
    private ValidationErrorMessages validationErrorMessages;

    // Hardcoded shipping fees - would typically come from a database or config
    private static final Map<String, Integer> SHIPPING_FEES = new HashMap<>();
    
    // Hardcoded shipping time estimates
    private static final Map<String, String> SHIPPING_TIMES = new HashMap<>();
    
    // Hardcoded provinces by country - would typically come from a database
    private static final Map<String, List<String>> PROVINCES_BY_COUNTRY = new HashMap<>();
    
    static {
        // Updated shipping fees as per requirements
        SHIPPING_FEES.put("standard", 30000);    // Giao hàng tiết kiệm
        SHIPPING_FEES.put("express", 50000);     // Giao hàng nhanh
        SHIPPING_FEES.put("sameday", 70000);    // Hỏa tốc
        
        // Shipping time estimates
        SHIPPING_TIMES.put("standard", "5-7 ngày");
        SHIPPING_TIMES.put("express", "3-5 ngày");
        SHIPPING_TIMES.put("sameday", "2-4 giờ (trong ngày)");
        
        // Initialize provinces data
        PROVINCES_BY_COUNTRY.put("Vietnam", List.of(
            "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ", 
            "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu",
            "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước",
            "Bình Thuận", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Đắk Nông"
        ));
        
        PROVINCES_BY_COUNTRY.put("United States", List.of(
            "Alabama", "Alaska", "Arizona", "Arkansas", "California",
            "Colorado", "Connecticut", "Delaware", "Florida", "Georgia",
            "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa",
            "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland"
        ));
        
        PROVINCES_BY_COUNTRY.put("Japan", List.of(
            "Hokkaido", "Aomori", "Iwate", "Miyagi", "Akita",
            "Yamagata", "Fukushima", "Ibaraki", "Tochigi", "Gunma",
            "Saitama", "Chiba", "Tokyo", "Kanagawa", "Niigata"
        ));
        
        PROVINCES_BY_COUNTRY.put("China", List.of(
            "Beijing", "Shanghai", "Tianjin", "Chongqing", "Anhui",
            "Fujian", "Gansu", "Guangdong", "Guizhou", "Hainan",
            "Hebei", "Heilongjiang", "Henan", "Hubei", "Hunan"
        ));
        
        PROVINCES_BY_COUNTRY.put("Singapore", List.of("Central Region", "East Region", "North Region", "North-East Region", "West Region"));
    }

    public CheckoutController(ICartService cartService, IOrderService orderService,
            IAddressService addressService, IPaymentService paymentService,
            IRevenueReportService revenueReportService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.addressService = addressService;
        this.paymentService = paymentService;
        this.revenueReportService = revenueReportService;
    }

    // Step 1: Address Selection
    @GetMapping("/address")
    public String showAddressStep(Model model, HttpSession session) {
        Customer customer = getCustomerFromSession(session);
        if (customer == null) {
            return "redirect:/auth/login";
        }

        // Get all addresses for the customer
        List<Address> addresses = addressService.findByCustomerId(customer.getCustomerId());

        // Get available shipping countries (could be from a service/configuration)
        List<String> countries = List.of("Việt Nam", "Mỹ", "Trung Quốc", "Nhật Bản", "Hàn Quốc", "Singapore", "Thái Lan");

        // Get selected address from session if available
        Integer selectedAddressId = (Integer) session.getAttribute("checkoutAddressId");

        model.addAttribute("addresses", addresses);
        model.addAttribute("countries", countries);
        model.addAttribute("customer", customer);
        model.addAttribute("selectedAddressId", selectedAddressId);

        return "checkout/address";
    }

    @PostMapping("/select-address")
    public String selectAddress(@RequestParam("addressId") Integer addressId, HttpSession session, RedirectAttributes redirectAttributes) {
        Customer customer = getCustomerFromSession(session);
        if (customer == null) {
            return "redirect:/auth/login";
        }

        // Validate address belongs to customer
        Address address = addressService.findById(addressId);
        if (address == null || !address.getCustomerId().equals(customer.getCustomerId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Địa chỉ không hợp lệ");
            return "redirect:/checkout/address";
        }

        // Save selected address ID in session
        session.setAttribute("checkoutAddressId", addressId);

        return "redirect:/checkout/shipping";
    }

    @PostMapping("/add-address")
    public String addAddress(@RequestParam(value = "addressLine", required = false) String addressLine,
                            @RequestParam(value = "city", required = false) String city,
                            @RequestParam(value = "country", required = false) String country,
                            @RequestParam(value = "zipCode", required = false) String zipCode,
                            @RequestParam(value = "wardCommune", required = false) String wardCommune,
                            @RequestParam(value = "district", required = false) String district,
                            @RequestParam(value = "streetAddress", required = false) String streetAddress,
                            @RequestParam(value = "isDefault", required = false) Boolean isDefault,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Customer customer = getCustomerFromSession(session);
        if (customer == null) {
            return "redirect:/auth/login";
        }

        try {
            // Validate required fields
            Map<String, String> errors = new HashMap<>();
            
            // Kiểm tra nếu addressLine được cung cấp trực tiếp
            if ((addressLine == null || addressLine.trim().isEmpty()) && 
                (streetAddress == null || streetAddress.trim().isEmpty())) {
                errors.put("address_error", validationErrorMessages.getMessage("INVALID_ADDRESSLINE"));
            }
            
            if (wardCommune == null || wardCommune.trim().isEmpty()) {
                errors.put("wardCommune_error", validationErrorMessages.getMessage("INVALID_WARD_COMMUNE"));
            }
            
            if (district == null || district.trim().isEmpty()) {
                errors.put("district_error", validationErrorMessages.getMessage("INVALID_DISTRICT"));
            }
            
            if (city == null || city.trim().isEmpty()) {
                errors.put("city_error", validationErrorMessages.getMessage("INVALID_PROVINCE_CITY"));
            }
            
            if (country == null || country.trim().isEmpty()) {
                errors.put("country_error", validationErrorMessages.getMessage("INVALID_COUNTRY"));
            }
            
            // Kiểm tra cả mã bưu điện
            if (zipCode == null || zipCode.trim().isEmpty()) {
                errors.put("zipCode_error", validationErrorMessages.getMessage("INVALID_ZIPCODE"));
            }
            
            if (!errors.isEmpty()) {
                for (Map.Entry<String, String> error : errors.entrySet()) {
                    redirectAttributes.addFlashAttribute(error.getKey(), error.getValue());
                }
                return "redirect:/checkout/address";
            }
            
            // Xử lý để tạo addressLine từ các thành phần nếu không được cung cấp trực tiếp
            if (addressLine == null || addressLine.trim().isEmpty()) {
                addressLine = streetAddress + ", " + wardCommune + ", " + district;
            }
            
            // Create new address
            Address address = new Address();
            address.setCustomerId(customer.getCustomerId());
            address.setAddressLine(addressLine);
            address.setCity(city);
            address.setCountry(country);
            address.setZipCode(zipCode);
            address.setIsDefault(isDefault != null && isDefault);

            // Save to database
            Address savedAddress = addressService.save(address);

            // Set as selected address for checkout
            session.setAttribute("checkoutAddressId", savedAddress.getAddressId());

            redirectAttributes.addFlashAttribute("successMessage", "Địa chỉ đã được thêm thành công");
            return "redirect:/checkout/shipping";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout/address";
        }
    }

    // Step 2: Shipping Method
    @GetMapping("/shipping")
    public String showShippingStep(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Customer customer = getCustomerFromSession(session);
        if (customer == null) {
            return "redirect:/auth/login";
        }

        // Check if address was selected
        Integer addressId = (Integer) session.getAttribute("checkoutAddressId");
        if (addressId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn địa chỉ giao hàng trước");
            return "redirect:/checkout/address";
        }

        // Get selected address
        Address shippingAddress = addressService.findById(addressId);
        if (shippingAddress == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Địa chỉ không tồn tại");
            return "redirect:/checkout/address";
        }

        // Get available shipping methods (in a real app, this would come from a service)
        List<Map<String, Object>> shippingMethods = getAvailableShippingMethods(shippingAddress);

        // Get cart items and calculate totals
        Optional<Cart> cartOpt = cartService.findByCustomerId(customer.getCustomerId());
        if (!cartOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giỏ hàng");
            return "redirect:/cart";
        }
        
        Cart cart = cartOpt.get();
        List<CartItem> cartItems = cartService.getCartItems(cart.getCartId());
        
        // Calculate totals
        BigDecimal subtotal = calculateSubtotal(cartItems);
        BigDecimal discount = BigDecimal.ZERO; // Replace with actual discount logic
        
        // Get selected shipping method from session if available
        String selectedShippingMethodId = (String) session.getAttribute("checkoutShippingMethodId");
        BigDecimal shippingCost = null;
        
        if (selectedShippingMethodId != null) {
            for (Map<String, Object> method : shippingMethods) {
                if (method.get("id").equals(selectedShippingMethodId)) {
                    shippingCost = (BigDecimal) method.get("price");
                    break;
                }
            }
        }
        
        BigDecimal total = calculateTotal(subtotal, discount, shippingCost);

        model.addAttribute("customer", customer);
        model.addAttribute("shippingAddress", shippingAddress);
        model.addAttribute("shippingMethods", shippingMethods);
        model.addAttribute("selectedShippingMethodId", selectedShippingMethodId);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("total", total);

        return "checkout/shipping";
    }

    @PostMapping("/select-shipping")
    public String selectShipping(@RequestParam("shippingMethodId") String shippingMethodId, 
                                HttpSession session, 
                                RedirectAttributes redirectAttributes) {
        Customer customer = getCustomerFromSession(session);
        if (customer == null) {
            return "redirect:/auth/login";
        }

        // Validate shipping method exists
        List<Map<String, Object>> shippingMethods = getAvailableShippingMethods(null);
        boolean validMethod = false;
        
        for (Map<String, Object> method : shippingMethods) {
            if (method.get("id").equals(shippingMethodId)) {
                validMethod = true;
                break;
            }
        }
        
        if (!validMethod) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phương thức vận chuyển không hợp lệ");
            return "redirect:/checkout/shipping";
        }

        // Save selected shipping method ID in session
        session.setAttribute("checkoutShippingMethodId", shippingMethodId);

        return "redirect:/checkout/payment";
    }

    // Step 3: Payment Method
    @GetMapping("/payment")
    public String showPaymentStep(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Customer customer = getCustomerFromSession(session);
        if (customer == null) {
            return "redirect:/auth/login";
        }

        // Check if address and shipping method were selected
        Integer addressId = (Integer) session.getAttribute("checkoutAddressId");
        String shippingMethodId = (String) session.getAttribute("checkoutShippingMethodId");
        
        if (addressId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn địa chỉ giao hàng trước");
            return "redirect:/checkout/address";
        }
        
        if (shippingMethodId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn phương thức vận chuyển trước");
            return "redirect:/checkout/shipping";
        }

        // Get selected address and shipping method
        Address shippingAddress = addressService.findById(addressId);
        Map<String, Object> shippingMethod = null;
        
        List<Map<String, Object>> shippingMethods = getAvailableShippingMethods(null);
        for (Map<String, Object> method : shippingMethods) {
            if (method.get("id").equals(shippingMethodId)) {
                shippingMethod = method;
                break;
            }
        }

        // Get cart items and calculate totals
        Optional<Cart> cartOpt = cartService.findByCustomerId(customer.getCustomerId());
        if (!cartOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giỏ hàng");
            return "redirect:/cart";
        }
        
        Cart cart = cartOpt.get();
        List<CartItem> cartItems = cartService.getCartItems(cart.getCartId());
        
        // Calculate totals
        BigDecimal subtotal = calculateSubtotal(cartItems);
        BigDecimal discount = BigDecimal.ZERO; // Replace with actual discount logic
        BigDecimal shippingCost = (BigDecimal) shippingMethod.get("price");
        BigDecimal total = calculateTotal(subtotal, discount, shippingCost);

        // Get selected payment method from session if available
        String selectedPaymentMethodId = (String) session.getAttribute("checkoutPaymentMethodId");

        model.addAttribute("customer", customer);
        model.addAttribute("shippingAddress", shippingAddress);
        model.addAttribute("shippingMethod", shippingMethod);
        model.addAttribute("selectedPaymentMethodId", selectedPaymentMethodId);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("total", total);

        return "checkout/payment";
    }

    @PostMapping("/select-payment")
    public String selectPayment(@RequestParam("paymentMethodId") String paymentMethodId, 
                                @RequestParam(value = "cardNumber", required = false) String cardNumber,
                                @RequestParam(value = "cardFirstName", required = false) String cardFirstName,
                                @RequestParam(value = "cardLastName", required = false) String cardLastName,
                                @RequestParam(value = "cardExpiryDate", required = false) String cardExpiryDate,
                                @RequestParam(value = "cardCVC", required = false) String cardCVC,
                                @RequestParam(value = "saveCard", required = false, defaultValue = "false") Boolean saveCard,
                                HttpSession session, 
                                RedirectAttributes redirectAttributes) {
        Customer customer = getCustomerFromSession(session);
        if (customer == null) {
            return "redirect:/auth/login";
        }

        // Validate payment method is supported
        if (!isValidPaymentMethod(paymentMethodId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phương thức thanh toán không hợp lệ");
            return "redirect:/checkout/payment";
        }

        // Lưu thông tin phương thức thanh toán vào session để sử dụng khi đặt hàng
        try {
            System.out.println("Saving payment method info to session: " + paymentMethodId);
            
            // Kiểm tra và xác thực thông tin dựa vào loại phương thức thanh toán
            if ("credit".equals(paymentMethodId)) {
                // Kiểm tra xem đã nhập đầy đủ thông tin thẻ hay chưa
                if (cardNumber != null && !cardNumber.trim().isEmpty() && 
                    cardFirstName != null && !cardFirstName.trim().isEmpty() &&
                    cardLastName != null && !cardLastName.trim().isEmpty() &&
                    cardExpiryDate != null && !cardExpiryDate.trim().isEmpty() &&
                    cardCVC != null && !cardCVC.trim().isEmpty()) {
                    
                    // Lưu thông tin thẻ tín dụng vào session
                    CardDetails cardDetails = new CardDetails();
                    cardDetails.setCardNumber(cardNumber.replaceAll("\\s+", ""));
                    cardDetails.setFirstName(cardFirstName);
                    cardDetails.setLastName(cardLastName);
                    cardDetails.setExpiryDate(cardExpiryDate);
                    cardDetails.setCvc(cardCVC);
                    cardDetails.setSaveCard(saveCard);
                    
                    // Lưu vào session
                    session.setAttribute("checkoutCardDetails", cardDetails);
                } else {
                    // Thông tin thẻ không đầy đủ
                    redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập đầy đủ thông tin thẻ tín dụng");
                    return "redirect:/checkout/payment";
                }
            }
            
            // Lưu paymentMethodId vào session
            session.setAttribute("checkoutPaymentMethodId", paymentMethodId);
            
            return "redirect:/checkout/confirmation";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xử lý phương thức thanh toán: " + e.getMessage());
            return "redirect:/checkout/payment";
        }
    }

    // Step 4: Order Confirmation
    @GetMapping("/confirmation")
    public String showConfirmationStep(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Customer customer = getCustomerFromSession(session);
        if (customer == null) {
            return "redirect:/auth/login";
        }

        // Check if all previous steps are completed
        Integer addressId = (Integer) session.getAttribute("checkoutAddressId");
        String shippingMethodId = (String) session.getAttribute("checkoutShippingMethodId");
        String paymentMethodId = (String) session.getAttribute("checkoutPaymentMethodId");
        
        if (addressId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn địa chỉ giao hàng trước");
            return "redirect:/checkout/address";
        }
        
        if (shippingMethodId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn phương thức vận chuyển trước");
            return "redirect:/checkout/shipping";
        }
        
        if (paymentMethodId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn phương thức thanh toán trước");
            return "redirect:/checkout/payment";
        }

        // Get selected address and shipping method
        Address shippingAddress = addressService.findById(addressId);
        
        Map<String, Object> shippingMethod = null;
        List<Map<String, Object>> shippingMethods = getAvailableShippingMethods(null);
        for (Map<String, Object> method : shippingMethods) {
            if (method.get("id").equals(shippingMethodId)) {
                shippingMethod = method;
                break;
            }
        }
        
        // Get payment method details
        Map<String, Object> paymentMethod = getPaymentMethodDetails(paymentMethodId);

        // Get cart items and calculate totals
        Optional<Cart> cartOpt = cartService.findByCustomerId(customer.getCustomerId());
        if (!cartOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giỏ hàng");
            return "redirect:/cart";
        }
        
        Cart cart = cartOpt.get();
        List<CartItem> cartItems = cartService.getCartItems(cart.getCartId());
        
        // Calculate totals
        BigDecimal subtotal = calculateSubtotal(cartItems);
        BigDecimal discount = BigDecimal.ZERO; // Replace with actual discount logic
        BigDecimal shippingCost = (BigDecimal) shippingMethod.get("price");
        BigDecimal total = calculateTotal(subtotal, discount, shippingCost);

        model.addAttribute("customer", customer);
        model.addAttribute("shippingAddress", shippingAddress);
        model.addAttribute("shippingMethod", shippingMethod);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("total", total);

        return "checkout/confirmation";
    }

    @PostMapping("/confirm")
    public String confirmOrder(@RequestParam(value = "orderNote", required = false) String orderNote,
                              @RequestParam(value = "termsAccepted", defaultValue = "false") boolean termsAccepted,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        System.out.println("=== Starting confirmOrder method ===");
        
        // Validate terms acceptance
        if (!termsAccepted) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đồng ý với điều khoản dịch vụ để tiếp tục");
            return "redirect:/checkout/confirmation";
        }

        Customer customer = getCustomerFromSession(session);
        if (customer == null) {
            System.out.println("Customer not found in session");
            return "redirect:/auth/login";
        }
        
        System.out.println("Customer ID: " + customer.getCustomerId());

        try {
            // Get checkout data from session
            Integer addressId = (Integer) session.getAttribute("checkoutAddressId");
            String shippingMethodId = (String) session.getAttribute("checkoutShippingMethodId");
            String paymentMethodId = (String) session.getAttribute("checkoutPaymentMethodId");
            
            // Log session data for debugging
            System.out.println("Session data - addressId: " + addressId + 
                              ", shippingMethodId: " + shippingMethodId + 
                              ", paymentMethodId: " + paymentMethodId);
            System.out.println("Order Note: " + orderNote);
            
            // Validate all required data is present
            if (addressId == null || shippingMethodId == null || paymentMethodId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Thông tin thanh toán không đầy đủ");
                return "redirect:/checkout/confirmation";
            }

            // Get shipping method details
            System.out.println("Finding shipping method with ID: " + shippingMethodId);
            Map<String, Object> shippingMethod = null;
            List<Map<String, Object>> shippingMethods = getAvailableShippingMethods(null);
            for (Map<String, Object> method : shippingMethods) {
                if (method.get("id").equals(shippingMethodId)) {
                    shippingMethod = method;
                    break;
                }
            }
            
            if (shippingMethod == null) {
                System.out.println("Invalid shipping method: " + shippingMethodId);
                redirectAttributes.addFlashAttribute("errorMessage", "Phương thức vận chuyển không hợp lệ");
                return "redirect:/checkout/shipping";
            }
            
            System.out.println("Using shipping method: " + shippingMethod.get("name"));
            
            // Lưu thông tin phương thức thanh toán vào bảng PaymentMethod trước khi tạo đơn hàng
            Integer databasePaymentMethodId = null;
            
            // Kiểm tra nếu là thẻ tín dụng và có thông tin thẻ trong session
            CardDetails cardDetails = (CardDetails) session.getAttribute("checkoutCardDetails");
            
            if ("credit".equals(paymentMethodId) && cardDetails != null) {
                // Lưu thông tin thẻ tín dụng
                String lastFourDigits = cardDetails.getCardNumber().substring(Math.max(0, cardDetails.getCardNumber().length() - 4));
                
                // Kiểm tra xem thẻ này đã tồn tại trong cơ sở dữ liệu chưa
                List<PaymentMethod> existingMethods = paymentMethodService.findByCustomerId(customer.getCustomerId());
                boolean cardExists = false;
                
                if (existingMethods != null) {
                    for (PaymentMethod method : existingMethods) {
                        if (method.getMethodName().equals("Credit Card") && 
                            method.getAccountNumber() != null && 
                            method.getAccountNumber().endsWith(lastFourDigits)) {
                            // Thẻ đã tồn tại, sử dụng ID của nó
                            databasePaymentMethodId = method.getPaymentMethodId();
                            cardExists = true;
                            System.out.println("Found existing credit card with ID: " + databasePaymentMethodId);
                            break;
                        }
                    }
                }
                
                // Nếu thẻ chưa tồn tại, luôn lưu thông tin thẻ
                if (!cardExists) {
                    PaymentMethod creditCardMethod = new PaymentMethod();
                    creditCardMethod.setCustomerId(customer.getCustomerId());
                    creditCardMethod.setMethodName("Credit Card");
                    
                    // Xác định nhà cung cấp thẻ dựa vào số đầu thẻ
                    String firstDigit = cardDetails.getCardNumber().substring(0, 1);
                    switch (firstDigit) {
                        case "4":
                            creditCardMethod.setProvider("Visa");
                            break;
                        case "5":
                            creditCardMethod.setProvider("MasterCard");
                            break;
                        default:
                            creditCardMethod.setProvider("Card");
                            break;
                    }
                    
                    // Che giấu số thẻ
                    String maskedCardNumber = "xxxx-xxxx-xxxx-" + lastFourDigits;
                    creditCardMethod.setAccountNumber(maskedCardNumber);
                    
                    // Thêm tên chủ thẻ, 4 số cuối và ngày hết hạn vào mô tả
                    creditCardMethod.setDescription("Thẻ " + creditCardMethod.getProvider() + " - " + 
                                                  cardDetails.getFirstName() + " " + cardDetails.getLastName() + 
                                                  " - " + lastFourDigits + 
                                                  " - Hết hạn: " + cardDetails.getExpiryDate());
                    
                    // Lưu thông tin thẻ
                    creditCardMethod = paymentMethodService.save(creditCardMethod);
                    databasePaymentMethodId = creditCardMethod.getPaymentMethodId();
                    System.out.println("Saved new credit card with ID: " + databasePaymentMethodId);
                }
            } else if ("bank".equals(paymentMethodId)) {
                // Tạo phương thức thanh toán chuyển khoản
                PaymentMethod bankTransferMethod = new PaymentMethod();
                bankTransferMethod.setCustomerId(customer.getCustomerId());
                bankTransferMethod.setMethodName("Chuyển khoản ngân hàng");
                bankTransferMethod.setProvider("Bank");
                bankTransferMethod.setDescription("Thanh toán bằng chuyển khoản ngân hàng");
                
                // Lưu vào cơ sở dữ liệu
                bankTransferMethod = paymentMethodService.save(bankTransferMethod);
                databasePaymentMethodId = bankTransferMethod.getPaymentMethodId();
                System.out.println("Saved bank transfer method with ID: " + databasePaymentMethodId);
            } else if ("momo".equals(paymentMethodId)) {
                // Tạo phương thức thanh toán MoMo
                PaymentMethod momoMethod = new PaymentMethod();
                momoMethod.setCustomerId(customer.getCustomerId());
                momoMethod.setMethodName("Ví điện tử MoMo");
                momoMethod.setProvider("MoMo");
                momoMethod.setDescription("Thanh toán qua ví điện tử MoMo");
                
                // Lưu vào cơ sở dữ liệu
                momoMethod = paymentMethodService.save(momoMethod);
                databasePaymentMethodId = momoMethod.getPaymentMethodId();
                System.out.println("Saved MoMo payment method with ID: " + databasePaymentMethodId);
            } else if ("cod".equals(paymentMethodId)) {
                // Tạo phương thức thanh toán COD
                PaymentMethod codMethod = new PaymentMethod();
                codMethod.setCustomerId(customer.getCustomerId());
                codMethod.setMethodName("Thanh toán khi nhận hàng (COD)");
                codMethod.setProvider("Cash");
                codMethod.setDescription("Thanh toán bằng tiền mặt khi nhận hàng");
                
                // Lưu vào cơ sở dữ liệu
                codMethod = paymentMethodService.save(codMethod);
                databasePaymentMethodId = codMethod.getPaymentMethodId();
                System.out.println("Saved COD payment method with ID: " + databasePaymentMethodId);
            } else {
                // Nếu không phải các loại trên, thử map với ID trong cơ sở dữ liệu
                System.out.println("Getting payment method database ID for: " + paymentMethodId);
                databasePaymentMethodId = getPaymentMethodDatabaseId(paymentMethodId);
                
                if (databasePaymentMethodId == null) {
                    // Fallback to using paymentMethodId as the database ID if it's numeric
                    try {
                        databasePaymentMethodId = Integer.parseInt(paymentMethodId);
                        System.out.println("Parsed paymentMethodId as integer: " + databasePaymentMethodId);
                    } catch (NumberFormatException e) {
                        // Use default payment method (COD) if invalid
                        databasePaymentMethodId = 2; // ID for Cash payment method
                        System.out.println("Using default payment method (COD) instead of invalid: " + paymentMethodId);
                    }
                }
            }
            
            System.out.println("Using payment method ID: " + databasePaymentMethodId + " for frontend ID: " + paymentMethodId);
            
            // Get cart items
            System.out.println("Finding cart for customer: " + customer.getCustomerId());
            Optional<Cart> cartOpt = cartService.findByCustomerId(customer.getCustomerId());
            if (!cartOpt.isPresent()) {
                System.out.println("Cart not found for customer: " + customer.getCustomerId());
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giỏ hàng");
                return "redirect:/cart";
            }
            
            Cart cart = cartOpt.get();
            System.out.println("Found cart with ID: " + cart.getCartId());
            
            List<CartItem> cartItems = cartService.getCartItems(cart.getCartId());
            System.out.println("Cart has " + cartItems.size() + " items");
            
            if (cartItems.isEmpty()) {
                System.out.println("Cart is empty");
                redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn trống");
                return "redirect:/cart";
            }
            
            // Calculate totals
            BigDecimal subtotal = calculateSubtotal(cartItems);
            BigDecimal discount = BigDecimal.ZERO; // Replace with actual discount logic
            BigDecimal shippingCost = (BigDecimal) shippingMethod.get("price");
            BigDecimal total = calculateTotal(subtotal, discount, shippingCost);
            
            System.out.println("Order totals - subtotal: " + subtotal + 
                              ", discount: " + discount + 
                              ", shipping: " + shippingCost + 
                              ", total: " + total);
            
            // Create order - using createFromCart method instead of save
            System.out.println("Creating order from cart: " + cart.getCartId());
            
            Order order = null;
            try {
                order = orderService.createFromCart(
                    cart.getCartId(), 
                    addressId, 
                    databasePaymentMethodId, 
                    shippingMethodId,
                    orderNote
                );
                
                if (order == null) {
                    throw new Exception("Order creation returned null");
                }
                
                // Order created successfully
                System.out.println("Order created successfully with ID: " + order.getOrderId());
                session.setAttribute("orderId", order.getOrderId());

                // Update OrderDetail entries to include the note if provided
                if (orderNote != null && !orderNote.trim().isEmpty()) {
                    System.out.println("Updating OrderDetail entries with note: " + orderNote);
                    List<OrderDetail> orderDetails = orderService.getOrderDetails(order.getOrderId());
                    for (OrderDetail detail : orderDetails) {
                        detail.setNote(orderNote);
                        orderService.updateOrderDetail(detail);
                    }
                    System.out.println("Updated " + orderDetails.size() + " OrderDetail entries with note");
                }
            } catch (Exception e) {
                System.err.println("Error creating order: " + e.getMessage());
                e.printStackTrace();
                // Chỉ gán messageCode, không gán errorMessage nữa
                redirectAttributes.addFlashAttribute("messageCode", "CONNECT_ERROR");
                return "redirect:/checkout/confirmation";
            }
            
            // Set estimated delivery date based on shipping method
            LocalDate estimatedDeliveryDate = LocalDate.now().plusDays(
                    calculateDeliveryDays((String) shippingMethod.get("estimatedDeliveryTime")));
            System.out.println("Estimated delivery date: " + estimatedDeliveryDate);
            
            // Create payment record if not COD
            if (!paymentMethodId.equals("cod")) {
                try {
                    System.out.println("Processing payment for order: " + order.getOrderId());
                    Payment payment = new Payment();
                    payment.setOrderId(order.getOrderId());
                    payment.setPaymentDate(LocalDateTime.now());
                    payment.setAmount(total);
                    payment.setPaymentStatus("pending");
                    payment.setCurrency("VND");
                    payment.setPaymentMethodId(databasePaymentMethodId);
                    
                    // Save note to payment if provided
                    if (orderNote != null && !orderNote.trim().isEmpty()) {
                        payment.setNote(orderNote);
                    }
                    
                    // Process payment instead of just saving
                    paymentService.processPayment(order.getOrderId(), databasePaymentMethodId, total.doubleValue());
                    System.out.println("Payment processed successfully");
                } catch (Exception e) {
                    System.err.println("Error processing payment: " + e.getMessage());
                    e.printStackTrace();
                    // Không throw exception, tiếp tục xử lý để tạo đơn hàng
                }
            }
            
            // Clear cart after successful order creation
            try {
                System.out.println("Clearing cart: " + cart.getCartId());
                cartService.clearCart(cart.getCartId());
                System.out.println("Cart cleared successfully");
            } catch (Exception e) {
                System.err.println("Error clearing cart: " + e.getMessage());
                e.printStackTrace();
                // Không throw exception
            }
            
            // Clear checkout session data
            clearCheckoutSession(session);
            System.out.println("Checkout session data cleared");
            
            // Set success information for confirmation page
            model.addAttribute("orderPlaced", true);
            model.addAttribute("orderId", order.getOrderId());
            // Chỉ gán messageCode, không gán successMessage nữa
            model.addAttribute("messageCode", "POST_SUCCESS");
            
            System.out.println("=== Completed confirmOrder method successfully ===");
            return "checkout/confirmation";
            
        } catch (Exception e) {
            System.err.println("=== Error in confirmOrder method ===");
            e.printStackTrace();
            // Chỉ gán messageCode, không gán errorMessage nữa
            redirectAttributes.addFlashAttribute("messageCode", "CONNECT_ERROR");
            return "redirect:/checkout/confirmation";
        }
    }
    
    // Helper methods
    private Customer getCustomerFromSession(HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            return null;
        }
        Optional<Customer> customerOpt = customerService.findById(customerId);
        return customerOpt.orElse(null);
    }
    
    private List<Map<String, Object>> getAvailableShippingMethods(Address shippingAddress) {
        // In a real application, shipping methods might vary based on the address
        // Here we're returning static methods for simplicity
        List<Map<String, Object>> methods = List.of(
            createShippingMethod("standard", "Giao hàng tiêu chuẩn", "Giao hàng trong 3-5 ngày làm việc", "3-5 ngày làm việc", new BigDecimal("30000")),
            createShippingMethod("express", "Giao hàng nhanh", "Giao hàng trong 1-2 ngày làm việc", "1-2 ngày làm việc", new BigDecimal("50000")),
            createShippingMethod("same_day", "Giao hàng trong ngày", "Chỉ áp dụng cho đơn hàng đặt trước 13:00", "Trong ngày", new BigDecimal("70000"))
        );
        
        return methods;
    }
    
    private Map<String, Object> createShippingMethod(String id, String name, String description, 
                                                     String estimatedDeliveryTime, BigDecimal price) {
        Map<String, Object> method = new HashMap<>();
        method.put("id", id);
        method.put("name", name);
        method.put("description", description);
        method.put("estimatedDeliveryTime", estimatedDeliveryTime);
        method.put("price", price);
        return method;
    }
    
    private BigDecimal calculateSubtotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal discount, BigDecimal shippingCost) {
        BigDecimal total = subtotal;
        
        // Subtract discount if present
        if (discount != null) {
            total = total.subtract(discount);
        }
        
        // Add shipping cost if present
        if (shippingCost != null) {
            total = total.add(shippingCost);
        }
        
        return total;
    }
    
    private boolean isValidPaymentMethod(String paymentMethodId) {
        return paymentMethodId != null && 
               (paymentMethodId.equals("cod") || 
                paymentMethodId.equals("bank") || 
                paymentMethodId.equals("credit") || 
                paymentMethodId.equals("momo"));
    }
    
    private Map<String, Object> getPaymentMethodDetails(String paymentMethodId) {
        Map<String, Object> method = new HashMap<>();
        method.put("id", paymentMethodId);
        
        switch (paymentMethodId) {
            case "cod":
                method.put("name", "Thanh toán khi nhận hàng (COD)");
                method.put("description", "Thanh toán bằng tiền mặt khi nhận hàng");
                break;
            case "bank":
                method.put("name", "Chuyển khoản ngân hàng");
                method.put("description", "Thanh toán bằng chuyển khoản ngân hàng");
                break;
            case "credit":
                method.put("name", "Thẻ tín dụng/Ghi nợ");
                method.put("description", "Thanh toán an toàn bằng thẻ tín dụng hoặc thẻ ghi nợ");
                break;
            case "momo":
                method.put("name", "Ví điện tử MoMo");
                method.put("description", "Thanh toán qua ví điện tử MoMo");
                break;
            default:
                method.put("name", "Phương thức không xác định");
                method.put("description", "");
        }
        
        return method;
    }
    
    private Integer getPaymentMethodDatabaseId(String frontendPaymentMethodId) {
        // Map frontend payment method IDs to database payment method IDs
        // In a real application, you would look this up from the database
        switch (frontendPaymentMethodId) {
            case "cod":
                return 2; // ID for Cash payment method
            case "bank":
                return 3; // ID for Bank transfer method
            case "credit":
                return 1; // ID for Credit card method
            case "momo":
                return 4; // ID for MoMo e-wallet method
            default:
                return null;
        }
    }
    
    private int calculateDeliveryDays(String estimatedDeliveryTime) {
        // Parse the estimated delivery time string to get number of days
        if (estimatedDeliveryTime.contains("3-5")) {
            return 5; // Maximum from range
        } else if (estimatedDeliveryTime.contains("1-2")) {
            return 2; // Maximum from range
        } else if (estimatedDeliveryTime.contains("Trong ngày")) {
            return 1;
        } else {
            return 3; // Default
        }
    }
    
    private void clearCheckoutSession(HttpSession session) {
        session.removeAttribute("checkoutAddressId");
        session.removeAttribute("checkoutShippingMethodId");
        session.removeAttribute("checkoutPaymentMethodId");
    }

    /**
     * Main checkout entry point - redirects to the first step of checkout process
     */
    @GetMapping
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            redirectAttributes.addFlashAttribute("loginMessage", "Vui lòng đăng nhập để tiếp tục thanh toán");
            return "redirect:/auth/login?redirect=checkout";
        }

        try {
            // Get customer information
            Customer customer = getCustomerFromSession(session);
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Không tìm thấy thông tin khách hàng trong phiên làm việc");
            }

            // Check if cart is empty
            Cart cart = cartService.getOrCreateCart(customer.getCustomerId());
            List<CartItem> cartItems = cartService.getActiveCartItems(cart.getCartId());
            
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống");
                return "redirect:/cart";
            }

            // Redirect to first step of checkout process - address selection
            return "redirect:/checkout/address";
            
        } catch (CustomerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/cart";
        }
    }


    /**
     * Validate checkout form inputs
     */
    private Map<String, String> validateCheckoutForm(
            boolean isUsingNewAddress, String address, String wardCommune, 
            String district, String zipcode, String country, String provinceCity,
            String paymentMethod, String cardNumber, String expirationDate, String securityCode) {
        
        Map<String, String> errors = new HashMap<>();
        
        // Check address fields only if using new address
        if (isUsingNewAddress) {
            if (address == null || address.trim().isEmpty()) {
                errors.put("address", validationErrorMessages.getMessage("INVALID_ADDRESSLINE"));
            }
            if (wardCommune == null || wardCommune.trim().isEmpty()) {
                errors.put("wardCommune", validationErrorMessages.getMessage("INVALID_WARD_COMMUNE"));
            }
            if (district == null || district.trim().isEmpty()) {
                errors.put("district", validationErrorMessages.getMessage("INVALID_DISTRICT"));
            }
            if (provinceCity == null || provinceCity.trim().isEmpty()) {
                errors.put("provinceCity", validationErrorMessages.getMessage("INVALID_PROVINCE_CITY"));
            }
            if (country == null || country.trim().isEmpty()) {
                errors.put("country", validationErrorMessages.getMessage("INVALID_COUNTRY"));
            }
            if (zipcode == null || zipcode.trim().isEmpty()) {
                errors.put("zipcode", validationErrorMessages.getMessage("INVALID_ZIPCODE"));
            }
        }
        
        // Validate card data only if using credit card
        if ("creditcard".equals(paymentMethod)) {
            if (cardNumber == null || cardNumber.trim().isEmpty() || cardNumber.length() < 13 || cardNumber.length() > 19) {
                errors.put("cardNumber", validationErrorMessages.getMessage("INVALID_CARD_NUMBER"));
            }
            if (expirationDate == null || expirationDate.trim().isEmpty() || !expirationDate.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
                errors.put("expirationDate", validationErrorMessages.getMessage("INVALID_EXPIRATION_DATE"));
            }
            if (securityCode == null || securityCode.trim().isEmpty() || !securityCode.matches("^[0-9]{3,4}$")) {
                errors.put("securityCode", validationErrorMessages.getMessage("INVALID_SECURITY_CODE"));
            }
        }
        
        return errors;
    }
    
    /**
     * Save card payment method for customer
     */
    private Integer saveCardPaymentMethod(Integer customerId, String cardNumber, 
                                         String expirationDate, String firstName, String lastName) {
        try {
            // Mask card number except last 4 digits
            String maskedCardNumber = "xxxx-xxxx-xxxx-" + cardNumber.substring(cardNumber.length() - 4);
            
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setCustomerId(customerId);
            paymentMethod.setMethodName("Credit Card");
            paymentMethod.setProvider("Card");
            paymentMethod.setAccountNumber(maskedCardNumber);
            paymentMethod.setDescription("Thẻ " + firstName + " " + lastName + " - " + 
                                         cardNumber.substring(cardNumber.length() - 4));
            
            // Save payment method
            paymentMethod = paymentService.addPaymentMethod(paymentMethod);
            return paymentMethod.getPaymentMethodId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Display order confirmation page
     */
    @GetMapping("/confirmation/{orderId}")
    public String orderConfirmation(
            @PathVariable Integer orderId,
            @RequestParam(required = false) String paymentMethod,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Vui lòng đăng nhập để xem xác nhận đơn hàng");
            return "redirect:/auth/login";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Không tìm thấy thông tin khách hàng trong phiên làm việc");
            }

            // Get order
            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                throw new ResourceNotFoundException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng có ID: " + orderId);
            }
            
            Order order = orderOpt.get();

            // Verify order belongs to the current customer
            if (!order.getCustomerId().equals(customer.getCustomerId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập đơn hàng này");
                return "redirect:/account/orders";
            }

            // Get order details
            List<OrderDetail> orderDetails = orderService.getOrderDetails(orderId);

            // Get order timeline events
            List<OrderTimelineEvent> timelineEvents = orderService.getOrderTimeline(orderId);

            // Add data to model
            model.addAttribute("order", order);
            model.addAttribute("orderDetails", orderDetails);
            model.addAttribute("timelineEvents", timelineEvents);
            
            // Add payment method info if provided
            if (paymentMethod != null) {
                model.addAttribute("paymentMethod", paymentMethod);
                
                if ("momo".equals(paymentMethod) || "vnpay".equals(paymentMethod)) {
                    model.addAttribute("redirectToPayment", true);
                    model.addAttribute("paymentProvider", paymentMethod.toUpperCase());
                }
            }

            return "checkout/confirmation";
        } catch (CustomerException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/orders";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/account/orders";
        }
    }
}
