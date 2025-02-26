package com.example.bookstore.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private LocalDateTime expiryDateTime;
    
    @Column(nullable = false)
    private boolean used;
    
    // Thời gian token có hiệu lực tính theo phút
    public static final int EXPIRATION_MINUTES = 15;
    
    public PasswordResetToken() {
        this.used = false;
    }
    
    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDateTime = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        this.used = false;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDateTime);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiryDateTime() {
        return expiryDateTime;
    }

    public void setExpiryDateTime(LocalDateTime expiryDateTime) {
        this.expiryDateTime = expiryDateTime;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}