package com.example.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.bookstore.security.JwtAuthenticationFilter;
import com.example.bookstore.security.UserDetailsServiceImpl;

import io.swagger.v3.oas.models.PathItem;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;
    
    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtAuthenticationFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
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
                .requestMatchers(new AntPathRequestMatcher("/api/password/forgot")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/auth/role")).permitAll() // Cho phép API quên mật khẩu
                .requestMatchers(new AntPathRequestMatcher("/api/password/reset/validate")).permitAll() // Cho phép API validate token
                .requestMatchers(new AntPathRequestMatcher("/api/password/reset")).permitAll() // Cho phép API đặt lại mật khẩu
                .requestMatchers(new AntPathRequestMatcher("/api/admin/**")).hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/product-categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/images/**").permitAll() // Cho phép truy cập hình ảnh
                .anyRequest().authenticated() // Các request khác phải xác thực
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không dùng session
            .csrf(csrf -> csrf.disable()) // Tắt CSRF (vì API REST không cần)
            .cors()
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
             
        return http.build();
    }
}