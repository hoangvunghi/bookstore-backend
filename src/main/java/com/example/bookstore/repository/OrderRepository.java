package com.example.bookstore.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bookstore.model.Order;
import com.example.bookstore.model.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    Page<Order> findByUser(User user, Pageable pageable);
    List<Order> findByStatus(String status);
    Page<Order> findByStatus(String status, Pageable pageable);
    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);
    List<Order> findByOrderDateBetween(Date startDate, Date endDate);
    List<Order> findByOrderDateBetweenAndStatus(Date startDate, Date endDate, String status);
} 