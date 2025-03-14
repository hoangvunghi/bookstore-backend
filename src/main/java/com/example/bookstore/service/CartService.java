package com.example.bookstore.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.CartDTO;
import com.example.bookstore.dto.CartDetailDTO;
import com.example.bookstore.model.Cart;
import com.example.bookstore.model.CartDetail;
import com.example.bookstore.model.Product;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.CartDetailRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.ProductRepository;
import com.example.bookstore.repository.UserRepository;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository,
                      CartDetailRepository cartDetailRepository,
                      UserRepository userRepository,
                      ProductRepository productRepository,
                      ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    // Chuyển đổi Cart thành CartDTO
    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getCartId());
        dto.setUserId(cart.getUser().getUserId());
        dto.setTotalAmount(cart.getTotalAmount());
        
        List<CartDetailDTO> detailDTOs = cart.getCartDetails().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        dto.setCartDetails(detailDTOs);
        
        return dto;
    }

    // Chuyển đổi CartDetail thành CartDetailDTO
    private CartDetailDTO convertToDTO(CartDetail detail) {
        CartDetailDTO dto = new CartDetailDTO();
        dto.setCartDetailId(detail.getCartDetailId());
        dto.setCartId(detail.getCart().getCartId());
        dto.setProductId(detail.getProduct().getProductId());
        dto.setProductName(detail.getProduct().getName());
        dto.setPrice(detail.getProduct().getPrice());
        dto.setQuantity(detail.getQuantity());
        
        // Thêm URL ảnh đầu tiên của sản phẩm
        String imageUrl = productService.getFirstProductImage(detail.getProduct().getProductId());
        dto.setProductImageUrl(imageUrl);
        
        return dto;
    }

    // Lấy giỏ hàng của người dùng
    public CartDTO getCartByUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        Cart cart = cartRepository.findByUser(user)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setCartDetails(new ArrayList<>());
                return cartRepository.save(newCart);
            });

        return convertToDTO(cart);
    }

    private CartDetail createNewCartDetail(Cart cart, Product product) {
        CartDetail newDetail = new CartDetail();
        newDetail.setCart(cart);
        newDetail.setProduct(product);
        newDetail.setQuantity(0);
        return newDetail;
    }

    // Thêm sản phẩm vào giỏ hàng
    @Transactional
    public CartDTO addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            return null;
        }

        User user = userRepository.findById(userId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        if (user == null || product == null) {
            return null;
        }

        Cart cart = cartRepository.findByUser(user)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setCartDetails(new ArrayList<>());
                return cartRepository.save(newCart);
            });

        final Cart finalCart = cart;
        final Product finalProduct = product;
        CartDetail detail = cartDetailRepository.findByCartAndProduct(cart, product)
            .orElseGet(() -> createNewCartDetail(finalCart, finalProduct));

        detail.setQuantity(detail.getQuantity() + quantity);
        cartDetailRepository.save(detail);

        cart.calculateTotalAmount();
        cart = cartRepository.save(cart);

        return convertToDTO(cart);
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    @Transactional
    public CartDTO updateCartItemQuantity(Long userId, Long productId, int quantity) {
        if (quantity < 0) {
            return null;
        }

        User user = userRepository.findById(userId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        if (user == null || product == null) {
            return null;
        }

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            return null;
        }

        CartDetail detail = cartDetailRepository.findByCartAndProduct(cart, product).orElse(null);
        if (detail == null) {
            return null;
        }

        if (quantity == 0) {
            // Xóa sản phẩm khỏi giỏ hàng
            cartDetailRepository.delete(detail);
        } else {
            // Cập nhật số lượng
            detail.setQuantity(quantity);
            cartDetailRepository.save(detail);
        }

        // Cập nhật tổng tiền
        cart.calculateTotalAmount();
        cart = cartRepository.save(cart);

        return convertToDTO(cart);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @Transactional
    public CartDTO removeFromCart(Long userId, Long productId) {
        // Kiểm tra user tồn tại
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            System.out.println("Không tìm thấy người dùng với ID: " + userId);
            return null;
        }

        // Kiểm tra giỏ hàng tồn tại
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            System.out.println("Không tìm thấy giỏ hàng cho người dùng: " + userId);
            return null;
        }

        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            System.out.println("Không tìm thấy sản phẩm với ID: " + productId);
            // Nếu sản phẩm không tồn tại, vẫn xóa khỏi giỏ hàng nếu có
            List<CartDetail> detailsToRemove = cart.getCartDetails().stream()
                    .filter(detail -> detail.getProduct() != null && 
                            detail.getProduct().getProductId().equals(productId))
                    .collect(Collectors.toList());
            
            if (!detailsToRemove.isEmpty()) {
                for (CartDetail detail : detailsToRemove) {
                    cartDetailRepository.delete(detail);
                }
                cart.calculateTotalAmount();
                cart = cartRepository.save(cart);
                System.out.println("Đã xóa sản phẩm không tồn tại khỏi giỏ hàng");
            }
            return convertToDTO(cart);
        }

        // Kiểm tra sản phẩm có trong giỏ hàng không
        CartDetail detail = cartDetailRepository.findByCartAndProduct(cart, product).orElse(null);
        if (detail == null) {
            System.out.println("Sản phẩm " + productId + " không có trong giỏ hàng của người dùng " + userId);
            return convertToDTO(cart);
        }

        // Xóa sản phẩm khỏi giỏ hàng
        cartDetailRepository.delete(detail);
        cart.calculateTotalAmount();
        cart = cartRepository.save(cart);
        System.out.println("Đã xóa sản phẩm " + productId + " khỏi giỏ hàng của người dùng " + userId);

        return convertToDTO(cart);
    }

    // Xóa toàn bộ giỏ hàng
    @Transactional
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart != null) {
            cart.getCartDetails().clear();
            cart.setTotalAmount(0);
            cartRepository.save(cart);
        }
    }
} 