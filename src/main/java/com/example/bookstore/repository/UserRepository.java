package com.example.bookstore.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookstore.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    // Tìm kiếm người dùng theo tên, email, username
    Page<User> findByUsernameContainingOrEmailContainingOrFullNameContaining(
            String username, String email, String fullName, Pageable pageable);
    
    // Đếm số lượng người dùng theo trạng thái
    long countByIsActiveTrue();
    long countByIsActiveFalse();
    
    // Đếm số lượng người dùng theo vai trò
    long countByRole(String role);
}