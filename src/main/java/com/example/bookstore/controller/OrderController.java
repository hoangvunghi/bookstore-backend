package com.example.bookstore.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.ApiResponse;
import com.example.bookstore.dto.OrderDTO;
import com.example.bookstore.security.UserDetailsImpl;
import com.example.bookstore.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // API lấy danh sách đơn hàng của người dùng hiện tại
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse> getMyOrders(
            Authentication authentication,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Page<OrderDTO> orders = orderService.getOrdersByUser(userDetails.getUser().getUserId(), pageable);
        return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách đơn hàng thành công", orders));
    }

    // API lấy chi tiết một đơn hàng
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        OrderDTO order = orderService.getOrderById(orderId);
        
        if (order == null) {
            return ResponseEntity.notFound()
                .build();
        }

        // Kiểm tra xem người dùng có quyền xem đơn hàng này không
        if (!order.getUserId().equals(userDetails.getUser().getUserId()) && 
            !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403)
                .body(new ApiResponse(false, "Không có quyền xem đơn hàng này"));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin đơn hàng thành công", order));
    }

    // API tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(
            Authentication authentication,
            @RequestBody OrderDTO orderDTO) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        orderDTO.setUserId(userDetails.getUser().getUserId());
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        
        if (createdOrder == null) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tạo đơn hàng thất bại"));
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "Tạo đơn hàng thành công", createdOrder));
    }

    // API cập nhật trạng thái đơn hàng (chỉ ADMIN)
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, status);
        
        if (updatedOrder == null) {
            return ResponseEntity.notFound()
                .build();
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật trạng thái đơn hàng thành công", updatedOrder));
    }

    // API hủy đơn hàng
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        OrderDTO order = orderService.getOrderById(orderId);
        
        if (order == null) {
            return ResponseEntity.notFound()
                .build();
        }

        // Kiểm tra xem người dùng có quyền hủy đơn hàng này không
        if (!order.getUserId().equals(userDetails.getUser().getUserId()) && 
            !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403)
                .body(new ApiResponse(false, "Không có quyền hủy đơn hàng này"));
        }

        boolean cancelled = orderService.cancelOrder(orderId);
        if (!cancelled) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Không thể hủy đơn hàng này"));
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "Hủy đơn hàng thành công"));
    }

    // API lấy danh sách đơn hàng theo trạng thái (chỉ ADMIN)
    @GetMapping("/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getOrdersByStatus(
            @RequestParam String status,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<OrderDTO> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách đơn hàng theo trạng thái thành công", orders));
    }
} 