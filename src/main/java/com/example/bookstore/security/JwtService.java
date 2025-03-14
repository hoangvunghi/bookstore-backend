package com.example.bookstore.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String accessSecret;

    @Value("${jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${jwt.expiration}")
    private long expirationTime;  // Thời gian sống của Access Token

    // Lấy secret cho refresh token
    public String getRefreshSecret() {
        return refreshSecret;
    }
    
    // Lấy key ký cho access token
    public Key getSigningKey() {
        return getSigningKey(accessSecret);
    }
    
    // Lấy key ký cho refresh token
    public Key getRefreshSigningKey() {
        return getSigningKey(refreshSecret);
    }

    // Phương thức tạo key từ secret
    private Key getSigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Phương thức tạo Access Token
    public String generateAccessToken(UserDetailsImpl userDetails) {
        return generateToken(userDetails.getUsername(), expirationTime, getSigningKey(accessSecret));
    }

    // Phương thức tạo Refresh Token
    public String generateRefreshToken(UserDetailsImpl userDetails) {
        long refreshExpirationTime = expirationTime * 7;  // Refresh Token sống lâu hơn Access Token 7 lần
        return generateToken(userDetails.getUsername(), refreshExpirationTime, getSigningKey(refreshSecret));
    }

    // Phương thức tạo JWT token
    private String generateToken(String username, long expiryDuration, Key key) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiryDuration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Lấy thời điểm hết hạn của token
    public Date getExpirationDateFromToken(String token, Key key) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }

    // Lấy thời điểm hết hạn của access token
    public Date getAccessTokenExpiration(String token) {
        return getExpirationDateFromToken(token, getSigningKey(accessSecret));
    }

    // Lấy thời điểm hết hạn của refresh token
    public Date getRefreshTokenExpiration(String token) {
        return getExpirationDateFromToken(token, getSigningKey(refreshSecret));
    }

    // Phương thức validate Access Token
    public boolean validateAccessToken(String token, UserDetailsImpl userDetails) {
        return validateToken(token, userDetails, getSigningKey(accessSecret));
    }

    // Phương thức validate Refresh Token
    public boolean validateRefreshToken(String token, UserDetailsImpl userDetails) {
        return validateToken(token, userDetails, getSigningKey(refreshSecret));
    }

    // Phương thức validate Token (chung cho Access và Refresh Token)
    private boolean validateToken(String token, UserDetailsImpl userDetails, Key key) {
        try {
            String username = extractUsername(token, key);
            // Kiểm tra xem người dùng có bị vô hiệu hóa không (isActive = false)
            if (!userDetails.isEnabled()) {
                System.out.println("Người dùng " + username + " đã bị vô hiệu hóa");
                return false;
            }
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token, key);
        } catch (Exception e) {
            return false;
        }
    }

    // Phương thức lấy username từ JWT token
    public String extractUsername(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    // Phương thức lấy username từ access token
    public String extractUsernameFromAccessToken(String token) {
        return extractUsername(token, getSigningKey(accessSecret));
    }
    
    // Phương thức lấy username từ refresh token
    public String extractUsernameFromRefreshToken(String token) {
        return extractUsername(token, getSigningKey(refreshSecret));
    }

    // Phương thức kiểm tra token có hết hạn hay không
    private boolean isTokenExpired(String token, Key key) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}