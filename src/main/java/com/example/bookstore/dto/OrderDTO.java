package com.example.bookstore.dto;

import java.util.Date;
import java.util.List;

public class OrderDTO {
    private Long orderId;
    private Long userId;
    private Date orderDate;
    private int totalAmount;
    private String status;
    
    // Thông tin địa chỉ nhận hàng
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private boolean useUserAddress;
    
    private List<OrderDetailDTO> orderDetails;

    // Constructors
    public OrderDTO() {}

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderDetailDTO> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetailDTO> orderDetails) {
        this.orderDetails = orderDetails;
    }
    
    // Getters và setters cho thông tin địa chỉ nhận hàng
    public String getShippingName() {
        return shippingName;
    }

    public void setShippingName(String shippingName) {
        this.shippingName = shippingName;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public boolean isUseUserAddress() {
        return useUserAddress;
    }

    public void setUseUserAddress(boolean useUserAddress) {
        this.useUserAddress = useUserAddress;
    }
} 