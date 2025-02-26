package com.example.bookstore.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.AuthRequest;
import com.example.bookstore.dto.AuthResponse;
import com.example.bookstore.dto.RegisterRequest;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtService;
import com.example.bookstore.security.UserDetailsImpl;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new AuthResponse("Invalid credentials", ""));
        }
    }

    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Check if the username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken!");
        }

        // Create a new user
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEmail(request.getEmail());
        newUser.setFullName(request.getFullName());
        newUser.setActive(true);
        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully!");
    }

    // Refresh token endpoint
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> requestMap) {
        String refreshToken = requestMap.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(400).body(new AuthResponse("Refresh token không được cung cấp", ""));
        }
        
        try {
            System.out.println("Refresh token nhận được: " + refreshToken);
            
            // Lấy username từ refresh token sử dụng key đúng
            String username;
            try {
                username = jwtService.extractUsernameFromRefreshToken(refreshToken);
                System.out.println("Extracted username: " + username);
            } catch (Exception e) {
                System.out.println("Lỗi khi giải mã refresh token: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(401).body(new AuthResponse("Invalid refresh token", ""));
            }

            // Kiểm tra xem người dùng tồn tại không
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(401).body(new AuthResponse("Không tìm thấy người dùng", ""));
            }

            UserDetailsImpl userDetails = new UserDetailsImpl(userOptional.get());
            
            // Xác thực refresh token
            if (jwtService.validateRefreshToken(refreshToken, userDetails)) {
                // Tạo access token mới
                String newAccessToken = jwtService.generateAccessToken(userDetails);
                return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
            } else {
                return ResponseEntity.status(401).body(new AuthResponse("Refresh token không hợp lệ hoặc đã hết hạn", ""));
            }
        } catch (Exception e) {
            System.out.println("Lỗi tổng thể: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new AuthResponse("Đã xảy ra lỗi: " + e.getMessage(), ""));
        }
    }
}