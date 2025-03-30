package com.example.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Cho phép tất cả origin bằng cách set allowCredentials = false
        config.addAllowedOriginPattern("*");
        
        // Cho phép tất cả các header
        config.addAllowedHeader("*");
        
        // Cho phép tất cả các method (GET, POST, PUT, DELETE, etc.)
        config.addAllowedMethod("*");
        
        // Không sử dụng credentials để có thể dùng allowedOriginPattern("*")
        config.setAllowCredentials(false);
        
        // Cho phép các header tùy chỉnh
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        
        // Thời gian cache cho preflight request (1 giờ)
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 