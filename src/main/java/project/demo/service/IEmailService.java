package project.demo.service;

/**
 * Service interface for email operations
 */
public interface IEmailService {

    /**
     * Send forgot password email with the current password
     * 
     * @param toEmail      recipient email address
     * @param customerName customer's full name
     * @param password     current password
     */
    void sendForgotPasswordEmail(String toEmail, String customerName, String password);

    /**
     * Send a simple email
     * 
     * @param toEmail recipient email address
     * @param subject email subject
     * @param content email content
     */
    void sendSimpleEmail(String toEmail, String subject, String content);
}