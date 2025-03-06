package com.example.bookstore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.ApiResponse;
import com.example.bookstore.dto.UserProfileDTO;
import com.example.bookstore.model.User;
import com.example.bookstore.security.UserDetailsImpl;
import com.example.bookstore.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userService.getUserById(userDetails.getUser().getUserId());
        
        if (user == null) {
            return ResponseEntity.notFound()
                .build();
        }

        // Tạo DTO để trả về (không bao gồm mật khẩu)
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setFullName(user.getFullName());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setPhoneNumber(user.getPhoneNumber());
        profileDTO.setAddress(user.getAddress());

        return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin profile thành công", profileDTO));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(
            Authentication authentication,
            @RequestBody UserProfileDTO profileDTO) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Nếu có yêu cầu đổi mật khẩu
        if (profileDTO.getCurrentPassword() != null && profileDTO.getNewPassword() != null) {
            boolean passwordChanged = userService.changePassword(
                userDetails.getUser().getUserId(),
                profileDTO.getCurrentPassword(),
                profileDTO.getNewPassword()
            );
            
            if (!passwordChanged) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Mật khẩu hiện tại không đúng"));
            }
        }

        // Cập nhật thông tin cá nhân
        User updatedUser = userService.updateProfile(userDetails.getUser().getUserId(), profileDTO);
        if (updatedUser == null) {
            return ResponseEntity.notFound()
                .build();
        }

        // Tạo DTO để trả về
        UserProfileDTO updatedProfile = new UserProfileDTO();
        updatedProfile.setFullName(updatedUser.getFullName());
        updatedProfile.setEmail(updatedUser.getEmail());
        updatedProfile.setPhoneNumber(updatedUser.getPhoneNumber());
        updatedProfile.setAddress(updatedUser.getAddress());

        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật profile thành công", updatedProfile));
    }
}
