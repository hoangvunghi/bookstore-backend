package com.example.bookstore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.ForgotPasswordRequest;
import com.example.bookstore.dto.ResetPasswordRequest;
import com.example.bookstore.service.PasswordResetService;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;
    
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }
    
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        boolean result = passwordResetService.createPasswordResetTokenForEmail(request.getEmail());
        
        if (result) {
            return ResponseEntity.ok("Một email hướng dẫn đặt lại mật khẩu đã được gửi đến địa chỉ email của bạn.");
        } else {
            return ResponseEntity.ok("Nếu email tồn tại trong hệ thống, một hướng dẫn đặt lại mật khẩu sẽ được gửi đến.");
        }
    }
    
    @GetMapping("/reset/validate")
    public ResponseEntity<?> validateResetToken(@RequestParam("token") String token) {
        String result = passwordResetService.validatePasswordResetToken(token);
        
        switch (result) {
            case "valid":
                return ResponseEntity.ok("Token hợp lệ");
            case "invalidToken":
                return ResponseEntity.badRequest().body("Token không hợp lệ");
            case "expired":
                return ResponseEntity.badRequest().body("Token đã hết hạn");
            case "used":
                return ResponseEntity.badRequest().body("Token đã được sử dụng");
            default:
                return ResponseEntity.badRequest().body("Lỗi không xác định");
        }
    }
    
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        // Kiểm tra mật khẩu và xác nhận mật khẩu có khớp nhau không
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu xác nhận không khớp");
        }
        
        // Kiểm tra độ mạnh của mật khẩu
        if (request.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body("Mật khẩu phải có ít nhất 8 ký tự");
        }
        
        boolean result = passwordResetService.resetPassword(request.getToken(), request.getPassword());
        
        if (result) {
            return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công");
        } else {
            return ResponseEntity.badRequest().body("Không thể đặt lại mật khẩu. Token có thể không hợp lệ hoặc đã hết hạn.");
        }
    }
}