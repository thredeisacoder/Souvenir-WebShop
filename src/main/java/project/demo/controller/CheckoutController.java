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
                    shippingMethodId,  // Truyền shippingMethodId thay vì tên phương thức
                    orderNote  // Passing orderNote to be saved in Order table
                );
                
                if (order == null) {
                    throw new Exception("Order creation returned null");
                }
                
                System.out.println("Order created successfully with ID: " + order.getOrderId());
                
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
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tạo đơn hàng: " + e.getMessage());
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
            
            // Create initial order timeline event
            try {
                System.out.println("Creating timeline event for order: " + order.getOrderId());
                OrderTimelineEvent timelineEvent = new OrderTimelineEvent();
                timelineEvent.setOrderId(order.getOrderId());
                timelineEvent.setStatus("Order Placed");
                timelineEvent.setTimestamp(LocalDateTime.now());
                timelineEvent.setIcon("fa-shopping-cart");
                timelineEvent.setIconBackgroundColor("bg-info");
                timelineEvent.setDescription("Đơn hàng của bạn đã được đặt thành công.");
                // Không sử dụng trường note trong timeline event
                
                OrderTimelineEvent savedEvent = orderTimelineEventsService.save(timelineEvent);
                System.out.println("Timeline event created with ID: " + savedEvent.getId());
            } catch (Exception e) {
                System.err.println("Error creating timeline event: " + e.getMessage());
                e.printStackTrace();
                // Không throw exception, tiếp tục xử lý để hoàn tất đơn hàng
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
            
            System.out.println("=== Completed confirmOrder method successfully ===");
            return "checkout/confirmation";
            
        } catch (Exception e) {
            System.err.println("=== Error in confirmOrder method ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hoàn tất đơn hàng: " + e.getMessage());
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
     * Process order placement with all required checkout fields
     */
    @PostMapping("/place-order")
    public String placeOrder(
            // Address Information
            @RequestParam(required = false) String savedAddressId,
            @RequestParam(required = false) String useNewAddress,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String wardCommune,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String zipcode,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String provinceCity,
            @RequestParam(required = false, defaultValue = "false") String saveAddress,
            @RequestParam(required = false, defaultValue = "false") String setAsDefault,
            
            // Payment Information
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) String expirationDate,
            @RequestParam(required = false) String securityCode,
            @RequestParam(required = false) String firstNameCardHolder,
            @RequestParam(required = false) String lastNameCardHolder,
            
            // Order Options
            @RequestParam String shippingMethod,
            @RequestParam(required = false) String orderNote,
            
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("Processing order with payment method: " + paymentMethod);
        
        boolean isUsingNewAddress = "true".equals(useNewAddress);
        
        // Validate inputs based on what's required for the selected options
        Map<String, String> validationErrors = validateCheckoutForm(
                isUsingNewAddress, address, wardCommune, district, zipcode, 
                country, provinceCity, paymentMethod, cardNumber, expirationDate, securityCode);
        
        if (!validationErrors.isEmpty()) {
            for (Map.Entry<String, String> error : validationErrors.entrySet()) {
                redirectAttributes.addFlashAttribute(error.getKey() + "_error", error.getValue());
            }
            return "redirect:/checkout";
        }

        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Vui lòng đăng nhập để đặt hàng");
            return "redirect:/auth/login?redirect=checkout";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Không tìm thấy thông tin khách hàng trong phiên làm việc");
            }

            // Get cart
            Cart cart = cartService.getOrCreateCart(customer.getCustomerId());
            
            // Get shipping fee
            Integer shippingFee = SHIPPING_FEES.getOrDefault(shippingMethod, 20000);
            
            // Handle address (either use saved or create new)
            Integer addressId = null;
            
            if (isUsingNewAddress) {
                // Create new address from form data
                Address newAddress = new Address();
                newAddress.setCustomerId(customer.getCustomerId());
                
                // Add district and ward as part of address line
                String fullAddress = address + ", " + wardCommune + ", " + district;
                newAddress.setAddressLine(fullAddress);
                
                newAddress.setCity(provinceCity);
                newAddress.setCountry(country);
                newAddress.setZipCode(zipcode);
                
                // Check if we should save address for later use
                boolean shouldSaveAddress = "true".equals(saveAddress);
                boolean shouldSetAsDefault = "true".equals(setAsDefault);
                
                if (shouldSetAsDefault) {
                    newAddress.setIsDefault(true);
                }
                
                // Save address permanently
                newAddress = addressService.save(newAddress);
                addressId = newAddress.getAddressId();
            } else {
                // Use existing address
                if (savedAddressId != null && !savedAddressId.equals("new")) {
                    try {
                        addressId = Integer.parseInt(savedAddressId);
                        
                        // Verify address exists
                        Address checkAddress = addressService.findById(addressId);
                        if (checkAddress == null) {
                            throw new ResourceNotFoundException("ADDRESS_NOT_FOUND", 
                                "Không tìm thấy địa chỉ có ID: " + addressId);
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("ID địa chỉ không hợp lệ: " + savedAddressId);
                    }
                } else {
                    // If no address selected, throw exception
                    throw new IllegalArgumentException("Vui lòng chọn địa chỉ giao hàng");
                }
            }
            
            // Handle payment method based on selection
            Integer paymentMethodId = null;
            
            if ("creditcard".equals(paymentMethod)) {
                // Save card payment method
                paymentMethodId = saveCardPaymentMethod(customer.getCustomerId(), 
                        cardNumber, expirationDate, firstNameCardHolder, lastNameCardHolder);
                        
                if (paymentMethodId == null) {
                    throw new IllegalArgumentException("Không thể lưu thông tin thẻ tín dụng. Vui lòng thử lại.");
                }
            } else if ("momo".equals(paymentMethod)) {
                // Create MOMO payment method
                PaymentMethod momoPayment = new PaymentMethod();
                momoPayment.setCustomerId(customer.getCustomerId());
                momoPayment.setMethodName("MOMO");
                momoPayment.setProvider("MOMO");
                momoPayment.setDescription("Thanh toán qua ví MOMO");
                
                // Save payment method
                momoPayment = paymentService.addPaymentMethod(momoPayment);
                paymentMethodId = momoPayment.getPaymentMethodId();
            } else if ("vnpay".equals(paymentMethod)) {
                // Create VN PAY payment method
                PaymentMethod vnpayPayment = new PaymentMethod();
                vnpayPayment.setCustomerId(customer.getCustomerId());
                vnpayPayment.setMethodName("VN PAY");
                vnpayPayment.setProvider("VN PAY");
                vnpayPayment.setDescription("Thanh toán qua VN PAY");
                
                // Save payment method
                vnpayPayment = paymentService.addPaymentMethod(vnpayPayment);
                paymentMethodId = vnpayPayment.getPaymentMethodId();
            } else {
                // Try to parse as existing payment method ID
                try {
                    paymentMethodId = Integer.parseInt(paymentMethod);
                    
                    // Verify payment method exists and belongs to customer
                    Integer databasePaymentMethodId = getPaymentMethodDatabaseId(paymentMethod);
                    PaymentMethod checkPaymentMethod = null;
                    List<PaymentMethod> customerPaymentMethods = paymentService.getCustomerPaymentMethods(customer.getCustomerId());
                    if (customerPaymentMethods != null && databasePaymentMethodId != null) {
                        checkPaymentMethod = customerPaymentMethods.stream()
                            .filter(pm -> pm.getPaymentMethodId().equals(databasePaymentMethodId))
                            .findFirst()
                            .orElse(null);
                    }
                    
                    if (checkPaymentMethod == null) {
                        throw new ResourceNotFoundException("PAYMENT_METHOD_NOT_FOUND", 
                            "Không tìm thấy phương thức thanh toán có ID: " + databasePaymentMethodId);
                    }
                    
                    if (!checkPaymentMethod.getCustomerId().equals(customer.getCustomerId())) {
                        throw new IllegalArgumentException("Phương thức thanh toán không thuộc về khách hàng này");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("ID phương thức thanh toán không hợp lệ: " + paymentMethod);
                }
            }
            
            System.out.println("Creating order with: addressId=" + addressId + ", paymentMethodId=" + paymentMethodId + 
                               ", shippingMethod=" + shippingMethod);
            
            try {
                // Create order from cart
                Order order = orderService.createFromCart(cart.getCartId(), addressId, paymentMethodId, 
                        shippingMethod, orderNote);
                
                if (order == null || order.getOrderId() == null) {
                    throw new IllegalStateException("Không thể tạo đơn hàng. Vui lòng thử lại.");
                }
                
                // Calculate final amounts for revenue report
                double subtotal = 0.0;
                
                if (cart.getTotalAmount() != null) {
                    subtotal = cart.getTotalAmount().doubleValue();
                    if (cart.getDiscountAmount() != null) {
                        subtotal -= cart.getDiscountAmount().doubleValue();
                    }
                }
                
                // Ensure non-negative values
                if (subtotal < 0) {
                    subtotal = 0;
                }
                
                double discountAmount = cart.getDiscountAmount() != null ? cart.getDiscountAmount().doubleValue() : 0.0;
                
                // Save shipping data to revenue report
                try {
                    revenueReportService.saveShippingData(order.getOrderId(), subtotal, shippingFee.doubleValue(), 
                            discountAmount);
                } catch (Exception e) {
                    System.err.println("Error saving revenue report data: " + e.getMessage());
                    e.printStackTrace();
                }

                // Clear temporary session attributes
                session.removeAttribute("selectedAddressId");
                session.removeAttribute("selectedPaymentMethod");
                session.removeAttribute("selectedPaymentMethodId");
                session.removeAttribute("selectedShippingMethod");
                session.removeAttribute("creditcardFormActive");
                session.removeAttribute("showNewAddressForm");

                // For MOMO and VN PAY, we would redirect to their payment gateways here
                // For this implementation, we'll just go to the confirmation page
                if ("momo".equals(paymentMethod)) {
                    // In a real implementation, redirect to MOMO payment gateway
                    return "redirect:/checkout/confirmation/" + order.getOrderId() + "?paymentMethod=momo";
                } else if ("vnpay".equals(paymentMethod)) {
                    // In a real implementation, redirect to VN PAY payment gateway
                    return "redirect:/checkout/confirmation/" + order.getOrderId() + "?paymentMethod=vnpay";
                } else {
                    // Normal confirmation for other payment methods
                    return "redirect:/checkout/confirmation/" + order.getOrderId();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Có lỗi khi tạo đơn hàng: " + e.getMessage(), e);
            }
        } catch (CustomerException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout";
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Định dạng không hợp lệ: " + e.getMessage());
            return "redirect:/checkout";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xử lý đơn hàng: " + e.getMessage());
            return "redirect:/checkout";
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

    /**
     * Get provinces for a country (used by form)
     */
    @GetMapping("/provinces")
    public String getProvinces(
            @RequestParam String country,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            List<String> provinces = PROVINCES_BY_COUNTRY.getOrDefault(country, List.of());
            model.addAttribute("provinces", provinces);
            model.addAttribute("selectedCountry", country);
            return "redirect:/checkout?selectedCountry=" + country;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading provinces: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
    
    /**
     * Update shipping method and recalculate totals
     */
    @PostMapping("/update-shipping")
    public String updateShipping(
            @RequestParam String shippingMethod,
            @RequestParam(required = false) String selectedCountry,
            RedirectAttributes redirectAttributes) {
        
        try {
            String redirectUrl = "/checkout?shippingMethod=" + shippingMethod;
            if (selectedCountry != null && !selectedCountry.isEmpty()) {
                redirectUrl += "&selectedCountry=" + selectedCountry;
            }
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating shipping: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    /**
     * Handle address selection during checkout
     */
    @PostMapping("/address-selection")
    public String handleAddressSelection(
            @RequestParam(value = "addressId", required = false) Integer addressId,
            @RequestParam(value = "useNewAddress", required = false) Boolean useNewAddress,
            @RequestParam(value = "address", required = false) String addressLine,
            @RequestParam(value = "wardCommune", required = false) String wardCommune,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "zipcode", required = false) String zipCode,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "provinceCity", required = false) String city,
            @RequestParam(value = "saveAddress", required = false) Boolean saveAddress,
            @RequestParam(value = "setAsDefault", required = false) Boolean setAsDefault,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }
            
            // Store the address selection type in session
            session.setAttribute("showNewAddressForm", Boolean.TRUE.equals(useNewAddress));
            
            // Process existing address selection
            if (addressId != null && !Boolean.TRUE.equals(useNewAddress)) {
                // Store selected address ID in session
                session.setAttribute("selectedAddressId", addressId);
                return "redirect:/checkout";
            }
            
            // Process new address submission
            if (Boolean.TRUE.equals(useNewAddress) && addressLine != null && !addressLine.isEmpty()) {
                // Validate address fields
                if (addressLine == null || addressLine.isEmpty() ||
                    wardCommune == null || wardCommune.isEmpty() ||
                    district == null || district.isEmpty() ||
                    zipCode == null || zipCode.isEmpty() ||
                    country == null || country.isEmpty() ||
                    city == null || city.isEmpty()) {
                    
                    redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin địa chỉ");
                    return "redirect:/checkout";
                }
                
                // Create and save new address
                Address newAddress = new Address();
                newAddress.setCustomerId(customer.getCustomerId());
                
                // Combine address details into address line
                String fullAddress = addressLine + ", " + wardCommune + ", " + district;
                newAddress.setAddressLine(fullAddress);
                newAddress.setZipCode(zipCode);
                newAddress.setCountry(country);
                newAddress.setCity(city);
                
                // Save as default if requested
                if (Boolean.TRUE.equals(setAsDefault)) {
                    newAddress.setIsDefault(true);
                }
                
                // Save address
                Address savedAddress = addressService.save(newAddress);
                
                // Store selected address ID in session
                session.setAttribute("selectedAddressId", savedAddress.getAddressId());
            }
            
            return "redirect:/checkout";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xử lý địa chỉ: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    /**
     * Handle payment method selection
     */
    @PostMapping("/payment-selection")
    public String handlePaymentSelection(
            @RequestParam(value = "paymentMethodId", required = false) String paymentMethodId,
            @RequestParam(value = "creditcardFormActive", required = false) Boolean creditcardFormActive,
            @RequestParam(value = "cardNumber", required = false) String cardNumber,
            @RequestParam(value = "cardExpiryDate", required = false) String cardExpiryDate,
            @RequestParam(value = "cardCVC", required = false) String cardCVC,
            @RequestParam(value = "cardFirstName", required = false) String cardFirstName,
            @RequestParam(value = "cardLastName", required = false) String cardLastName,
            @RequestParam(value = "saveCard", required = false) Boolean saveCard,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }
            
            // Get cart
            Cart cart = cartService.getOrCreateCart(customer.getCustomerId());
            List<CartItem> cartItems = cartService.getActiveCartItems(cart.getCartId());
            
            // Calculate order amounts for display - convert BigDecimal to double safely
            double subtotal = cart.getTotalAmount().doubleValue() - 
                    (cart.getDiscountAmount() != null ? cart.getDiscountAmount().doubleValue() : 0);
            String selectedShippingMethod = (String) session.getAttribute("selectedShippingMethod");
            int shippingFee = SHIPPING_FEES.getOrDefault(selectedShippingMethod != null ? selectedShippingMethod : "standard", 30000);
            double total = subtotal + shippingFee;
            
            // Handle payment method selection logic
            if (paymentMethodId != null) {
                // Store selected payment method in session
                session.setAttribute("selectedPaymentMethod", paymentMethodId);
                
                if ("creditcard".equals(paymentMethodId)) {
                    // Show credit card form
                    model.addAttribute("selectedPaymentMethod", "creditcard");
                    model.addAttribute("creditcardFormActive", true);
                    
                    // If card details were submitted, process them
                    if (cardNumber != null && !cardNumber.isEmpty() && 
                        cardExpiryDate != null && !cardExpiryDate.isEmpty() &&
                        cardCVC != null && !cardCVC.isEmpty() &&
                        cardFirstName != null && !cardFirstName.isEmpty() &&
                        cardLastName != null && !cardLastName.isEmpty()) {
                        
                        // Store card details in session temporarily
                        CardDetails cardDetails = new CardDetails();
                        cardDetails.setCardNumber(cardNumber);
                        cardDetails.setExpiryDate(cardExpiryDate);
                        cardDetails.setCvc(cardCVC);
                        cardDetails.setFirstName(cardFirstName);
                        cardDetails.setLastName(cardLastName);
                        
                        session.setAttribute("cardDetails", cardDetails);
                        
                        // Always save card information when processing a credit card payment
                        // Check if the card with the same last 4 digits already exists
                        boolean cardExists = false;
                        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
                        List<PaymentMethod> existingPaymentMethods = paymentService.getCustomerPaymentMethods(customer.getCustomerId());
                        
                        if (existingPaymentMethods != null) {
                            for (PaymentMethod method : existingPaymentMethods) {
                                if (method.getAccountNumber() != null && 
                                    method.getAccountNumber().endsWith(lastFourDigits)) {
                                    cardExists = true;
                                    break;
                                }
                            }
                        }
                        
                        // Only save if the card doesn't exist or user explicitly requested to save it
                        if (!cardExists || Boolean.TRUE.equals(saveCard)) {
                            // Save card payment method
                            PaymentMethod cardPayment = new PaymentMethod();
                            cardPayment.setCustomerId(customer.getCustomerId());
                            cardPayment.setMethodName("Credit Card");
                            cardPayment.setProvider("Card");
                            
                            // Mask card number except last 4 digits
                            String maskedCardNumber = "xxxx-xxxx-xxxx-" + lastFourDigits;
                            cardPayment.setAccountNumber(maskedCardNumber);
                            cardPayment.setDescription(cardFirstName + " " + cardLastName + "'s card ending in " + lastFourDigits);
                            
                            // Save if this is the first payment method
                            if (existingPaymentMethods == null || existingPaymentMethods.isEmpty()) {
                                cardPayment.setIsDefault(true);
                            } else {
                                cardPayment.setIsDefault(Boolean.TRUE.equals(saveCard)); // Set as default if explicitly requested
                            }
                            
                            try {
                                // Save payment method
                                PaymentMethod savedMethod = paymentService.addPaymentMethod(cardPayment);
                                // Store the payment method ID in session
                                session.setAttribute("savedCardPaymentMethodId", savedMethod.getPaymentMethodId());
                                System.out.println("Successfully saved credit card payment method with ID: " + savedMethod.getPaymentMethodId());
                            } catch (Exception e) {
                                System.err.println("Error saving credit card payment method: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        
                        // Proceed to order review
                        return "redirect:/checkout/review";
                    }
                } else if ("cash".equals(paymentMethodId)) {
                    // Cash on delivery selected, no additional info needed
                    session.setAttribute("selectedPaymentMethod", "cash");
                    
                    // Proceed directly to order review
                    return "redirect:/checkout/review";
                } else {
                    // Existing payment method selected
                    try {
                        Integer paymentMethodIdInt = Integer.parseInt(paymentMethodId);
                        // Validate payment method exists and belongs to customer
                        List<PaymentMethod> customerPaymentMethods = paymentService.getCustomerPaymentMethods(customer.getCustomerId());
                        PaymentMethod paymentMethod = null;
                        if (customerPaymentMethods != null) {
                            paymentMethod = customerPaymentMethods.stream()
                                .filter(pm -> pm.getPaymentMethodId().equals(paymentMethodIdInt))
                                .findFirst()
                                .orElse(null);
                        }
                        
                        if (paymentMethod != null && paymentMethod.getCustomerId().equals(customer.getCustomerId())) {
                            session.setAttribute("selectedPaymentMethodId", paymentMethodIdInt);
                            // Proceed to order review
                            return "redirect:/checkout/review";
                        }
                    } catch (NumberFormatException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Invalid payment method selected");
                        return "redirect:/checkout";
                    }
                }
            }
            
            // Load data for the payment selection page
            List<PaymentMethod> paymentMethods = paymentService.getCustomerPaymentMethods(customer.getCustomerId());
            
            // Add model attributes
            model.addAttribute("cart", cart);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("customer", customer);
            model.addAttribute("paymentMethods", paymentMethods);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("total", total);
            model.addAttribute("selectedPaymentMethod", session.getAttribute("selectedPaymentMethod"));
            model.addAttribute("creditcardFormActive", creditcardFormActive);
            
            // Add card details if they were entered but validation failed
            if (cardNumber != null) {
                CardDetails cardDetails = new CardDetails();
                cardDetails.setCardNumber(cardNumber);
                cardDetails.setExpiryDate(cardExpiryDate);
                cardDetails.setCvc(cardCVC);
                cardDetails.setFirstName(cardFirstName);
                cardDetails.setLastName(cardLastName);
                model.addAttribute("cardDetails", cardDetails);
            } else if (session.getAttribute("cardDetails") != null) {
                model.addAttribute("cardDetails", session.getAttribute("cardDetails"));
            }
            
            return "checkout/payment";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing payment selection: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    /**
     * Handler for shipping method selection
     */
    @PostMapping("/shipping-selection")
    public String handleShippingSelection(
            @RequestParam(value = "shippingMethod", required = false) String shippingMethod,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }
            
            // Get cart
            Cart cart = cartService.getOrCreateCart(customer.getCustomerId());
            
            // Store selected shipping method in session
            if (shippingMethod != null) {
                session.setAttribute("selectedShippingMethod", shippingMethod);
                
                // Calculate new shipping fee based on selection
                int shippingFee = SHIPPING_FEES.getOrDefault(shippingMethod, 30000);
                session.setAttribute("shippingFee", shippingFee);
                
                // Proceed to payment selection
                return "redirect:/checkout/payment";
            }
            
            // If no method was selected (unlikely but possible), redirect back to shipping page
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a shipping method");
            return "redirect:/checkout/shipping";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing shipping selection: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    /**
     * Display order review page (last step before placing order)
     */
    @GetMapping("/review")
    public String reviewOrder(
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to proceed to checkout");
            return "redirect:/auth/login?redirect=checkout/review";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            // Get cart and items
            Cart cart = cartService.getOrCreateCart(customer.getCustomerId());
            List<CartItem> cartItems = cartService.getActiveCartItems(cart.getCartId());

            // Check if cart is empty
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty");
                return "redirect:/cart";
            }

            // Get shipping method from session
            String selectedShippingMethod = (String) session.getAttribute("selectedShippingMethod");
            if (selectedShippingMethod == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select a shipping method");
                return "redirect:/checkout";
            }

            // Get payment method from session
            String selectedPaymentMethod = (String) session.getAttribute("selectedPaymentMethod");
            Integer selectedPaymentMethodId = (Integer) session.getAttribute("selectedPaymentMethodId");
            
            if (selectedPaymentMethod == null && selectedPaymentMethodId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select a payment method");
                return "redirect:/checkout";
            }

            // Get address ID from session
            Integer selectedAddressId = (Integer) session.getAttribute("selectedAddressId");
            if (selectedAddressId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select a shipping address");
                return "redirect:/checkout";
            }

            // Retrieve address - sửa lỗi không dùng orElseThrow vì findById không trả về Optional
            Address shippingAddress = addressService.findById(selectedAddressId);
            if (shippingAddress == null) {
                throw new ResourceNotFoundException("ADDRESS_NOT_FOUND", "Address not found");
            }

            // Verify this address belongs to the customer
            if (!shippingAddress.getCustomerId().equals(customer.getCustomerId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid shipping address");
                return "redirect:/checkout";
            }

            // Get payment method details
            PaymentMethod paymentMethod = null;
            if (selectedPaymentMethodId != null) {
                List<PaymentMethod> customerPaymentMethods = paymentService.getCustomerPaymentMethods(customer.getCustomerId());
                if (customerPaymentMethods != null) {
                    paymentMethod = customerPaymentMethods.stream()
                        .filter(pm -> pm.getPaymentMethodId().equals(selectedPaymentMethodId))
                        .findFirst()
                        .orElse(null);
                }
                
                if (paymentMethod == null || !paymentMethod.getCustomerId().equals(customer.getCustomerId())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Invalid payment method");
                    return "redirect:/checkout";
                }
            }

            // Calculate totals
            int shippingFee = SHIPPING_FEES.getOrDefault(selectedShippingMethod, 30000);
            double subtotal = cart.getTotalAmount().doubleValue() - 
                    (cart.getDiscountAmount() != null ? cart.getDiscountAmount().doubleValue() : 0);
            double total = subtotal + shippingFee;

            // Add data to model
            model.addAttribute("cart", cart);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("customer", customer);
            model.addAttribute("shippingAddress", shippingAddress);
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("selectedPaymentMethod", selectedPaymentMethod);
            model.addAttribute("selectedShippingMethod", selectedShippingMethod);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("total", total);
            model.addAttribute("cardDetails", session.getAttribute("cardDetails"));

            return "checkout/review";
        } catch (CustomerException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
}
