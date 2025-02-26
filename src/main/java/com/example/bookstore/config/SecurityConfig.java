package com.example.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.bookstore.security.UserDetailsServiceImpl;

@Configuration
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    
    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    // AuthenticationManager để xác thực người dùng
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    // Mã hóa mật khẩu với BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // Cấu hình bảo mật cho HTTP requests
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**") // Chỉ áp dụng bảo mật cho API
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/api/auth/login")).permitAll() // Cho phép không cần auth
                .requestMatchers(new AntPathRequestMatcher("/api/auth/refresh")).permitAll() // Cho phép không cần auth
                .requestMatchers(new AntPathRequestMatcher("/api/auth/register")).permitAll() // Cho phép không cần auth
                .requestMatchers(new AntPathRequestMatcher("/api/password/forgot")).permitAll() // Cho phép API quên mật khẩu
                .requestMatchers(new AntPathRequestMatcher("/api/password/reset/validate")).permitAll() // Cho phép API validate token
                .requestMatchers(new AntPathRequestMatcher("/api/password/reset")).permitAll() // Cho phép API đặt lại mật khẩu
                .anyRequest().authenticated() // Các request khác phải xác thực
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không dùng session
            .csrf(csrf -> csrf.disable()) // Tắt CSRF (vì API REST không cần)
            .cors(cors -> cors.disable()); // Tắt CORS nếu không cần
             
        return http.build();
    }
}