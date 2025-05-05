package project.demo.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import project.demo.model.Catalog;
import project.demo.model.Product;
import project.demo.model.ProductDetail;
import project.demo.service.ICatalogService;
import project.demo.service.IProductService;

/**
 * Controller for product-related operations
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    private final IProductService productService;
    private final ICatalogService catalogService;

    @Autowired
    public ProductController(IProductService productService, ICatalogService catalogService) {
        this.productService = productService;
        this.catalogService = catalogService;
    }

    /**
     * Display a list of products, optionally filtered by category with pagination
     * using Spring Data's Pageable
     */
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String query, // For search from /products/search
            @RequestParam(defaultValue = "0") int page, // Spring Data pages are 0-based
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "productName") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        // If query parameter is provided (from search form), use it as search term
        if (query != null && !query.trim().isEmpty()) {
            search = query;
        }

        // Create Pageable object with sorting
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Get products based on filters
        Page<Product> productPage;

        if (category != null) {
            // Filter by category with pagination
            productPage = productService.findByCatalogIdPaginated(category, pageable);
            model.addAttribute("selectedCategory", catalogService.findById(category));
        } else if (search != null && !search.trim().isEmpty()) {
            // Search by name with pagination
            productPage = productService.searchByNamePaginated(search, pageable);
            model.addAttribute("searchTerm", search);
            // For debugging
            System.out.println("Searching with term: " + search);
            System.out.println("Found " + productPage.getTotalElements() + " products");
        } else {
            // Get all products with pagination
            productPage = productService.findAllPaginated(pageable);
        }

        List<Product> products = productPage.getContent();
        List<Product> inStockProducts = new java.util.ArrayList<>();

        // Get product images, discount prices, and stock information
        Map<Integer, String> productImages = new HashMap<>();
        Map<Integer, BigDecimal> productDiscountPrices = new HashMap<>();
        Map<Integer, Boolean> productStockStatus = new HashMap<>();

        for (Product product : products) {
            try {
                ProductDetail detail = productService.getProductDetail(product.getProductId());
                if (detail != null) {
                    // Store image URL if available
                    if (detail.getImageUrl() != null) {
                        productImages.put(product.getProductId(), detail.getImageUrl());
                    }

                    // Store discount price if available
                    if (detail.getDiscountPrice() != null) {
                        productDiscountPrices.put(product.getProductId(), detail.getDiscountPrice());
                    }

                    // Check stock status (true = in stock, false = out of stock)
                    boolean inStock = detail.getQuantityInStock() != null && detail.getQuantityInStock() > 0;
                    productStockStatus.put(product.getProductId(), inStock);
                    
                    // Only add products with stock to the filtered list
                    if (inStock) {
                        inStockProducts.add(product);
                    }
                }
            } catch (Exception e) {
                // If product detail not found, skip this product
                productStockStatus.put(product.getProductId(), false);
            }
        }

        // Get all categories for the filter dropdown
        List<Catalog> categories = catalogService.findAllActive();

        // Add data to the model - use inStockProducts instead of all products
        model.addAttribute("products", inStockProducts);
        model.addAttribute("productImages", productImages);
        model.addAttribute("productDiscountPrices", productDiscountPrices);
        model.addAttribute("productStockStatus", productStockStatus);
        model.addAttribute("categories", categories);

        // Add pagination data
        model.addAttribute("currentPage", productPage.getNumber() + 1); // Convert to 1-based for display
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalProducts", inStockProducts.size());
        model.addAttribute("pageSize", size);

        // Add sorting data
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDirection", direction);
        model.addAttribute("reverseSortDirection", direction.equals("asc") ? "desc" : "asc");

        return "products/list";
    }

    /**
     * Display product details
     */
    @GetMapping("/{id}")
    public String productDetails(@PathVariable("id") Integer productId, Model model) {
        try {
            // Get product
            Product product = productService.findById(productId);

            // Get product details
            ProductDetail productDetail = null;
            try {
                productDetail = productService.getProductDetail(productId);
            } catch (Exception e) {
                // Product detail not found, continue with null
            }

            // Get related products (products in the same category)
            List<Product> relatedProducts = productService.findByCatalogId(product.getCatalogId());
            relatedProducts.removeIf(p -> p.getProductId().equals(productId)); // Remove current product

            // Limit to 4 related products
            if (relatedProducts.size() > 4) {
                relatedProducts = relatedProducts.subList(0, 4);
            }

            // Get images for related products
            Map<Integer, String> relatedProductImages = new HashMap<>();
            for (Product relatedProduct : relatedProducts) {
                try {
                    ProductDetail detail = productService.getProductDetail(relatedProduct.getProductId());
                    if (detail != null && detail.getImageUrl() != null) {
                        relatedProductImages.put(relatedProduct.getProductId(), detail.getImageUrl());
                    }
                } catch (Exception e) {
                    // If product detail not found, continue without image
                }
            }

            // Add data to the model
            model.addAttribute("product", product);
            model.addAttribute("productDetail", productDetail);
            model.addAttribute("relatedProducts", relatedProducts);
            model.addAttribute("relatedProductImages", relatedProductImages);

            return "products/details";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/products";
        }
    }

    /**
     * Search for products
     */
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "productName") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        // For debugging
        System.out.println("Search endpoint called with query: " + query);

        // Pass the query parameter as the search parameter to listProducts
        return listProducts(null, null, query, page, size, sort, direction, model);
    }
}
