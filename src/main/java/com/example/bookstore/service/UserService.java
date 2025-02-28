package com.example.bookstore.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.bookstore.dto.UserProfileDTO;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateProfile(Long userId, UserProfileDTO profileDTO) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        // Cập nhật thông tin cơ bản
        if (profileDTO.getFullName() != null) {
            user.setFullName(profileDTO.getFullName());
        }
        if (profileDTO.getEmail() != null) {
            user.setEmail(profileDTO.getEmail());
        }
        if (profileDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(profileDTO.getPhoneNumber());
        }
        if (profileDTO.getAddress() != null) {
            user.setAddress(profileDTO.getAddress());
        }

        return userRepository.save(user);
    }

    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}
