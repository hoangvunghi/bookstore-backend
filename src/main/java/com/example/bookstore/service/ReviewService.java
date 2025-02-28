package com.example.bookstore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.ReviewDTO;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderDetail;
import com.example.bookstore.model.Product;
import com.example.bookstore.model.Review;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.ProductRepository;
import com.example.bookstore.repository.ReviewRepository;
import com.example.bookstore.repository.UserRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                        OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setUserId(review.getUser().getUserId());
        dto.setProductId(review.getProduct().getProductId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedDate(review.getCreatedDate());
        return dto;
    }

    @Transactional
    public ReviewDTO createReview(Long userId, Long orderId, Long productId, int rating, String comment) {
        // Kiểm tra thông tin đầu vào
        if (rating < 1 || rating > 5 || comment == null || comment.trim().isEmpty()) {
            return null;
        }

        // Kiểm tra user và product có tồn tại không
        User user = userRepository.findById(userId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        Order order = orderRepository.findById(orderId).orElse(null);

        if (user == null || product == null || order == null) {
            return null;
        }

        // Kiểm tra đơn hàng có phải của user này không
        if (!order.getUser().getUserId().equals(userId)) {
            return null;
        }

        // Kiểm tra trạng thái đơn hàng đã hoàn thành chưa
        if (!"DELIVERED".equals(order.getStatus())) {
            return null;
        }

        // Kiểm tra sản phẩm có trong đơn hàng không
        boolean productInOrder = order.getOrderDetails().stream()
            .map(OrderDetail::getProduct)
            .anyMatch(p -> p.getProductId().equals(productId));

        if (!productInOrder) {
            return null;
        }

        // Kiểm tra user đã review sản phẩm này trong đơn hàng này chưa
        boolean hasReviewed = reviewRepository.existsByUserAndProductAndOrder(user, product, order);
        if (hasReviewed) {
            return null;
        }

        // Tạo review mới
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setOrder(order);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedDate(new java.util.Date());

        review = reviewRepository.save(review);
        
        return convertToDTO(review);
    }
} 