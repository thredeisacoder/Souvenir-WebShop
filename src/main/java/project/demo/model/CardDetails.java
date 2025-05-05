package project.demo.model;

/**
 * Model for credit card details
 * Used temporarily during checkout process
 * Sensitive data is not persisted to database
 */
public class CardDetails {
    private String cardNumber;
    private String expiryDate;
    private String cvc;
    private String firstName;
    private String lastName;
    private Boolean saveCard;
    
    // Constructors
    public CardDetails() {
        // Default constructor
    }
    
    public CardDetails(String cardNumber, String expiryDate, String cvc, String firstName, String lastName) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvc = cvc;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Getters and Setters
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getCvc() {
        return cvc;
    }
    
    public void setCvc(String cvc) {
        this.cvc = cvc;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public Boolean getSaveCard() {
        return saveCard;
    }
    
    public void setSaveCard(Boolean saveCard) {
        this.saveCard = saveCard;
    }
}
