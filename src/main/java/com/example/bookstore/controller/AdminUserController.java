package com.example.bookstore.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.ApiResponse;
import com.example.bookstore.dto.UserDTO;
import com.example.bookstore.model.Role;
import com.example.bookstore.service.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // Lấy danh sách tất cả người dùng (có phân trang)
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<UserDTO> users;
        if (search != null && !search.isEmpty()) {
            users = adminUserService.searchUsers(search, pageable);
        } else {
            users = adminUserService.getAllUsers(pageable);
        }
        return ResponseEntity.ok(users);
    }

    // Lấy thông tin chi tiết của một người dùng
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        UserDTO user = adminUserService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin người dùng thành công", user));
    }

    // Tạo người dùng mới (bao gồm cả ADMIN)
    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody UserDTO userDTO) {
        // Kiểm tra username và email đã tồn tại chưa
        if (adminUserService.isUsernameExists(userDTO.getUsername())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Tên đăng nhập đã tồn tại"));
        }
        if (adminUserService.isEmailExists(userDTO.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email đã tồn tại"));
        }

        UserDTO createdUser = adminUserService.createUser(userDTO);
        return ResponseEntity.ok(new ApiResponse(true, "Tạo người dùng thành công", createdUser));
    }

    // Cập nhật thông tin người dùng
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        // Kiểm tra email đã tồn tại chưa (nếu thay đổi)
        if (userDTO.getEmail() != null && adminUserService.isEmailExistsForOtherUser(userDTO.getEmail(), id)) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email đã tồn tại"));
        }

        UserDTO updatedUser = adminUserService.updateUser(id, userDTO);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật người dùng thành công", updatedUser));
    }

    // Vô hiệu hóa tài khoản người dùng (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deactivateUser(@PathVariable Long id) {
        boolean success = adminUserService.deactivateUser(id);
        if (!success) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse(true, "Vô hiệu hóa tài khoản thành công"));
    }

    // Kích hoạt lại tài khoản người dùng
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse> activateUser(@PathVariable Long id) {
        boolean success = adminUserService.activateUser(id);
        if (!success) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse(true, "Kích hoạt tài khoản thành công"));
    }

    // Đặt lại mật khẩu cho người dùng
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@PathVariable Long id) {
        String newPassword = adminUserService.resetPassword(id);
        if (newPassword == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse(true, "Đặt lại mật khẩu thành công", 
                                                Map.of("newPassword", newPassword)));
    }

    // Thay đổi vai trò của người dùng (USER <-> ADMIN)
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse> changeRole(@PathVariable Long id, @RequestParam String role) {
        try {
            Role newRole = Role.valueOf(role.toUpperCase());
            boolean success = adminUserService.changeRole(id, newRole);
            if (!success) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new ApiResponse(true, "Thay đổi vai trò thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Vai trò không hợp lệ"));
        }
    }

    // Lấy thống kê về người dùng
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getUserStats() {
        Map<String, Object> stats = adminUserService.getUserStats();
        return ResponseEntity.ok(new ApiResponse(true, "Lấy thống kê người dùng thành công", stats));
    }
} 