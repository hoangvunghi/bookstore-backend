package com.example.bookstore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.ReviewDTO;
import com.example.bookstore.dto.ApiResponse;
import com.example.bookstore.security.UserDetailsImpl;
import com.example.bookstore.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/orders/{orderId}/products/{productId}")
    public ResponseEntity<ApiResponse> createReview(
            Authentication authentication,
            @PathVariable Long orderId,
            @PathVariable Long productId,
            @RequestParam int rating,
            @RequestParam String comment) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ReviewDTO review = reviewService.createReview(
            userDetails.getUser().getUserId(),
            orderId,
            productId,
            rating,
            comment
        );

        if (review == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(new ApiResponse(true, "Tạo đánh giá thành công", review));
    }
} 