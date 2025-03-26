package com.example.bookstore.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.UserDTO;
import com.example.bookstore.model.Role;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.UserRepository;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Chuyển đổi User thành UserDTO
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setRegistrationDate(user.getRegistrationDate());
        // Không set password
        return dto;
    }

    // Lấy danh sách tất cả người dùng
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToDTO);
    }

    // Tìm kiếm người dùng theo tên, email, username
    public Page<UserDTO> searchUsers(String search, Pageable pageable) {
        // Cần thêm phương thức tìm kiếm vào UserRepository
        return userRepository.findByUsernameContainingOrEmailContainingOrFullNameContaining(
                search, search, search, pageable).map(this::convertToDTO);
    }

    // Lấy thông tin chi tiết của một người dùng
    public UserDTO getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(this::convertToDTO).orElse(null);
    }

    // Kiểm tra username đã tồn tại chưa
    public boolean isUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // Kiểm tra email đã tồn tại chưa
    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // Kiểm tra email đã tồn tại cho người dùng khác chưa
    public boolean isEmailExistsForOtherUser(String email, Long userId) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent() && !user.get().getUserId().equals(userId);
    }

    // Tạo người dùng mới
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setAddress(userDTO.getAddress());
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : Role.USER.name());
        user.setActive(true);
        user.setRegistrationDate(new Date());

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    // Cập nhật thông tin người dùng
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();
        
        // Cập nhật thông tin cơ bản
        if (userDTO.getFullName() != null) {
            user.setFullName(userDTO.getFullName());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if (userDTO.getAddress() != null) {
            user.setAddress(userDTO.getAddress());
        }
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }
        
        // Cập nhật mật khẩu nếu có
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    // Vô hiệu hóa tài khoản người dùng (soft delete)
    @Transactional
    public boolean deactivateUser(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();
        user.setActive(false);
        System.out.println("--------------------------------");
        System.out.println("User deactivated: " + user.getUsername());
        System.out.println("--------------------------------");
        userRepository.save(user);
        return true;
    }

    // Kích hoạt lại tài khoản người dùng
    @Transactional
    public boolean activateUser(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();
        user.setActive(true);
        userRepository.save(user);
        return true;
    }

    // Đặt lại mật khẩu cho người dùng
    @Transactional
    public String resetPassword(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();
        
        // Tạo mật khẩu ngẫu nhiên
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return newPassword;
    }

    // Tạo mật khẩu ngẫu nhiên
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    // Thay đổi vai trò của người dùng
    @Transactional
    public boolean changeRole(Long id, Role role) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();
        user.setRole(role.name());
        userRepository.save(user);
        return true;
    }

    // Lấy thống kê về người dùng
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long inactiveUsers = userRepository.countByIsActiveFalse();
        long adminUsers = userRepository.countByRole(Role.ADMIN.name());
        long regularUsers = userRepository.countByRole(Role.USER.name());
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", inactiveUsers);
        stats.put("adminUsers", adminUsers);
        stats.put("regularUsers", regularUsers);
        
        return stats;
    }
} 