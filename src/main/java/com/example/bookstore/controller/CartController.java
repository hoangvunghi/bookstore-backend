package com.example.bookstore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.CartDTO;
import com.example.bookstore.dto.ApiResponse;
import com.example.bookstore.security.UserDetailsImpl;
import com.example.bookstore.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // API lấy giỏ hàng của người dùng hiện tại
    @GetMapping
    public ResponseEntity<CartDTO> getMyCart(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        CartDTO cart = cartService.getCartByUser(userDetails.getUser().getUserId());
        return ResponseEntity.ok(cart);
    }

    // API thêm sản phẩm vào giỏ hàng
    @PostMapping("/items/{productId}")
    public ResponseEntity<CartDTO> addToCart(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        CartDTO updatedCart = cartService.addToCart(userDetails.getUser().getUserId(), productId, quantity);
        
        if (updatedCart == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(updatedCart);
    }

    // API cập nhật số lượng sản phẩm trong giỏ hàng
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartDTO> updateCartItemQuantity(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam int quantity) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        CartDTO updatedCart = cartService.updateCartItemQuantity(
            userDetails.getUser().getUserId(), productId, quantity);
        
        if (updatedCart == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(updatedCart);
    }

    // API xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse> removeFromCart(
            Authentication authentication,
            @PathVariable Long productId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        CartDTO updatedCart = cartService.removeFromCart(userDetails.getUser().getUserId(), productId);
        
        if (updatedCart == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "Xóa sản phẩm khỏi giỏ hàng thành công", updatedCart));
    }

    // API xóa toàn bộ giỏ hàng
    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        cartService.clearCart(userDetails.getUser().getUserId());
        return ResponseEntity.ok(new ApiResponse(true, "Xóa toàn bộ giỏ hàng thành công"));
    }
} 