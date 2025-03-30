package com.example.bookstore.dto;

import java.util.List;

public class CreateOrderRequest {
    private List<OrderDetailDTO> orderDetails;
    private boolean useUserAddress;
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;

    public List<OrderDetailDTO> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetailDTO> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public boolean isUseUserAddress() {
        return useUserAddress;
    }

    public void setUseUserAddress(boolean useUserAddress) {
        this.useUserAddress = useUserAddress;
    }

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
} 