package com.example.bookstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.example.bookstore.service.VNPAYService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @Autowired
    private VNPAYService vnPayService;

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
        response.put("orderId", request.getParameter("vnp_OrderInfo"));
        response.put("totalPrice", request.getParameter("vnp_Amount"));
        response.put("paymentTime", request.getParameter("vnp_PayDate"));
        response.put("transactionId", request.getParameter("vnp_TransactionNo"));
        
        if (paymentStatus == 1) {
            response.put("status", "success");
            response.put("message", "Thanh toán thành công");
        } else if (paymentStatus == 0) {
            response.put("status", "failed");
            response.put("message", "Thanh toán thất bại");
        } else {
            response.put("status", "invalid");
            response.put("message", "Chữ ký không hợp lệ");
        }
        
        return ResponseEntity.ok(response);
    }
}