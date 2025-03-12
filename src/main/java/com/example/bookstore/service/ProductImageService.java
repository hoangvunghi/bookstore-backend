package com.example.bookstore.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${image.upload.dir}")
    private String uploadDir;

    public ProductImageService(ProductImageRepository productImageRepository,
                             ProductRepository productRepository) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    private ProductImageDTO convertToDTO(ProductImage image) {
        ProductImageDTO dto = new ProductImageDTO();
        dto.setProductImageId(image.getProductImageId());
        dto.setProductId(image.getProduct().getProductId());
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

    // Thêm ảnh mới cho sản phẩm từ base64
    @Transactional
    public ProductImageDTO addProductImage(Long productId, String base64Image) throws IOException {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || base64Image == null || base64Image.trim().isEmpty()) {
            return null;
        }

        // Lưu ảnh từ base64
        String imageUrl = saveImageFromBase64(base64Image);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageURL(imageUrl);
        image = productImageRepository.save(image);

        return convertToDTO(image);
    }

    private String saveImageFromBase64(String base64Image) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        System.out.println("----------------------");
        System.out.println("Image bytes: " + imageBytes.length);
        String fileName = UUID.randomUUID().toString() + ".png";
        System.out.println("File name: " + fileName);
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        System.out.println("----------------------");
        Path filePath = uploadPath.resolve(fileName);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(imageBytes);
        }
        return "/images/" + fileName; // Đường dẫn public của ảnh
    }

    // Cập nhật ảnh
    @Transactional
    public ProductImageDTO updateProductImage(Long imageId, String base64Image) throws IOException {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return null;
        }

        ProductImage image = productImageRepository.findById(imageId).orElse(null);
        if (image == null) {
            return null;
        }

        // Lưu ảnh từ base64
        String imageUrl = saveImageFromBase64(base64Image);

        image.setImageURL(imageUrl);
        image = productImageRepository.save(image);

        return convertToDTO(image);
    }

    // Xóa ảnh
    @Transactional
    public boolean deleteProductImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId).orElse(null);
        if (image == null) {
            return false;
        }

        productImageRepository.delete(image);
        return true;
    }

    // Xóa tất cả ảnh của một sản phẩm
    @Transactional
    public void deleteAllProductImages(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            productImageRepository.deleteByProduct(product);
        }
    }
}