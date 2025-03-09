package com.example.bookstore.dto;

import java.util.Date;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String error;
    private String role;
    private Date accessTokenExpirationDate;  // Thời điểm hết hạn của access token
    private Date refreshTokenExpirationDate; // Thời điểm hết hạn của refresh token

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, String role, Date accessTokenExpirationDate, Date refreshTokenExpirationDate) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.accessTokenExpirationDate = accessTokenExpirationDate;
        this.refreshTokenExpirationDate = refreshTokenExpirationDate;
    }

    public AuthResponse(String accessToken, String refreshToken, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }

    // get role
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Date getAccessTokenExpirationDate() {
        return accessTokenExpirationDate;
    }

    public void setAccessTokenExpirationDate(Date accessTokenExpirationDate) {
        this.accessTokenExpirationDate = accessTokenExpirationDate;
    }

    public Date getRefreshTokenExpirationDate() {
        return refreshTokenExpirationDate;
    }

    public void setRefreshTokenExpirationDate(Date refreshTokenExpirationDate) {
        this.refreshTokenExpirationDate = refreshTokenExpirationDate;
    }
}