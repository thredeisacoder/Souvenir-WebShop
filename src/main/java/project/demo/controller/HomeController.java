package project.demo.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import project.demo.model.Catalog;
import project.demo.model.Product;
import project.demo.model.Promotion;
import project.demo.service.ICatalogService;
import project.demo.service.IProductService;
import project.demo.service.IPromotionService;

/**
 * Controller for the homepage
 */
@Controller
public class HomeController {

    private final IProductService productService;
    private final ICatalogService catalogService;
    private final IPromotionService promotionService;

    @Autowired
    public HomeController(IProductService productService,
            ICatalogService catalogService,
            IPromotionService promotionService) {
        this.productService = productService;
        this.catalogService = catalogService;
        this.promotionService = promotionService;
    }

    /**
     * Display the homepage with featured products, categories, and promotions
     */
    @GetMapping("/")
    public String home(Model model) {
        try {
            // Add empty data to model to avoid null pointer exceptions
            model.addAttribute("featuredProducts", new ArrayList<Product>());
            model.addAttribute("productImages", new HashMap<Integer, String>());
            model.addAttribute("productDiscountPrices", new HashMap<Integer, BigDecimal>());
            model.addAttribute("productStockStatus", new HashMap<Integer, Boolean>());
            model.addAttribute("categories", new ArrayList<Catalog>());
            model.addAttribute("promotions", new ArrayList<Promotion>());
            
            return "home/index"; // Template at templates/home/index.html
        } catch (Exception e) {
            // Log the error
            System.err.println("Error loading home page: " + e.getMessage());
            e.printStackTrace();
            
            // Return a simple error view
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
