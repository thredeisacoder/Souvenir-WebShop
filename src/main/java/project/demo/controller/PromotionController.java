package project.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import project.demo.enums.DiscountType;
import project.demo.enums.PromotionStatus;
import project.demo.model.Promotion;
import project.demo.model.ProductPromotion;
import project.demo.service.IPromotionService;
import project.demo.service.IProductService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/promotions")
public class PromotionController {

    private final IPromotionService promotionService;
    private final IProductService productService;

    @Autowired
    public PromotionController(IPromotionService promotionService, IProductService productService) {
        this.promotionService = promotionService;
        this.productService = productService;
    }

    /**
     * List all active promotions
     */
    @GetMapping
    public String listPromotions(Model model) {
        // Fetch active promotions
        List<Promotion> activePromotions = promotionService.findAllActive();

        // Add promotions to the model
        model.addAttribute("promotions", activePromotions);
        model.addAttribute("today", LocalDate.now());

        // Add discount types for display
        model.addAttribute("discountTypes", DiscountType.values());

        return "promotions/list";
    }

    /**
     * View promotion details
     */
    @GetMapping("/{id}")
    public String viewPromotionDetails(@PathVariable("id") Integer promotionId, Model model) {
        // Fetch promotion by ID
        Promotion promotion = promotionService.findById(promotionId);

        // Get products associated with this promotion
        List<ProductPromotion> productPromotions = promotionService.findProductPromotionsByProductId(promotionId);

        // Create a map of product IDs to product names for display
        Map<Integer, String> productNames = new HashMap<>();
        productPromotions.forEach(pp -> {
            productNames.put(pp.getProductId(),
                    productService.findById(pp.getProductId()).getProductName());
        });

        // Add data to the model
        model.addAttribute("promotion", promotion);
        model.addAttribute("productPromotions", productPromotions);
        model.addAttribute("productNames", productNames);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("discountTypes", DiscountType.values());
        model.addAttribute("promotionStatuses", PromotionStatus.values());

        return "promotions/details";
    }
}
