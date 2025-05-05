package project.demo.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.demo.enums.DiscountType;
import project.demo.enums.PromotionStatus;
import project.demo.exception.PromotionException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.*;
import project.demo.repository.*;
import project.demo.service.IPromotionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of the IPromotionService interface for managing Promotion entities
 */
@Service
public class PromotionServiceImpl implements IPromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductPromotionRepository productPromotionRepository;
    private final OrderPromotionRepository orderPromotionRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public PromotionServiceImpl(PromotionRepository promotionRepository,
                               ProductPromotionRepository productPromotionRepository,
                               OrderPromotionRepository orderPromotionRepository,
                               ProductRepository productRepository,
                               OrderRepository orderRepository) {
        this.promotionRepository = promotionRepository;
        this.productPromotionRepository = productPromotionRepository;
        this.orderPromotionRepository = orderPromotionRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promotion findById(Integer promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("PROMOTION_NOT_FOUND", 
                        "Promotion not found with ID: " + promotionId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Promotion> findAllActive() {
        return promotionRepository.findActivePromotions(
                PromotionStatus.ACTIVE.getValue(), 
                LocalDate.now());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Promotion save(Promotion promotion) {
        // Validate promotion
        validatePromotion(promotion);
        
        // Set default status if not provided
        if (promotion.getStatus() == null) {
            promotion.setStatus(PromotionStatus.ACTIVE.getValue());
        }
        
        return promotionRepository.save(promotion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Promotion update(Promotion promotion) {
        // Check if promotion exists
        Integer promotionId = promotion.getPromotionId();
        if (promotionId == null) {
            throw PromotionException.missingRequiredField("Promotion ID");
        }
        
        findById(promotionId);
        
        // Validate promotion
        validatePromotion(promotion);
        
        return promotionRepository.save(promotion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(Integer promotionId) {
        // Check if promotion exists
        Promotion promotion = findById(promotionId);
        
        // Check if promotion is used in any product promotions
        List<ProductPromotion> productPromotions = productPromotionRepository.findByPromotionId(promotionId);
        if (!productPromotions.isEmpty()) {
            throw PromotionException.cannotDelete(promotionId, 
                    "Promotion is used in " + productPromotions.size() + " product promotions");
        }
        
        // Check if promotion is used in any order promotions
        List<OrderPromotion> orderPromotions = orderPromotionRepository.findByPromotionId(promotionId);
        if (!orderPromotions.isEmpty()) {
            throw PromotionException.cannotDelete(promotionId, 
                    "Promotion is used in " + orderPromotions.size() + " order promotions");
        }
        
        promotionRepository.deleteById(promotionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProductPromotion> findProductPromotionsByProductId(Integer productId) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                    "Product not found with ID: " + productId);
        }
        
        return productPromotionRepository.findByProductId(productId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductPromotion applyPromotionToProduct(Integer productId, Integer promotionId) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", 
                    "Product not found with ID: " + productId);
        }
        
        // Check if promotion exists
        Promotion promotion = findById(promotionId);
        
        // Check if promotion is active
        if (!PromotionStatus.ACTIVE.getValue().equals(promotion.getStatus())) {
            throw PromotionException.notActive(promotionId);
        }
        
        // Check if promotion is already applied to the product
        List<ProductPromotion> existingPromotions = productPromotionRepository.findByProductIdAndPromotionId(productId, promotionId);
        if (!existingPromotions.isEmpty()) {
            throw PromotionException.duplicateApplication(promotionId, "product", productId);
        }
        
        // Create product promotion
        ProductPromotion productPromotion = new ProductPromotion();
        productPromotion.setProductId(productId);
        productPromotion.setPromotionId(promotionId);
        productPromotion.setStartDate(promotion.getStartDate());
        productPromotion.setEndDate(promotion.getEndDate());
        productPromotion.setStatus(PromotionStatus.ACTIVE.getValue());
        
        return productPromotionRepository.save(productPromotion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void removePromotionFromProduct(Integer productPromotionId) {
        // Check if product promotion exists
        if (!productPromotionRepository.existsById(productPromotionId)) {
            throw new ResourceNotFoundException("PRODUCT_PROMOTION_NOT_FOUND", 
                    "Product promotion not found with ID: " + productPromotionId);
        }
        
        productPromotionRepository.deleteById(productPromotionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderPromotion applyPromotionToOrder(Integer orderId, Integer promotionId) {
        // Check if order exists
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("ORDER_NOT_FOUND", 
                    "Order not found with ID: " + orderId);
        }
        
        // Check if promotion exists
        Promotion promotion = findById(promotionId);
        
        // Check if promotion is active
        if (!PromotionStatus.ACTIVE.getValue().equals(promotion.getStatus())) {
            throw PromotionException.notActive(promotionId);
        }
        
        // Check if promotion is already applied to the order
        List<OrderPromotion> existingPromotions = orderPromotionRepository.findByOrderIdAndPromotionId(orderId, promotionId);
        if (!existingPromotions.isEmpty()) {
            throw PromotionException.duplicateApplication(promotionId, "order", orderId);
        }
        
        // Check if promotion has reached its usage limit
        if (promotion.getUsageLimit() != null) {
            long usageCount = orderPromotionRepository.countByPromotionId(promotionId);
            if (usageCount >= promotion.getUsageLimit()) {
                throw PromotionException.usageLimitReached(promotionId);
            }
        }
        
        // Calculate discount amount
        BigDecimal discountAmount = calculateOrderDiscount(orderId, promotionId);
        
        // Create order promotion
        OrderPromotion orderPromotion = new OrderPromotion();
        orderPromotion.setOrderId(orderId);
        orderPromotion.setPromotionId(promotionId);
        orderPromotion.setDiscountAmount(discountAmount);
        
        return orderPromotionRepository.save(orderPromotion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal calculateOrderDiscount(Integer orderId, Integer promotionId) {
        // Check if order exists
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", 
                        "Order not found with ID: " + orderId));
        
        // Check if promotion exists
        Promotion promotion = findById(promotionId);
        
        // Calculate discount based on discount type
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        if (DiscountType.PERCENTAGE.getValue().equals(promotion.getDiscountType())) {
            // Calculate percentage discount
            BigDecimal percentage = promotion.getDiscountValue();
            discountAmount = order.getTotalAmount()
                    .multiply(percentage.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        } else if (DiscountType.FIXED_AMOUNT.getValue().equals(promotion.getDiscountType())) {
            // Fixed amount discount
            discountAmount = promotion.getDiscountValue();
            
            // Ensure discount doesn't exceed order total
            if (discountAmount.compareTo(order.getTotalAmount()) > 0) {
                discountAmount = order.getTotalAmount();
            }
        } else if (DiscountType.FREE_SHIPPING.getValue().equals(promotion.getDiscountType())) {
            // Free shipping discount
            discountAmount = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        }
        
        return discountAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrderPromotion> findOrderPromotionsByOrderId(Integer orderId) {
        // Check if order exists
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("ORDER_NOT_FOUND", 
                    "Order not found with ID: " + orderId);
        }
        
        return orderPromotionRepository.findByOrderId(orderId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void activatePromotion(Integer promotionId) {
        // Check if promotion exists
        Promotion promotion = findById(promotionId);
        
        // Update status
        promotion.setStatus(PromotionStatus.ACTIVE.getValue());
        promotionRepository.save(promotion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deactivatePromotion(Integer promotionId) {
        // Check if promotion exists
        Promotion promotion = findById(promotionId);
        
        // Update status
        promotion.setStatus(PromotionStatus.INACTIVE.getValue());
        promotionRepository.save(promotion);
    }
    
    /**
     * Validate a promotion
     * 
     * @param promotion the promotion to validate
     * @throws PromotionException if validation fails
     */
    private void validatePromotion(Promotion promotion) {
        if (promotion == null) {
            throw new IllegalArgumentException("Promotion cannot be null");
        }
        
        // Validate required fields
        if (promotion.getPromotionName() == null || promotion.getPromotionName().trim().isEmpty()) {
            throw PromotionException.missingRequiredField("Promotion name");
        }
        
        if (promotion.getStartDate() == null) {
            throw PromotionException.missingRequiredField("Start date");
        }
        
        if (promotion.getDiscountType() == null || promotion.getDiscountType().trim().isEmpty()) {
            throw PromotionException.missingRequiredField("Discount type");
        }
        
        if (promotion.getDiscountValue() == null) {
            throw PromotionException.missingRequiredField("Discount value");
        }
        
        // Validate discount type
        try {
            DiscountType.fromValue(promotion.getDiscountType());
        } catch (IllegalArgumentException e) {
            throw new PromotionException("INVALID_DISCOUNT_TYPE", 
                    "Invalid discount type: " + promotion.getDiscountType());
        }
        
        // Validate discount value
        if (promotion.getDiscountValue().compareTo(BigDecimal.ZERO) < 0) {
            throw PromotionException.invalidDiscountValue(promotion.getDiscountValue().toString());
        }
        
        // Validate percentage discount
        if (DiscountType.PERCENTAGE.getValue().equals(promotion.getDiscountType())) {
            if (promotion.getDiscountValue().compareTo(BigDecimal.ZERO) < 0 || 
                    promotion.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
                throw PromotionException.invalidPercentage(promotion.getDiscountValue().toString());
            }
        }
        
        // Validate date range
        if (promotion.getEndDate() != null && 
                promotion.getEndDate().isBefore(promotion.getStartDate())) {
            throw PromotionException.invalidDateRange(
                    promotion.getStartDate().toString(), 
                    promotion.getEndDate().toString());
        }
        
        // Validate status
        if (promotion.getStatus() != null) {
            try {
                PromotionStatus.fromValue(promotion.getStatus());
            } catch (IllegalArgumentException e) {
                throw new PromotionException("INVALID_STATUS", 
                        "Invalid status: " + promotion.getStatus());
            }
        }
    }
}
