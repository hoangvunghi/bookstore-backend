package com.example.bookstore.dto.statistics;

import java.util.Date;

import lombok.Data;

@Data
public class UserRegistrationDTO {
    private Date date;
    private Long totalRegistrations;
    private Long activeUsers;
    private Long inactiveUsers;
} 