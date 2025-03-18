package com.example.bookstore.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestParam("amount") int orderTotal,
                                         @RequestParam("orderInfo") String orderInfo,
                                         HttpServletRequest request) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String vnpayUrl = vnPayService.createOrder(request, orderTotal, orderInfo, baseUrl);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Tạo URL thanh toán thành công",
                "paymentUrl", vnpayUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
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
}