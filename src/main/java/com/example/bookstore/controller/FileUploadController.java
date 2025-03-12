package com.example.bookstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.bookstore.dto.ApiResponse;
import com.example.bookstore.service.CloudinaryService;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @Autowired
    public FileUploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Kiểm tra file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Vui lòng chọn file để tải lên", null));
            }

            // Kiểm tra loại file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Chỉ chấp nhận file hình ảnh", null));
            }

            System.out.println("Uploading file to Cloudinary: " + file.getOriginalFilename() + ", size: " + file.getSize() + " bytes");
            
            // Tải lên Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file);
            
            System.out.println("File uploaded successfully. URL: " + imageUrl);

            return ResponseEntity.ok(new ApiResponse(true, "Tải lên file thành công", imageUrl));
        } catch (Exception ex) {
            System.err.println("Error uploading file: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ApiResponse(false, "Không thể tải lên file: " + ex.getMessage(), null));
        }
    }
    
    @PostMapping("/upload-base64")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> uploadBase64Image(@RequestParam("imageData") String imageData) {
        try {
            // Kiểm tra dữ liệu
            if (imageData == null || imageData.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Dữ liệu hình ảnh không hợp lệ", null));
            }

            // Kiểm tra xem có phải là base64 không
            if (!cloudinaryService.isBase64Image(imageData)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Dữ liệu không phải là base64", null));
            }

            System.out.println("Uploading base64 image to Cloudinary, data length: " + imageData.length());
            
            // Tải lên Cloudinary
            String imageUrl = cloudinaryService.uploadBase64Image(imageData);
            
            System.out.println("Base64 image uploaded successfully. URL: " + imageUrl);

            return ResponseEntity.ok(new ApiResponse(true, "Tải lên hình ảnh thành công", imageUrl));
        } catch (Exception ex) {
            System.err.println("Error uploading base64 image: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ApiResponse(false, "Không thể tải lên hình ảnh: " + ex.getMessage(), null));
        }
    }
} 