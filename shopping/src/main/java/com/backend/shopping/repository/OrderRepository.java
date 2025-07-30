package com.backend.shopping.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.shopping.entity.Order;
import com.backend.shopping.entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	  // 주문 단건 조회 시 User와 주문 ID 조건 메서드 (반드시 추가)
    Optional<Order> findByIdAndUser(Long id, User user);

    // 유저별 주문 목록 페이징 조회 메서드 (반드시 Pageable 포함)
    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);

    // 기존 메서드도 필요하다면 유지
    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    // 만약 페이징 필요 없으면 List도 사용 가능
    List<Order> findByUserOrderByOrderDateDesc(User user);
}
