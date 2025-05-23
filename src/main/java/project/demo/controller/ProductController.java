package project.demo.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
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
     * Featured product endpoint - redirects to the product with highest stock
     */
    @GetMapping("/featured")
    public String featuredProduct(Model model) {
        try {
            // Lấy sản phẩm có số lượng tồn kho lớn nhất
            Product product = productService.findProductWithHighestStock();
            
            // Chuyển hướng đến trang chi tiết sản phẩm
            return "redirect:/products/" + product.getProductId();
        } catch (Exception e) {
            // Nếu không tìm thấy sản phẩm nào, điều hướng đến trang danh sách sản phẩm
            model.addAttribute("errorMessage", "Không tìm thấy sản phẩm nổi bật");
            return "redirect:/products";
        }
    }

    /**
     * Display a list of products, optionally filtered by category with pagination
     * using Spring Data's Pageable
     */
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String query, // For search from /products/search
            @RequestParam(required = false) List<String> priceRange, // Khoảng giá để lọc
            @RequestParam(defaultValue = "0") int page, // Spring Data pages are 0-based
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "productName") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        // Xử lý các tham số rỗng
        Integer categoryId = null;
        if (category != null && !category.trim().isEmpty()) {
            try {
                categoryId = Integer.parseInt(category);
            } catch (NumberFormatException e) {
                // Bỏ qua nếu category không phải số hợp lệ
            }
        }
        
        if (query != null && query.trim().isEmpty()) {
            query = null;
        }
        
        if (search != null && search.trim().isEmpty()) {
            search = null;
        }

        // If query parameter is provided (from search form), use it as search term
        if (query != null && !query.trim().isEmpty()) {
            search = query;
        }

        // Create Pageable object with sorting
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Get products based on filters
        Page<Product> productPage;

        try {
            if (categoryId != null) {
                // Filter by category with pagination
                productPage = productService.findByCatalogIdPaginated(categoryId, pageable);
                model.addAttribute("selectedCategory", catalogService.findById(categoryId));
            } else if (search != null && !search.trim().isEmpty()) {
                // Search by name with pagination
                productPage = productService.searchByNamePaginated(search, pageable);
                model.addAttribute("searchTerm", search);
            } else {
                // Get all products with pagination
                productPage = productService.findAllPaginated(pageable);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading products: " + e.getMessage());
            productPage = Page.empty(pageable);
        }

        List<Product> products = productPage.getContent();
        List<Product> filteredProducts = new java.util.ArrayList<>();

        // Get product images, discount prices, and stock information
        Map<Integer, String> productImages = new HashMap<>();
        Map<Integer, BigDecimal> productDiscountPrices = new HashMap<>();
        Map<Integer, Boolean> productStockStatus = new HashMap<>();

        // Chuyển đổi các khoảng giá thành min-max để lọc
        List<PriceRange> priceRanges = new ArrayList<>();
        if (priceRange != null && !priceRange.isEmpty()) {
            for (String range : priceRange) {
                String[] limits = range.split("-");
                if (limits.length == 2) {
                    try {
                        double min = Double.parseDouble(limits[0]);
                        double max = Double.parseDouble(limits[1]);
                        priceRanges.add(new PriceRange(min, max));
                    } catch (NumberFormatException e) {
                        // Bỏ qua nếu không thể chuyển đổi
                    }
                }
            }
        }
        
        // Xử lý trường hợp nhiều khoảng giá được chọn cùng lúc
        double[] effectivePriceRange = processMultiplePriceRanges(priceRanges);

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
                    
                    // Lấy giá hiển thị (giá khuyến mãi nếu có, nếu không thì giá gốc)
                    BigDecimal displayPrice = detail.getDiscountPrice() != null ? 
                                            detail.getDiscountPrice() : 
                                            product.getPrice();
                    
                    // Thêm sản phẩm vào danh sách hiển thị không phụ thuộc tình trạng tồn kho
                    // Chỉ lọc theo khoảng giá nếu có
                    if (effectivePriceRange != null) {
                        double productPrice = displayPrice.doubleValue();
                        
                        // Kiểm tra nếu giá sản phẩm nằm trong khoảng giá hiệu quả
                        if (productPrice >= effectivePriceRange[0] && productPrice <= effectivePriceRange[1]) {
                            filteredProducts.add(product);
                        }
                    } else {
                        // Không có lọc giá, thêm tất cả sản phẩm
                        filteredProducts.add(product);
                    }
                }
            } catch (Exception e) {
                // If product detail not found, still add the product but mark as out of stock
                productStockStatus.put(product.getProductId(), false);
                filteredProducts.add(product);
            }
        }

        // Get all categories for the filter dropdown
        List<Catalog> categories = catalogService.findAllActive();

        // Add data to the model
        model.addAttribute("products", filteredProducts);
        model.addAttribute("productImages", productImages);
        model.addAttribute("productDiscountPrices", productDiscountPrices);
        model.addAttribute("productStockStatus", productStockStatus);
        model.addAttribute("categories", categories);
        
        // Khoảng giá đã chọn
        model.addAttribute("selectedPriceRanges", priceRange);

        // Add pagination data
        model.addAttribute("currentPage", page + 1); // Convert to 1-based for display
        model.addAttribute("totalPages", Math.max(1, productPage.getTotalPages()));
        model.addAttribute("totalProducts", productPage.getTotalElements());
        model.addAttribute("pageSize", size);
        
        // Thêm thông báo nếu không có sản phẩm
        if (filteredProducts.isEmpty()) {
            model.addAttribute("noProductsMessage", "Không tìm thấy sản phẩm nào.");
        }

        // Add sorting data
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDirection", direction);
        model.addAttribute("reverseSortDirection", direction.equals("asc") ? "desc" : "asc");

        return "products/list";
    }
    
    /**
     * Helper method để tạo chuỗi tham số cho khoảng giá
     */
    private String getPriceRangeParams(List<String> priceRanges) {
        if (priceRanges == null || priceRanges.isEmpty()) {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        for (String range : priceRanges) {
            builder.append("&priceRange=").append(range);
        }
        return builder.toString();
    }
    
    /**
     * Inner class để đại diện cho khoảng giá
     */
    private static class PriceRange {
        private final double min;
        private final double max;
        
        public PriceRange(double min, double max) {
            this.min = min;
            this.max = max;
        }
        
        public double getMin() {
            return min;
        }
        
        public double getMax() {
            return max;
        }
    }
    
    /**
     * Xử lý logic lọc khoảng giá
     * Nếu người dùng chọn nhiều khoảng giá, lấy min của khoảng thấp nhất và max của khoảng cao nhất
     */
    private double[] processMultiplePriceRanges(List<PriceRange> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return null;
        }
        
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;
        
        for (PriceRange range : ranges) {
            if (range.getMin() < minPrice) {
                minPrice = range.getMin();
            }
            if (range.getMax() > maxPrice) {
                maxPrice = range.getMax();
            }
        }
        
        return new double[] { minPrice, maxPrice };
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

        // Truyền null cho tham số priceRange (không áp dụng lọc giá cho tìm kiếm)
        // Đảm bảo chuyển query thành String để xử lý đúng
        return listProducts(null, null, query.trim().isEmpty() ? null : query, null, page, size, sort, direction, model);
    }
}
