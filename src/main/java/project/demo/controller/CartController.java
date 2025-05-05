package project.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import project.demo.exception.CustomerException;
import project.demo.model.Cart;
import project.demo.model.CartItem;
import project.demo.model.Customer;
import project.demo.model.Product;
import project.demo.service.ICartService;
import project.demo.service.ICustomerService;
import project.demo.service.IProductDetailService;
import project.demo.service.IProductService;

/**
 * Controller for managing shopping cart
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final ICartService cartService;
    private final ICustomerService customerService;
    private final IProductService productService;
    private final IProductDetailService productDetailService; // Added product detail service

    @Autowired
    public CartController(ICartService cartService, ICustomerService customerService, 
                         IProductService productService, IProductDetailService productDetailService) {
        this.cartService = cartService;
        this.customerService = customerService;
        this.productService = productService;
        this.productDetailService = productDetailService; // Initialize product detail service
    }

    /**
     * Display cart contents
     */
    @GetMapping
    public String viewCart(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            // User is not logged in, redirect to login page with message
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to view your cart");
            return "redirect:/auth/login?redirect=cart";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            Cart cart;
            try {
                cart = cartService.getOrCreateCart(customer.getCustomerId());
            } catch (Exception e) {
                // Ghi log chi tiết lỗi giỏ hàng
                System.err.println("Error getting or creating cart: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("errorMessage", "Không thể tạo giỏ hàng: " + e.getMessage());
                return "cart/view";
            }

            List<CartItem> activeItems;
            List<CartItem> savedItems;
            List<Product> recommendations;
            
            try {
                // Get active cart items (not saved for later)
                activeItems = cartService.getActiveCartItems(cart.getCartId());
                
                // Get saved for later items
                savedItems = cartService.getSavedForLaterItems(cart.getCartId());
                
                // Get product recommendations
                recommendations = cartService.getRecommendations(cart.getCartId(), 4);
            } catch (Exception e) {
                // Ghi log chi tiết lỗi khi lấy dữ liệu giỏ hàng
                System.err.println("Error getting cart data: " + e.getMessage());
                e.printStackTrace();
                
                // Khởi tạo danh sách trống để tránh null
                activeItems = new ArrayList<>();
                savedItems = new ArrayList<>();
                recommendations = new ArrayList<>();
                
                model.addAttribute("errorMessage", "Lỗi khi lấy dữ liệu giỏ hàng: " + e.getMessage());
                // Tiếp tục hiển thị trang giỏ hàng nhưng với dữ liệu trống
            }

            // Get product images for recommendations
            Map<Integer, String> productImages = new HashMap<>();
            for (Product product : recommendations) {
                try {
                    if (product.getProductDetails() != null && !product.getProductDetails().isEmpty() &&
                            product.getProductDetails().get(0).getImageUrl() != null) {
                        productImages.put(product.getProductId(), product.getProductDetails().get(0).getImageUrl());
                    }
                } catch (Exception e) {
                    // If product detail not found, continue without image
                }
            }

            // Create a map to store stock quantities for each product
            Map<Integer, Integer> stockQuantities = new HashMap<>();
            for (CartItem item : activeItems) {
                try {
                    int availableStock = productDetailService.getQuantityInStock(item.getProduct().getProductId());
                    stockQuantities.put(item.getProduct().getProductId(), availableStock);
                } catch (Exception e) {
                    // If error occurs, default to 0 stock
                    stockQuantities.put(item.getProduct().getProductId(), 0);
                }
            }

            // Tính toán tổng tiền giỏ hàng
            double cartTotal = 0.0;
            double discountAmount = 0.0;
            double finalTotal = 0.0;
            
            try {
                // Cập nhật tổng giỏ hàng nếu có thể
                cartService.calculateTotal(cart.getCartId());
            } catch (Exception e) {
                System.err.println("Error calculating cart total: " + e.getMessage());
                // Không dừng xử lý, tiếp tục với tổng giỏ hàng được tính thủ công
            }
            
            // Tính tổng giá trị hàng trong giỏ
            for (CartItem item : activeItems) {
                cartTotal += item.getUnitPrice().doubleValue() * item.getQuantity();
            }
            
            // Lấy giảm giá từ cart nếu có
            if (cart.getDiscountAmount() != null) {
                discountAmount = cart.getDiscountAmount().doubleValue();
            }
            
            // Tính tổng cần thanh toán (không bao gồm phí vận chuyển ở trang giỏ hàng)
            finalTotal = cartTotal - discountAmount;
            
            // Truyền các thông tin giỏ hàng vào model
            model.addAttribute("cart", cart);
            model.addAttribute("cartItems", activeItems);
            model.addAttribute("savedItems", savedItems);
            model.addAttribute("recommendations", recommendations);
            model.addAttribute("productImages", productImages);
            model.addAttribute("stockQuantities", stockQuantities); // Add stock quantities to model
            
            // Thêm các biến tổng tiền
            model.addAttribute("cartTotal", cartTotal);
            model.addAttribute("discountAmount", discountAmount);
            model.addAttribute("finalTotal", finalTotal);

            return "cart/view";
        } catch (Exception e) {
            // Ghi log chi tiết lỗi
            System.err.println("Unexpected error in viewCart: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi xử lý giỏ hàng: " + e.getMessage());
            return "cart/view";
        }
    }

    /**
     * Add item to cart
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Integer productId,
            @RequestParam("quantity") Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            // Store the product info in session for later use after login
            session.setAttribute("pendingProductId", productId);
            session.setAttribute("pendingQuantity", quantity);

            // User is not logged in, redirect to login page without message
            return "redirect:/auth/login?redirect=products/" + productId;
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            // Xác thực thông tin customer trước khi thêm vào giỏ hàng
            Integer customerId = customer.getCustomerId();
            if (customerId == null) {
                throw new CustomerException("INVALID_CUSTOMER", "Invalid customer information in session");
            }

            // Tìm hoặc tạo giỏ hàng cho khách hàng
            Cart cart = cartService.getOrCreateCart(customerId);
            
            // Thêm sản phẩm vào giỏ hàng
            cartService.addItem(cart.getCartId(), productId, quantity);

            redirectAttributes.addFlashAttribute("successMessage", "Item added to cart");
            return "redirect:/cart";
        } catch (Exception e) {
            // Log lỗi để debug
            System.err.println("Error adding item to cart: " + e.getMessage());
            e.printStackTrace();
            
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot add item to cart: " + e.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    /**
     * Update item quantity
     */
    @PostMapping("/update")
    public String updateCartItem(@RequestParam("cartItemId") Integer cartItemId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "action", required = false) String action,
            RedirectAttributes redirectAttributes) {
        try {
            // Nếu có tham số action, tăng/giảm số lượng dựa vào giá trị action
            if (action != null) {
                try {
                    // Lấy số lượng hiện tại của cart item
                    CartItem item = cartService.getCartItem(cartItemId);
                    
                    // Tăng hoặc giảm số lượng
                    if ("increase".equals(action)) {
                        quantity = item.getQuantity() + 1;
                    } else if ("decrease".equals(action)) {
                        quantity = item.getQuantity() - 1;
                        // Không cho phép số lượng nhỏ hơn 1
                        if (quantity < 1) {
                            quantity = 1;
                        }
                    }
                } catch (Exception e) {
                    // Log lỗi để debug
                    System.err.println("Error retrieving cart item: " + e.getMessage());
                    e.printStackTrace();
                    throw new Exception("Unable to update quantity: " + e.getMessage());
                }
            }
            
            // Cập nhật số lượng
            if (quantity != null && quantity > 0) {
                cartService.updateItemQuantity(cartItemId, quantity);
            }
            
            return "redirect:/cart";
        } catch (Exception e) {
            // Log lỗi để debug
            System.err.println("Error updating cart item: " + e.getMessage());
            e.printStackTrace();
            
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot update item: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * Remove item from cart
     */
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("cartItemId") Integer cartItemId,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.removeItem(cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * Update item selection status
     */
    @PostMapping("/select")
    public String updateItemSelection(@RequestParam("cartItemId") Integer cartItemId,
            @RequestParam("selected") Boolean selected,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.updateItemSelection(cartItemId, selected);
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * Save an item for later
     */
    @PostMapping("/save-for-later")
    public String saveForLater(@RequestParam("cartItemId") Integer cartItemId,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.saveForLater(cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item saved for later");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * Move a saved item back to the cart
     */
    @PostMapping("/move-to-cart")
    public String moveToCart(@RequestParam("cartItemId") Integer cartItemId,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.moveToCart(cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item moved to cart");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * Apply a promotion code to the cart
     */
    @PostMapping("/apply-promo")
    public String applyPromoCode(@RequestParam("promoCode") String promoCode,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to apply promotion codes");
            return "redirect:/auth/login?redirect=cart";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            Cart cart = cartService.getOrCreateCart(customer.getCustomerId());
            cartService.applyPromoCode(cart.getCartId(), promoCode);

            redirectAttributes.addFlashAttribute("successMessage", "Promotion code applied successfully");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * Clear all items from cart
     */
    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to clear your cart");
            return "redirect:/auth/login?redirect=cart";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            Cart cart = cartService.getOrCreateCart(customer.getCustomerId());
            cartService.clearCart(cart.getCartId());

            redirectAttributes.addFlashAttribute("successMessage", "Cart cleared successfully");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * Proceed to checkout
     */
    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("loginMessage", "Please log in to proceed to checkout");
            return "redirect:/auth/login?redirect=cart";
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                throw new CustomerException("CUSTOMER_NOT_FOUND", "Customer not found in session");
            }

            // Get cart by customer ID and handle the Optional
            Optional<Cart> cartOpt = cartService.findByCustomerId(customer.getCustomerId());
            if (cartOpt.isEmpty()) {
                // Create a new cart if it doesn't exist
                Cart cart = cartService.getOrCreateCart(customer.getCustomerId());
                return "redirect:/checkout";
            }
            
            Cart cart = cartOpt.get();
            cartService.calculateTotal(cart.getCartId()); // Recalculate total

            return "redirect:/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * Get cart item count for mini cart
     */
    @GetMapping("/count")
    @ResponseBody
    public int getCartItemCount(HttpSession session) {
        // Check if user is logged in
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            return 0;
        }

        try {
            // Get customer from session
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                return 0;
            }

            Cart cart = cartService.getOrCreateCart(customer.getCustomerId());
            return cartService.getItemCount(cart.getCartId());
        } catch (Exception e) {
            return 0;
        }
    }
}