package project.demo.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import project.demo.service.IEmailService;

/**
 * Implementation of email service using Spring Mail
 */
@Service
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendForgotPasswordEmail(String toEmail, String customerName, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Khôi phục mật khẩu - The Souvenir Shop");

            String emailContent = buildForgotPasswordEmailContent(customerName, password);
            message.setText(emailContent);

            mailSender.send(message);
            System.out.println("Forgot password email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Error sending forgot password email to " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendSimpleEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Error sending email to " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Build the content for forgot password email
     * 
     * @param customerName customer's full name
     * @param password     current password
     * @return formatted email content
     */
    private String buildForgotPasswordEmailContent(String customerName, String password) {
        return String.format(
                "Xin chào %s,\n\n" +
                        "Bạn đã yêu cầu khôi phục mật khẩu cho tài khoản tại The Souvenir Shop.\n\n" +
                        "Mật khẩu hiện tại của bạn là: %s\n\n" +
                        "Vì lý do bảo mật, chúng tôi khuyến nghị bạn đăng nhập và thay đổi mật khẩu ngay sau khi nhận được email này.\n\n"
                        +
                        "Để thay đổi mật khẩu:\n" +
                        "1. Đăng nhập vào tài khoản của bạn\n" +
                        "2. Vào mục \"Hồ sơ cá nhân\"\n" +
                        "3. Chọn \"Đổi mật khẩu\"\n" +
                        "4. Nhập mật khẩu hiện tại và mật khẩu mới\n\n" +
                        "Nếu bạn không yêu cầu khôi phục mật khẩu, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ The Souvenir Shop\n\n" +
                        "---\n" +
                        "Email này được gửi tự động, vui lòng không trả lời.",
                customerName, password);
    }
}