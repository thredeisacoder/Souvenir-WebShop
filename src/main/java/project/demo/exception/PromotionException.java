package project.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an issue with a promotion.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PromotionException extends BusinessLogicException {

    private static final String DEFAULT_ERROR_CODE = "PROMOTION_ERROR";

    /**
     * Constructs a new exception with a default error code and the specified detail
     * message.
     *
     * @param message the detail message
     */
    public PromotionException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * Constructs a new exception with the specified error code and detail message.
     *
     * @param errorCode the error code
     * @param message   the detail message
     */
    public PromotionException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Creates a new exception for an expired promotion.
     *
     * @param promotionCode the promotion code
     * @return a new PromotionException
     */
    public static PromotionException expired(String promotionCode) {
        return new PromotionException(
                "PROMOTION_EXPIRED",
                String.format("Promotion '%s' has expired", promotionCode));
    }

    /**
     * Creates a new exception for an expired promotion by ID.
     *
     * @param promotionId the ID of the promotion
     * @return a new PromotionException
     */
    public static PromotionException expiredById(Integer promotionId) {
        return new PromotionException(
                "PROMOTION_EXPIRED",
                String.format("Promotion with ID %d has expired", promotionId));
    }

    /**
     * Creates a new exception for an invalid promotion.
     *
     * @param promotionCode the promotion code
     * @return a new PromotionException
     */
    public static PromotionException invalid(String promotionCode) {
        return new PromotionException(
                "INVALID_PROMOTION",
                String.format("Promotion '%s' is invalid", promotionCode));
    }

    /**
     * Creates a new exception for a promotion that is not applicable to the current
     * order.
     *
     * @param promotionCode the promotion code
     * @param reason        the reason why the promotion is not applicable
     * @return a new PromotionException
     */
    public static PromotionException notApplicable(String promotionCode, String reason) {
        return new PromotionException(
                "PROMOTION_NOT_APPLICABLE",
                String.format("Promotion '%s' is not applicable: %s", promotionCode, reason));
    }

    /**
     * Creates a new exception for a missing required field.
     *
     * @param fieldName the name of the missing field
     * @return a new PromotionException
     */
    public static PromotionException missingRequiredField(String fieldName) {
        return new PromotionException(
                "MISSING_REQUIRED_FIELD",
                String.format("%s is required", fieldName));
    }

    /**
     * Creates a new exception for an invalid date range.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return a new PromotionException
     */
    public static PromotionException invalidDateRange(String startDate, String endDate) {
        return new PromotionException(
                "INVALID_DATE_RANGE",
                String.format("End date (%s) must be after start date (%s)", endDate, startDate));
    }

    /**
     * Creates a new exception for an invalid discount value.
     *
     * @param discountValue the invalid discount value
     * @return a new PromotionException
     */
    public static PromotionException invalidDiscountValue(String discountValue) {
        return new PromotionException(
                "INVALID_DISCOUNT_VALUE",
                String.format("Invalid discount value: %s", discountValue));
    }

    /**
     * Creates a new exception for an invalid percentage discount.
     *
     * @param percentage the invalid percentage
     * @return a new PromotionException
     */
    public static PromotionException invalidPercentage(String percentage) {
        return new PromotionException(
                "INVALID_PERCENTAGE",
                String.format("Percentage discount must be between 0 and 100, got: %s", percentage));
    }

    /**
     * Creates a new exception for a promotion that cannot be deleted.
     *
     * @param promotionId the ID of the promotion
     * @param reason      the reason why the promotion cannot be deleted
     * @return a new PromotionException
     */
    public static PromotionException cannotDelete(Integer promotionId, String reason) {
        return new PromotionException(
                "CANNOT_DELETE_PROMOTION",
                String.format("Cannot delete promotion with ID %d: %s", promotionId, reason));
    }

    /**
     * Creates a new exception for a promotion that has reached its usage limit.
     *
     * @param promotionId the ID of the promotion
     * @return a new PromotionException
     */
    public static PromotionException usageLimitReached(Integer promotionId) {
        return new PromotionException(
                "USAGE_LIMIT_REACHED",
                String.format("Promotion with ID %d has reached its usage limit", promotionId));
    }

    /**
     * Creates a new exception for a promotion that is not active.
     *
     * @param promotionId the ID of the promotion
     * @return a new PromotionException
     */
    public static PromotionException notActive(Integer promotionId) {
        return new PromotionException(
                "PROMOTION_NOT_ACTIVE",
                String.format("Promotion with ID %d is not active", promotionId));
    }

    /**
     * Creates a new exception for a promotion that has not started yet.
     *
     * @param promotionId the ID of the promotion
     * @param startDate   the start date
     * @return a new PromotionException
     */
    public static PromotionException notStarted(Integer promotionId, String startDate) {
        return new PromotionException(
                "PROMOTION_NOT_STARTED",
                String.format("Promotion with ID %d has not started yet. Start date: %s", promotionId, startDate));
    }

    /**
     * Creates a new exception for a duplicate promotion application.
     *
     * @param promotionId the ID of the promotion
     * @param entityType  the type of entity (e.g., "product", "order")
     * @param entityId    the ID of the entity
     * @return a new PromotionException
     */
    public static PromotionException duplicateApplication(Integer promotionId, String entityType, Integer entityId) {
        return new PromotionException(
                "DUPLICATE_PROMOTION_APPLICATION",
                String.format("Promotion with ID %d is already applied to %s with ID %d", promotionId, entityType,
                        entityId));
    }
}
