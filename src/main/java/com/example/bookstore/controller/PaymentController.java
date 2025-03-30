package com.example.bookstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.OrderPaymentRequest;
import com.example.bookstore.dto.PaymentResponse;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.Payment;
import com.example.bookstore.model.PaymentMethod;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.service.PaymentService;
import com.example.bookstore.service.VNPAYService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @Autowired
    private VNPAYService vnPayService;
    
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<PaymentResponse> processOrderPayment(
            @RequestBody OrderPaymentRequest request,
            HttpServletRequest servletRequest) {
        
        // Lấy thông tin đơn hàng
        Order order = orderService.getOrderEntity(request.getOrderId());
        if (order == null) {
            return ResponseEntity.badRequest()
                .body(new PaymentResponse("error", "Không tìm thấy đơn hàng", null, request.getOrderId()));
        }

        // Tạo payment
        Payment payment = paymentService.createPayment(order, request.getPaymentMethod());

        // Xử lý theo phương thức thanh toán
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            try {
                // Tạo URL thanh toán VNPAY
                String baseUrl = servletRequest.getScheme() + "://" + servletRequest.getServerName() + ":" + servletRequest.getServerPort();
                String vnpayUrl = vnPayService.createOrder(servletRequest, order.getTotalAmount(), 
                    String.valueOf(order.getOrderId()), baseUrl);
                
                return ResponseEntity.ok(new PaymentResponse(
                    "success",
                    "Vui lòng thanh toán qua VNPAY",
                    vnpayUrl,
                    order.getOrderId()
                ));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                    .body(new PaymentResponse("error", "Lỗi khi tạo URL thanh toán", null, order.getOrderId()));
            }
        } else if (request.getPaymentMethod() == PaymentMethod.COD) {
            // Cập nhật trạng thái đơn hàng sang CONFIRMED với COD
            orderService.updateOrderStatus(order.getOrderId(), "CONFIRMED");
            return ResponseEntity.ok(new PaymentResponse(
                "success",
                "Đơn hàng sẽ được thanh toán khi nhận hàng",
                null,
                order.getOrderId()
            ));
        }

        return ResponseEntity.ok(new PaymentResponse(
            "success",
            "Đã cập nhật phương thức thanh toán",
            null,
            order.getOrderId()
        ));
    }

    // @GetMapping("/vnpay-return")
    // public ResponseEntity<ApiResponse> paymentReturn(HttpServletRequest request) {
    //     try {
    //         // Lấy thông tin từ request
    //         String orderId = request.getParameter("vnp_OrderInfo");
    //         String amount = request.getParameter("vnp_Amount");
    //         String transactionId = request.getParameter("vnp_TransactionNo");
    //         String paymentDate = request.getParameter("vnp_PayDate");
    //         String responseCode = request.getParameter("vnp_ResponseCode");
            
    //         // Kiểm tra kết quả giao dịch
    //         if ("00".equals(responseCode)) {
    //             // Thanh toán thành công
    //             boolean processSuccess = paymentService.processSuccessfulPayment(
    //                 orderId, amount, transactionId, paymentDate);
                
    //             if (processSuccess) {
    //                 return ResponseEntity.ok(new ApiResponse(true, "Thanh toán thành công"));
    //             } else {
    //                 return ResponseEntity.ok(new ApiResponse(false, "Thanh toán thành công nhưng xử lý đơn hàng thất bại"));
    //             }
    //         } else {
    //             // Thanh toán thất bại
    //             paymentService.processFailedPayment(orderId);
    //             return ResponseEntity.ok(new ApiResponse(false, "Thanh toán thất bại"));
    //         }
    //     } catch (Exception e) {
    //         return ResponseEntity.internalServerError().body(new ApiResponse(false, "Lỗi xử lý thanh toán: " + e.getMessage()));
    //     }
    // }

    @GetMapping("/vnpay-return")
public ResponseEntity<Object> paymentReturn(HttpServletRequest request) {
    try {
        // Lấy thông tin từ request
        String orderId = request.getParameter("vnp_OrderInfo");
        String amount = request.getParameter("vnp_Amount");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String paymentDate = request.getParameter("vnp_PayDate");
        String responseCode = request.getParameter("vnp_ResponseCode");
        
        // Khởi tạo URL redirect mặc định
        String redirectUrl = "http://localhost:5173/payment-failed";
        
        // Kiểm tra kết quả giao dịch
        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            boolean processSuccess = paymentService.processSuccessfulPayment(
                orderId, amount, transactionId, paymentDate);
            
            if (processSuccess) {
                // Redirect đến trang thông báo thanh toán thành công
                redirectUrl = "http://localhost:5173/payment-success?orderId=" + orderId;
            } else {
                // Có lỗi xử lý sau thanh toán
                redirectUrl = "http://localhost:5173/payment-failed?reason=process-failed";
            }
        } else {
            // Thanh toán thất bại
            paymentService.processFailedPayment(orderId);
            redirectUrl = "http://localhost:5173/payment-failed?reason=payment-failed&code=" + responseCode;
        }
        
        // Thực hiện chuyển hướng HTTP
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Location", redirectUrl);
        return new ResponseEntity<>(headers, org.springframework.http.HttpStatus.FOUND);
    } catch (Exception e) {
        // Nếu có lỗi, redirect đến trang lỗi
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Location", "http://localhost:5173/payment-failed?reason=server-error");
        return new ResponseEntity<>(headers, org.springframework.http.HttpStatus.FOUND);
    }
}
    @PostMapping("/retry/{orderId}")
    public ResponseEntity<PaymentResponse> retryPayment(
            @PathVariable Long orderId,
            HttpServletRequest servletRequest) {
        
        // Lấy thông tin đơn hàng
        Order order = orderService.getOrderEntity(orderId);
        if (order == null) {
            return ResponseEntity.badRequest()
                .body(new PaymentResponse("error", "Không tìm thấy đơn hàng", null, orderId));
        }

        // Kiểm tra trạng thái đơn hàng
        if (order.getStatus().equals("PAID") || order.getStatus().equals("DELIVERED")) {
            return ResponseEntity.badRequest()
                .body(new PaymentResponse("error", "Đơn hàng đã được thanh toán", null, orderId));
        }

        try {
            // Tạo URL thanh toán VNPAY mới
            String baseUrl = servletRequest.getScheme() + "://" + servletRequest.getServerName() + ":" + servletRequest.getServerPort();
            String vnpayUrl = vnPayService.createOrder(servletRequest, order.getTotalAmount(), 
                String.valueOf(order.getOrderId()), baseUrl);
            
            // Cập nhật trạng thái payment về PENDING
            paymentService.updatePaymentStatus(orderId, "PENDING");
            
            return ResponseEntity.ok(new PaymentResponse(
                "success",
                "Vui lòng thử thanh toán lại qua VNPAY",
                vnpayUrl,
                orderId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new PaymentResponse("error", "Lỗi khi tạo URL thanh toán mới", null, orderId));
        }
    }
}