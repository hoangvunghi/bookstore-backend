package com.example.bookstore.controller;

import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/vnpay-return")
    public ResponseEntity<?> paymentReturn(HttpServletRequest request) {
        int paymentStatus = vnPayService.orderReturn(request);
        
        Map<String, Object> response = new HashMap<>();
        String orderId = request.getParameter("vnp_OrderInfo");
        String amount = request.getParameter("vnp_Amount");
        String paymentDate = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        
        response.put("orderId", orderId);
        response.put("totalPrice", amount);
        response.put("paymentTime", paymentDate);
        response.put("transactionId", transactionId);
        
        if (paymentStatus == 1) {
            // Xử lý thanh toán thành công
            boolean processSuccess = paymentService.processSuccessfulPayment(
                orderId, amount, transactionId, paymentDate);
            
            if (processSuccess) {
                response.put("status", "success");
                response.put("message", "Thanh toán thành công");
            } else {
                response.put("status", "error");
                response.put("message", "Thanh toán thành công nhưng xử lý đơn hàng thất bại");
            }
        } else if (paymentStatus == 0) {
            // Xử lý thanh toán thất bại
            paymentService.processFailedPayment(orderId);
            response.put("status", "failed");
            response.put("message", "Thanh toán thất bại");
        } else {
            response.put("status", "invalid");
            response.put("message", "Chữ ký không hợp lệ");
        }
        return ResponseEntity.ok(response);
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