package com.example.bookstore.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PaymentRequest {
    private Long amount;
    private String orderType;
    private String orderInfo;
    private String bankCode;
    private String language;
}