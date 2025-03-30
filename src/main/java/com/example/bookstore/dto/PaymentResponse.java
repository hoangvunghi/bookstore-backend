package com.example.bookstore.dto;

public class PaymentResponse {
    private String status;
    private String message;
    private String paymentUrl;  // URL thanh toán cho VNPAY
    private Long orderId;

    public PaymentResponse(String status, String message, String paymentUrl, Long orderId) {
        this.status = status;
        this.message = message;
        this.paymentUrl = paymentUrl;
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
} 