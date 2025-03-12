package com.example.bookstore.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.bookstore.dto.ProductCategoryDTO;
import com.example.bookstore.model.Category;
import com.example.bookstore.model.Product;
import com.example.bookstore.model.ProductCategory;
import com.example.bookstore.repository.CategoryRepository;
import com.example.bookstore.repository.ProductCategoryRepository;
import com.example.bookstore.repository.ProductRepository;

@Service
public class ProductCategoryService {
    
    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    public Page<ProductCategoryDTO> getAllProductCategories(Pageable pageable) {
        Page<ProductCategory> productCategoryPage = productCategoryRepository.findAll(pageable);
        return productCategoryPage.map(this::convertToDTO);
    }

    public List<ProductCategoryDTO> getAllProductCategories() {
        return productCategoryRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public ProductCategoryDTO getProductCategoryById(Long id) {
        Optional<ProductCategory> productCategory = productCategoryRepository.findById(id);
        return productCategory.map(this::convertToDTO).orElse(null);
    }

    public ProductCategoryDTO createProductCategory(ProductCategoryDTO productCategoryDTO) {
        // Kiểm tra xem liên kết này đã tồn tại chưa
        Product product = productRepository.findById(productCategoryDTO.getProductId()).orElse(null);
        Category category = categoryRepository.findById(productCategoryDTO.getCategoryId()).orElse(null);
        
        if (product == null || category == null) {
            return null;
        }
        
        // Kiểm tra xem liên kết này đã tồn tại chưa
        Optional<ProductCategory> existingLink = productCategoryRepository.findByProductAndCategory(product, category);
        if (existingLink.isPresent()) {
            // Nếu đã tồn tại, trả về DTO của liên kết đó
            return convertToDTO(existingLink.get());
        }
        
        // Nếu chưa tồn tại, tạo mới
        ProductCategory productCategory = convertToEntity(productCategoryDTO);
        ProductCategory savedProductCategory = productCategoryRepository.save(productCategory);
        return convertToDTO(savedProductCategory);
    }

    public boolean deleteProductCategory(Long productId, Long categoryId) {
        Product product = productRepository.findById(productId).orElse(null);
        Category category = categoryRepository.findById(categoryId).orElse(null);
        
        if (product == null || category == null) {
            return false;
        }
        
        Optional<ProductCategory> productCategory = productCategoryRepository.findByProductAndCategory(product, category);
        if (productCategory.isPresent()) {
            productCategoryRepository.delete(productCategory.get());
            return true;
        }   
        return false;
    }

    private ProductCategoryDTO convertToDTO(ProductCategory productCategory) {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setProductCategoryId(productCategory.getProductCategoryId());
        dto.setProductId(productCategory.getProduct().getProductId());
        dto.setCategoryId(productCategory.getCategory().getCategoryId());
        return dto;
    }

    private ProductCategory convertToEntity(ProductCategoryDTO dto) {
        ProductCategory productCategory = new ProductCategory();
        
        Product product = productRepository.findById(dto.getProductId()).orElse(null);
        Category category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
        
        if (product != null && category != null) {
            productCategory.setProduct(product);
            productCategory.setCategory(category);
        }
        
        return productCategory;
    }
} 