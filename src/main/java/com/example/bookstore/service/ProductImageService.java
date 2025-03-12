package com.example.bookstore.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.ProductImageDTO;
import com.example.bookstore.model.Product;
import com.example.bookstore.model.ProductImage;
import com.example.bookstore.repository.ProductImageRepository;
import com.example.bookstore.repository.ProductRepository;

@Service
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public ProductImageService(ProductImageRepository productImageRepository,
                             ProductRepository productRepository,
                             CloudinaryService cloudinaryService) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
        this.cloudinaryService = cloudinaryService;
    }

    private ProductImageDTO convertToDTO(ProductImage image) {
        ProductImageDTO dto = new ProductImageDTO();
        dto.setProductImageId(image.getProductImageId());
        if (image.getProduct() != null) {
            dto.setProductId(image.getProduct().getProductId());
        }
        dto.setImageURL(image.getImageURL());
        return dto;
    }

    // Lấy tất cả ảnh của một sản phẩm
    public List<ProductImageDTO> getProductImages(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return null;
        }

        return productImageRepository.findByProduct(product)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Thêm ảnh mới cho sản phẩm
    @Transactional
    public ProductImageDTO addProductImage(Long productId, String imageData) throws IOException {
        System.out.println("Bắt đầu thêm ảnh cho sản phẩm ID: " + productId);

        if (imageData == null || imageData.trim().isEmpty()) {
            System.out.println("Dữ liệu ảnh null hoặc rỗng");
            throw new IllegalArgumentException("Dữ liệu ảnh không được phép rỗng");
        }

        String imageURL;
        try {
            System.out.println("Kiểm tra định dạng dữ liệu ảnh");
            // Kiểm tra xem có phải là base64 không
            if (cloudinaryService.isBase64Image(imageData)) {
                System.out.println("Phát hiện dữ liệu base64, tiến hành tải lên Cloudinary");
                // Nếu là base64, tải lên Cloudinary
                imageURL = cloudinaryService.uploadBase64Image(imageData);
                System.out.println("Đã tải lên Cloudinary thành công, URL: " + imageURL);
            } else {
                // Nếu không phải base64, giả định là URL
                System.out.println("Không phải base64, sử dụng như URL: " + imageData);
                imageURL = imageData;
            }

            ProductImage image = new ProductImage();
            image.setImageURL(imageURL);

            // Nếu có productId, kiểm tra và set product
            if (productId != null) {
                Product product = productRepository.findById(productId).orElse(null);
                if (product != null) {
                    image.setProduct(product);
                    System.out.println("Đã liên kết với sản phẩm: " + product.getName());
                } else {
                    System.out.println("Không tìm thấy sản phẩm với ID: " + productId + ", ảnh sẽ được lưu mà không có product");
                }
            } else {
                System.out.println("ProductId chưa được cung cấp, ảnh sẽ được lưu mà không có product");
            }
            
            System.out.println("Lưu thông tin ảnh vào cơ sở dữ liệu");
            image = productImageRepository.save(image);
            System.out.println("Đã lưu ảnh với ID: " + image.getProductImageId());

            return convertToDTO(image);
        } catch (Exception e) {
            System.err.println("Lỗi trong addProductImage: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Không thể thêm ảnh sản phẩm: " + e.getMessage());
        }
    }

    // Thêm phương thức mới để liên kết ảnh với sản phẩm sau khi đã có product
    @Transactional
    public ProductImageDTO linkImageToProduct(Long imageId, Long productId) {
        if (imageId == null || productId == null) {
            throw new IllegalArgumentException("ImageId và ProductId không được phép null");
        }

        ProductImage image = productImageRepository.findById(imageId).orElse(null);
        if (image == null) {
            throw new IllegalArgumentException("Không tìm thấy ảnh với ID: " + imageId);
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId);
        }

        image.setProduct(product);
        image = productImageRepository.save(image);
        System.out.println("Đã liên kết ảnh " + imageId + " với sản phẩm " + productId);

        return convertToDTO(image);
    }

    // Cập nhật ảnh
    @Transactional
    public ProductImageDTO updateProductImage(Long imageId, String imageData) throws IOException {
        if (imageData == null || imageData.trim().isEmpty()) {
            return null;
        }

        ProductImage image = productImageRepository.findById(imageId).orElse(null);
        if (image == null) {
            return null;
        }

        try {
            // Nếu ảnh cũ là URL Cloudinary, xóa ảnh cũ
            if (image.getImageURL() != null && image.getImageURL().contains("cloudinary.com")) {
                System.out.println("Deleting old image: " + image.getImageURL());
                cloudinaryService.deleteImage(image.getImageURL());
            }

            String imageURL;
            
            // Kiểm tra xem có phải là base64 không
            if (cloudinaryService.isBase64Image(imageData)) {
                System.out.println("Uploading base64 image to Cloudinary for update");
                // Nếu là base64, tải lên Cloudinary
                imageURL = cloudinaryService.uploadBase64Image(imageData);
                System.out.println("Updated image URL: " + imageURL);
            } else {
                // Nếu không phải base64, giả định là URL
                System.out.println("Using provided URL for update: " + imageData);
                imageURL = imageData;
            }

            image.setImageURL(imageURL);
            image = productImageRepository.save(image);

            return convertToDTO(image);
        } catch (Exception e) {
            System.err.println("Error in updateProductImage: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Không thể cập nhật ảnh sản phẩm: " + e.getMessage());
        }
    }

    // Xóa ảnh
    @Transactional
    public boolean deleteProductImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId).orElse(null);
        if (image == null) {
            return false;
        }

        try {
            // Nếu ảnh là URL Cloudinary, xóa ảnh từ Cloudinary
            if (image.getImageURL() != null && image.getImageURL().contains("cloudinary.com")) {
                System.out.println("Deleting image from Cloudinary: " + image.getImageURL());
                cloudinaryService.deleteImage(image.getImageURL());
            }

            productImageRepository.delete(image);
            return true;
        } catch (Exception e) {
            System.err.println("Error in deleteProductImage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Xóa tất cả ảnh của một sản phẩm
    @Transactional
    public void deleteAllProductImages(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            try {
                // Lấy tất cả ảnh của sản phẩm
                List<ProductImage> images = productImageRepository.findByProduct(product);
                
                // Xóa từng ảnh từ Cloudinary
                for (ProductImage image : images) {
                    if (image.getImageURL() != null && image.getImageURL().contains("cloudinary.com")) {
                        System.out.println("Deleting image from Cloudinary in batch: " + image.getImageURL());
                        cloudinaryService.deleteImage(image.getImageURL());
                    }
                }
                
                // Xóa tất cả ảnh từ cơ sở dữ liệu
                productImageRepository.deleteByProduct(product);
            } catch (Exception e) {
                System.err.println("Error in deleteAllProductImages: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}