package com.backend.shopping.dto.request;

import org.springframework.data.domain.Sort;

import lombok.Data;

@Data
public class ProductSearchRequest {
    private String name;        // 상품명 검색
    private String category;    // 카테고리 필터
    private Boolean inStock;    // 재고 있는 상품만
    
    // 페이징
    private int page = 0;
    private int size = 20;
    
    // 정렬
    private String sortBy = "createdAt";
    private Sort.Direction sortDirection = Sort.Direction.DESC;
    
    public Sort getSort() {
        return Sort.by(sortDirection, sortBy);
    }
}