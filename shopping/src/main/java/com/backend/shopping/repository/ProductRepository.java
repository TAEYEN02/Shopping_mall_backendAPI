package com.backend.shopping.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.backend.shopping.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // 카테고리별 상품 조회
    Page<Product> findByCategory(String category, Pageable pageable);
    
    // 상품명으로 검색
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // 재고가 있는 상품만 조회
    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    Page<Product> findAvailableProducts(Pageable pageable);
    
    // 카테고리 목록 조회
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL")
    List<String> findDistinctCategories();
}
