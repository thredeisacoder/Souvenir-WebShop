package project.demo.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import project.demo.model.Address;
import project.demo.model.Cart;
import project.demo.model.CartItem;
import project.demo.model.Customer;
import project.demo.model.Order;
import project.demo.model.Payment;
import project.demo.model.PaymentMethod;
import project.demo.service.IAddressService;
import project.demo.service.ICartService;
import project.demo.service.ICustomerService;
import project.demo.service.IOrderService;
import project.demo.service.IPaymentService;
import project.demo.service.IRevenueReportService;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final IPaymentService paymentService;
    private final IOrderService orderService;
    private final ICustomerService customerService;
    private final IAddressService addressService;
    private final ICartService cartService;
    private final IRevenueReportService revenueReportService; // Thêm RevenueReportService

    public PaymentController(
            IPaymentService paymentService,
            IOrderService orderService,
            ICustomerService customerService,
            IAddressService addressService,
            ICartService cartService,
            IRevenueReportService revenueReportService) { // Cập nhật constructor
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.customerService = customerService;
        this.addressService = addressService;
        this.cartService = cartService;
        this.revenueReportService = revenueReportService;
    }

    @GetMapping("/methods")
    public String viewPaymentMethods(Model model, HttpSession session) {
        Integer customerId = getCurrentCustomerId(session);

        if (customerId == 0) {
            return "redirect:/login";
        }

        List<PaymentMethod> paymentMethods = paymentService.getCustomerPaymentMethods(customerId);
        model.addAttribute("paymentMethods", paymentMethods);

        return "payment/methods";
    }

    @GetMapping("/add-method")
    public String showAddPaymentMethodForm(Model model) {
        model.addAttribute("paymentMethod", new PaymentMethod());
        return "payment/add-method";
    }

    @PostMapping("/add-method")
    public String addPaymentMethod(@ModelAttribute PaymentMethod paymentMethod,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer customerId = getCurrentCustomerId(session);

        if (customerId == 0) {
            return "redirect:/login";
        }

        try {
            paymentMethod.setCustomerId(customerId);
            paymentService.addPaymentMethod(paymentMethod);
            redirectAttributes.addFlashAttribute("successMessage", "Phương thức thanh toán đã được thêm thành công");
            return "redirect:/payment/methods";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể thêm phương thức thanh toán: " + e.getMessage());
            return "redirect:/payment/add-method";
        }
    }

    @PostMapping("/delete-method/{id}")
    public String deletePaymentMethod(@PathVariable("id") Integer paymentMethodId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer customerId = getCurrentCustomerId(session);

        if (customerId == 0) {
            return "redirect:/login";
        }

        try {
            boolean deleted = paymentService.deletePaymentMethod(paymentMethodId, customerId);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Phương thức thanh toán đã được xóa");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa phương thức thanh toán");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa phương thức thanh toán: " + e.getMessage());
        }

        return "redirect:/payment/methods";
    }

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session) {
        Integer customerId = getCurrentCustomerId(session);

        if (customerId == 0) {
            return "redirect:/login?redirect=/payment/checkout";
        }

        Optional<Customer> customerOpt = customerService.findById(customerId);
        if (customerOpt.isEmpty()) {
            return "redirect:/login";
        }
        Customer customer = customerOpt.get();

        Optional<Cart> cartOpt = cartService.findByCustomerId(customerId);
        if (cartOpt.isEmpty() || cartService.getItems(cartOpt.get().getCartId()).isEmpty()) {
            return "redirect:/cart?error=Cart+is+empty";
        }
        Cart cart = cartOpt.get();
        List<CartItem> cartItems = cartService.getItems(cart.getCartId());

        List<Address> addresses = addressService.findByCustomerId(customerId);
        if (addresses.isEmpty()) {
            return "redirect:/customer/address/add?redirect=/payment/checkout";
        }

        List<PaymentMethod> paymentMethods = paymentService.getCustomerPaymentMethods(customerId);

        BigDecimal subtotal = cartService.getCartTotal(cart.getCartId());
        BigDecimal shippingFee = new BigDecimal("30000");
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(discountAmount);

        model.addAttribute("customer", customer);
        model.addAttribute("addresses", addresses);
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("totalAmount", totalAmount);

        return "payment/checkout";
    }

    @GetMapping("/process")
    public String showPaymentForm(@RequestParam("orderId") Integer orderId, Model model, HttpSession session) {
        Integer customerId = getCurrentCustomerId(session);

        if (customerId == 0) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderService.findById(orderId);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            if (!order.getCustomerId().equals(customerId)) {
                return "redirect:/orders?error=Unauthorized";
            }

            model.addAttribute("order", order);

            if (order.getPaymentMethodId() != null) {
                String paymentUrl = paymentService.createPaymentLink(orderId, order.getPaymentMethodId());
                model.addAttribute("paymentUrl", paymentUrl);
                return "payment/redirect";
            } else {
                List<PaymentMethod> paymentMethods = paymentService.getCustomerPaymentMethods(customerId);
                model.addAttribute("paymentMethods", paymentMethods);
                return "payment/select-method";
            }
        } else {
            return "redirect:/orders?error=Order+not+found";
        }
    }

    @PostMapping("/process")
    public String processPayment(
            @RequestParam(required = false) Integer orderId,
            @RequestParam(required = false) Integer paymentMethodId,
            @RequestParam(required = false) Integer addressId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) String expirationDate,
            @RequestParam(required = false) String securityCode,
            @RequestParam(required = false) String cardholderName,
            @RequestParam(required = false) Boolean saveCard,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer customerId = getCurrentCustomerId(session);
        if (customerId == 0) {
            return "redirect:/login";
        }

        try {
            if (orderId == null) {
                Optional<Cart> cartOpt = cartService.findByCustomerId(customerId);
                if (cartOpt.isEmpty()) {
                    return "redirect:/cart?error=Cart+is+empty";
                }

                if (addressId == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn địa chỉ giao hàng");
                    return "redirect:/payment/checkout";
                }

                if (paymentMethodId != null && paymentMethodId == -1) {
                    if (cardNumber == null || expirationDate == null || securityCode == null || cardholderName == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập đầy đủ thông tin thẻ");
                        return "redirect:/payment/checkout";
                    }

                    PaymentMethod newMethod = new PaymentMethod();
                    newMethod.setMethodName("Thẻ tín dụng/ghi nợ");
                    newMethod.setProvider("Visa/Mastercard");
                    newMethod.setAccountNumber(cardNumber.replaceAll("\\s", ""));
                    newMethod.setDescription("Thẻ của " + cardholderName);
                    newMethod.setCustomerId(customerId);

                    PaymentMethod savedMethod = paymentService.addPaymentMethod(newMethod);
                    paymentMethodId = savedMethod.getPaymentMethodId();
                }

                String shippingMethod = "Giao hàng tiêu chuẩn";
                Order newOrder = orderService.createFromCart(cartOpt.get().getCartId(), addressId, paymentMethodId, shippingMethod);
                orderId = newOrder.getOrderId();
            }

            Optional<Order> orderOpt = orderService.findById(orderId);

            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();

                if (!order.getCustomerId().equals(customerId)) {
                    return "redirect:/orders?error=Unauthorized";
                }

                Integer methodId = paymentMethodId != null ? paymentMethodId : order.getPaymentMethodId();
                methodId = methodId != null ? methodId : 1;

                Payment payment = paymentService.processPayment(orderId, methodId, order.getTotalAmount().doubleValue());

                if ("completed".equals(payment.getPaymentStatus())) {
                    // Lưu thông tin doanh thu khi thanh toán thành công
                    // Lấy giá trị từ đơn hàng
                    double subtotal = order.getTotalAmount().doubleValue();
                    double discountAmount = order.getDiscountAmount() != null ? order.getDiscountAmount().doubleValue() : 0.0;
                    
                    // Tính phí vận chuyển (có thể được lưu trong đơn hàng hoặc mặc định)
                    double shippingFee = order.getShippingFee() != null ? order.getShippingFee().doubleValue() : 30000.0;
                    
                    // Lưu thông tin doanh thu
                    revenueReportService.saveShippingData(orderId, subtotal - discountAmount, shippingFee, discountAmount);

                    redirectAttributes.addFlashAttribute("paymentSuccess", true);
                    redirectAttributes.addFlashAttribute("orderId", orderId);
                    return "redirect:/orders/confirmation";
                } else {
                    return "redirect:/payment/confirm?paymentId=" + payment.getPaymentId();
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng");
                return "redirect:/orders";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xử lý thanh toán: " + e.getMessage());
            return "redirect:/payment/checkout";
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam(required = false) Integer orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "payment/success";
    }

    @PostMapping("/webhook")
    @ResponseBody
    public String handlePaymentWebhook(@RequestParam("transactionId") String transactionId,
            @RequestParam("status") String status,
            @RequestParam("amount") Double amount) {
        try {
            Payment payment = paymentService.confirmPayment(transactionId, status, amount);
            
            // Cập nhật thông tin doanh thu cho thanh toán từ webhook
            if (payment != null && "completed".equals(payment.getPaymentStatus()) && payment.getOrderId() != null) {
                Optional<Order> orderOpt = orderService.findById(payment.getOrderId());
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    double subtotal = order.getTotalAmount().doubleValue();
                    double discountAmount = order.getDiscountAmount() != null ? order.getDiscountAmount().doubleValue() : 0.0;
                    double shippingFee = order.getShippingFee() != null ? order.getShippingFee().doubleValue() : 30000.0;
                    
                    // Lưu thông tin doanh thu khi thanh toán được xác nhận qua webhook
                    revenueReportService.saveShippingData(payment.getOrderId(), subtotal - discountAmount, shippingFee, discountAmount);
                }
            }
            
            return "OK";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private Integer getCurrentCustomerId(HttpSession session) {
        Object user = session.getAttribute("loggedInUser");
        if (user instanceof Customer customer) {
            return customer.getCustomerId();
        }
        return 0;
    }
}