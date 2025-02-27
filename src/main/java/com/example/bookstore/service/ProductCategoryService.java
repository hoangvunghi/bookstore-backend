package com.example.bookstore.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.bookstore.dto.ProductCategoryDTO;
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
        ProductCategory productCategory = convertToEntity(productCategoryDTO);
        ProductCategory savedProductCategory = productCategoryRepository.save(productCategory);
        return convertToDTO(savedProductCategory);
    }

    public ProductCategoryDTO updateProductCategory(Long id, ProductCategoryDTO productCategoryDTO) {
        if (productCategoryRepository.existsById(id)) {
            ProductCategory productCategory = convertToEntity(productCategoryDTO);
            productCategory.setProductCategoryId(id);
            ProductCategory updatedProductCategory = productCategoryRepository.save(productCategory);
            return convertToDTO(updatedProductCategory);
        }
        return null;
    }

    public boolean deleteProductCategory(Long id) {
        if (productCategoryRepository.existsById(id)) {
            productCategoryRepository.deleteById(id);
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
        productCategory.setProductCategoryId(dto.getProductCategoryId());
        productCategory.setProduct(productRepository.findById(dto.getProductId()).orElse(null));
        productCategory.setCategory(categoryRepository.findById(dto.getCategoryId()).orElse(null));
        return productCategory;
    }
} 