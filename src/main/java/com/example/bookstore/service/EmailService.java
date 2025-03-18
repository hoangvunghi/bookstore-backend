package com.example.bookstore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String senderEmail;
    
    @Value("${app.url}")
    private String appUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Gửi email thông thường
     * @param to Địa chỉ email người nhận
     * @param subject Tiêu đề email
     * @param content Nội dung email (có thể là plain text)
     * @throws MessagingException Nếu có lỗi khi gửi email
     */
    public void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, false); // false: nội dung là plain text
        
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject("Đặt lại mật khẩu - BookStore");
        
        String resetUrl = appUrl + "/reset-password?token=" + token;
        
        String emailContent = 
                "<html>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: #f9f9f9; padding: 20px; border-radius: 5px;'>" +
                "<h2 style='color: #4a6ee0;'>Đặt lại mật khẩu</h2>" +
                "<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình tại BookStore.</p>" +
                "<p>Vui lòng nhấp vào nút bên dưới để đặt lại mật khẩu của bạn:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + resetUrl + "' style='background-color: #4a6ee0; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold;'>Đặt lại mật khẩu</a>" +
                "</div>" +
                "<p>Hoặc bạn có thể sử dụng link sau: <a href='" + resetUrl + "'>" + resetUrl + "</a></p>" +
                "<p>Link này sẽ hết hạn sau 15 phút.</p>" +
                "<p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>" +
                "<p style='margin-top: 30px; font-size: 12px; color: #777;'>© 2025 BookStore. Tất cả các quyền được bảo lưu.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
        
        helper.setText(emailContent, true);
        
        mailSender.send(message);
    }
}