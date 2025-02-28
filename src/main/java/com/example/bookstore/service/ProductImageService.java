package com.example.bookstore.service;

import java.util.List;
import java.util.stream.Collectors;

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

    // Thêm ảnh mới cho sản phẩm
    @Transactional
    public ProductImageDTO addProductImage(Long productId, String imageURL) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || imageURL == null || imageURL.trim().isEmpty()) {
            return null;
        }

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageURL(imageURL);
        image = productImageRepository.save(image);

        return convertToDTO(image);
    }

    // Cập nhật ảnh
    @Transactional
    public ProductImageDTO updateProductImage(Long imageId, String imageURL) {
        if (imageURL == null || imageURL.trim().isEmpty()) {
            return null;
        }

        ProductImage image = productImageRepository.findById(imageId).orElse(null);
        if (image == null) {
            return null;
        }

        image.setImageURL(imageURL);
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