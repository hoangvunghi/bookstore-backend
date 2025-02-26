package com.example.bookstore.service;

import java.util.Optional;
import java.util.UUID;

import javax.mail.MessagingException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bookstore.model.PasswordResetToken;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.PasswordResetTokenRepository;
import com.example.bookstore.repository.UserRepository;
@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository, 
                         PasswordResetTokenRepository tokenRepository,
                         EmailService emailService,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean createPasswordResetTokenForEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            return false;
        }
        
        User user = userOptional.get();
        
        // Kiểm tra token cũ và xóa nếu có
        Optional<PasswordResetToken> existingToken = tokenRepository.findByUser(user);
        existingToken.ifPresent(tokenRepository::delete);
        
        // Tạo token mới
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);
        
        // Gửi email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public String validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        
        if (tokenOptional.isEmpty()) {
            return "invalidToken";
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        
        if (resetToken.isExpired()) {
            return "expired";
        }
        
        if (resetToken.isUsed()) {
            return "used";
        }
        
        return "valid";
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        
        if (tokenOptional.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        
        // Kiểm tra token có hợp lệ không
        if (resetToken.isExpired() || resetToken.isUsed()) {
            return false;
        }
        
        User user = resetToken.getUser();
        
        // Cập nhật mật khẩu
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Đánh dấu token đã sử dụng
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        return true;
    }
}