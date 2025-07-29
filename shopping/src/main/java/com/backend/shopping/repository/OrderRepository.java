package com.backend.shopping.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.shopping.entity.Order;
import com.backend.shopping.entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    List<Order> findByUserOrderByOrderDateDesc(User user);
}
