package com.example.bookstore.service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Tải lên hình ảnh từ MultipartFile
     */
    public String uploadImage(MultipartFile file) throws IOException {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                "folder", "bookstore",
                "public_id", UUID.randomUUID().toString(),
                "overwrite", true,
                "resource_type", "image"
            );
            
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            System.out.println("Upload result: " + uploadResult);
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            System.err.println("Error uploading to Cloudinary: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Không thể tải lên Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Tải lên hình ảnh từ chuỗi Base64
     */
    public String uploadBase64Image(String base64Image) throws IOException {
        try {
            System.out.println("Bắt đầu xử lý ảnh base64");
            System.out.println("Độ dài chuỗi đầu vào: " + base64Image.length());
            System.out.println("100 ký tự đầu tiên: " + 
                (base64Image.length() > 100 ? base64Image.substring(0, 100) + "..." : base64Image));
            
            // Xử lý chuỗi base64 để lấy phần dữ liệu
            String base64Data = base64Image;
            String contentType = null;
            
            // Nếu chuỗi có định dạng data:image/xxx;base64,
            if (base64Image.contains(";base64,")) {
                System.out.println("Phát hiện định dạng ;base64,");
                String[] parts = base64Image.split(";base64,");
                if (parts.length >= 2) {
                    // Lấy content type
                    if (parts[0].startsWith("data:")) {
                        contentType = parts[0].substring(5);
                        System.out.println("Content type: " + contentType);
                    }
                    base64Data = parts[1];
                    System.out.println("Đã tách được phần dữ liệu base64");
                }
            } else if (base64Image.contains(",")) {
                System.out.println("Phát hiện dấu phẩy, tách lấy phần sau dấu phẩy");
                base64Data = base64Image.split(",")[1];
            }
            
            // Loại bỏ tất cả các ký tự không phải là A-Z, a-z, 0-9, +, /, =
            String originalLength = String.valueOf(base64Data.length());
            base64Data = base64Data.replaceAll("[^A-Za-z0-9+/=]", "");
            System.out.println("Đã loại bỏ ký tự không hợp lệ: " + originalLength + " -> " + base64Data.length());
            
            // Đảm bảo độ dài chuỗi base64 là bội số của 4
            int remainder = base64Data.length() % 4;
            if (remainder > 0) {
                System.out.println("Thêm padding '=' cho đủ bội số của 4");
                base64Data = base64Data + "=".repeat(4 - remainder);
            }
            
            // Decode base64
            System.out.println("Tiến hành decode base64");
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            System.out.println("Kích thước ảnh sau khi decode: " + imageBytes.length + " bytes");
            
            // Tạo public_id dựa trên content type
            String publicId = UUID.randomUUID().toString();
            if (contentType != null) {
                // Thêm extension vào public_id nếu có thể xác định
                String extension = "";
                if (contentType.equals("image/jpeg") || contentType.equals("image/jpg")) {
                    extension = ".jpg";
                } else if (contentType.equals("image/png")) {
                    extension = ".png";
                } else if (contentType.equals("image/gif")) {
                    extension = ".gif";
                } else if (contentType.equals("image/webp")) {
                    extension = ".webp";
                }
                publicId += extension;
                System.out.println("Public ID với extension: " + publicId);
            }
            
            // Tải lên Cloudinary
            System.out.println("Bắt đầu tải lên Cloudinary");
            Map<String, Object> params = ObjectUtils.asMap(
                "folder", "bookstore",
                "public_id", publicId,
                "overwrite", true,
                "resource_type", "image"
            );
            
            Map<?, ?> uploadResult = cloudinary.uploader().upload(imageBytes, params);
            System.out.println("Kết quả tải lên: " + uploadResult);
            String secureUrl = (String) uploadResult.get("secure_url");
            System.out.println("URL của ảnh: " + secureUrl);
            return secureUrl;
        } catch (Exception e) {
            System.err.println("Lỗi khi tải lên Cloudinary: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Không thể tải lên Cloudinary: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra xem chuỗi có phải là base64 hay không
     */
    public boolean isBase64Image(String imageURL) {
        System.out.println("Kiểm tra chuỗi có phải base64 không");
        if (imageURL == null || imageURL.trim().isEmpty()) {
            System.out.println("Chuỗi null hoặc rỗng");
            return false;
        }
        
        System.out.println("Độ dài chuỗi: " + imageURL.length());
        System.out.println("100 ký tự đầu tiên: " + 
            (imageURL.length() > 100 ? imageURL.substring(0, 100) + "..." : imageURL));
        
        // Kiểm tra các định dạng base64 phổ biến
        if (imageURL.startsWith("data:image/") || 
            imageURL.startsWith("data:application/octet-stream;base64,") ||
            imageURL.startsWith("data:application/")) {
            System.out.println("Phát hiện định dạng data URI");
            return true;
        }
        
        // Kiểm tra xem có phải là URL không
        if (imageURL.startsWith("http://") || imageURL.startsWith("https://")) {
            System.out.println("Đây là URL thông thường");
            return false;
        }
        
        // Kiểm tra xem có phải là chuỗi base64 thuần túy không
        if (imageURL.length() > 100) {
            // Loại bỏ khoảng trắng và xuống dòng
            String cleaned = imageURL.replaceAll("\\s", "");
            System.out.println("Đã loại bỏ khoảng trắng");
            
            // Kiểm tra xem có chứa các ký tự base64 không
            if (cleaned.matches("^[A-Za-z0-9+/=]+$")) {
                System.out.println("Chuỗi chứa các ký tự base64 hợp lệ");
                // Kiểm tra độ dài chuỗi base64 (phải là bội số của 4)
                if (cleaned.length() % 4 == 0) {
                    System.out.println("Độ dài là bội số của 4");
                    return true;
                } else {
                    System.out.println("Độ dài không phải bội số của 4");
                }
            } else {
                System.out.println("Chuỗi chứa ký tự không hợp lệ cho base64");
            }
        } else {
            System.out.println("Chuỗi quá ngắn để là base64");
        }
        
        return false;
    }
    
    /**
     * Xóa hình ảnh từ Cloudinary
     */
    public boolean deleteImage(String imageUrl) {
        try {
            // Lấy public_id từ URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId == null) {
                return false;
            }
            
            System.out.println("Deleting image with public_id: " + publicId);
            
            // Xóa hình ảnh
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("Delete result: " + result);
            return "ok".equals(result.get("result"));
        } catch (Exception e) {
            System.err.println("Error deleting from Cloudinary: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Trích xuất public_id từ URL
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        // URL có dạng: https://res.cloudinary.com/cloud_name/image/upload/v1234567890/bookstore/image_id.jpg
        if (imageUrl == null) {
            return null;
        }
        
        try {
            // Tách URL để lấy phần path
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }
            
            String path = parts[1];
            
            // Loại bỏ phần version nếu có (v1234567890/)
            if (path.matches("^v\\d+/.*")) {
                path = path.replaceFirst("^v\\d+/", "");
            }
            
            // Loại bỏ phần extension (.jpg, .png, ...)
            int dotIndex = path.lastIndexOf(".");
            if (dotIndex > 0) {
                path = path.substring(0, dotIndex);
            }
            
            System.out.println("Extracted public_id: " + path);
            return path;
        } catch (Exception e) {
            System.err.println("Error extracting public_id: " + e.getMessage());
            return null;
        }
    }
} 