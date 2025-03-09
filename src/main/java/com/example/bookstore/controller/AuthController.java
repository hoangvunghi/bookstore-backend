package com.example.bookstore.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.ApiResponse;
import com.example.bookstore.dto.AuthRequest;
import com.example.bookstore.dto.AuthResponse;
import com.example.bookstore.dto.RegisterRequest;
import com.example.bookstore.model.Role;
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
            String role = userDetails.getRole();

            return ResponseEntity.ok(new AuthResponse(
                accessToken, 
                refreshToken, 
                role,
                jwtService.getAccessTokenExpiration(accessToken),
                jwtService.getRefreshTokenExpiration(refreshToken)
            ));
        } catch (Exception e) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setError("Tên đăng nhập hoặc mật khẩu không đúng");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        // Check if the username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Tên đăng nhập đã tồn tại"));
        }
        // check if the email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email đã tồn tại"));
        }

        // Create a new user
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEmail(request.getEmail());
        newUser.setFullName(request.getFullName());
        newUser.setRole(Role.USER.name());
        newUser.setActive(true);
        userRepository.save(newUser);

        return ResponseEntity.ok(new ApiResponse(true, "Đăng ký tài khoản thành công"));
    }

    // Refresh token endpoint
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> requestMap) {
        String refreshToken = requestMap.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setError("Refresh token không được cung cấp");
            return ResponseEntity.status(400).body(errorResponse);
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
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setError("Refresh token không hợp lệ");
                return ResponseEntity.status(401).body(errorResponse);
            }

            // Kiểm tra xem người dùng tồn tại không
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setError("Không tìm thấy người dùng");
                return ResponseEntity.status(401).body(errorResponse);
            }

            UserDetailsImpl userDetails = new UserDetailsImpl(userOptional.get());
            
            // Xác thực refresh token
            if (jwtService.validateRefreshToken(refreshToken, userDetails)) {
                // Tạo access token mới
                String newAccessToken = jwtService.generateAccessToken(userDetails);
                return ResponseEntity.ok(new AuthResponse(
                    newAccessToken, 
                    refreshToken, 
                    userDetails.getRole(),
                    jwtService.getAccessTokenExpiration(newAccessToken),
                    jwtService.getRefreshTokenExpiration(refreshToken)
                ));
            } else {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setError("Refresh token không hợp lệ hoặc đã hết hạn");
                return ResponseEntity.status(401).body(errorResponse);
            }
        } catch (Exception e) {
            System.out.println("Lỗi tổng thể: " + e.getMessage());
            e.printStackTrace();
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setError("Đã xảy ra lỗi: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    // api check role
    @GetMapping("/role")
    public ResponseEntity<?> getRole(@RequestHeader("Authorization") String token) {
        try {
            String accessToken = token.replace("Bearer ", "");
            String username = jwtService.extractUsernameFromAccessToken(accessToken);
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                String role = userOptional.get().getRole();
                return ResponseEntity.ok(new ApiResponse(true, "Role retrieved successfully", role));
            } else {
                return ResponseEntity.status(404).body(new ApiResponse(false, "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Invalid token"));
        }
    }
}