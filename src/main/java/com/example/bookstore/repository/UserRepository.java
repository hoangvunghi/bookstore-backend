package com.example.bookstore.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bookstore.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRegistrationDateBetween(Date startDate, Date endDate);
    
    // Tìm kiếm người dùng theo tên, email, username
    Page<User> findByUsernameContainingOrEmailContainingOrFullNameContaining(
            String username, String email, String fullName, Pageable pageable);
    
    // Đếm số lượng người dùng theo trạng thái
    long countByIsActiveTrue();
    long countByIsActiveFalse();
    
    // Đếm số lượng người dùng theo vai trò
    long countByRole(String role);
}