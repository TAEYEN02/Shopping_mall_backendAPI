package com.backend.shopping.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.shopping.dto.request.ProductRequest;
import com.backend.shopping.dto.request.ProductSearchRequest;
import com.backend.shopping.dto.response.ProductResponse;
import com.backend.shopping.entity.Product;
import com.backend.shopping.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    
    // 상품 생성 (관리자만)
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .build();
        
        Product savedProduct = productRepository.save(product);
        return convertToResponse(savedProduct);
    }
    
    // 상품 수정 (관리자만)
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        
        Product updatedProduct = productRepository.save(product);
        return convertToResponse(updatedProduct);
    }
    
    // 상품 삭제 (관리자만)
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));
        
        productRepository.delete(product);
    }
    
    // 상품 단건 조회
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));
        
        return convertToResponse(product);
    }
    
    // 상품 목록 조회 (검색, 필터링, 페이징)
    public Page<ProductResponse> getProducts(ProductSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(), 
                searchRequest.getSize(), 
                searchRequest.getSort()
        );
        
        Page<Product> products;
        
        // 검색 조건에 따른 쿼리 실행
        if (searchRequest.getName() != null && !searchRequest.getName().trim().isEmpty()) {
            // 상품명 검색
            products = productRepository.findByNameContainingIgnoreCase(
                    searchRequest.getName().trim(), pageable);
        } else if (searchRequest.getCategory() != null && !searchRequest.getCategory().trim().isEmpty()) {
            // 카테고리 필터
            products = productRepository.findByCategory(searchRequest.getCategory(), pageable);
        } else if (searchRequest.getInStock() != null && searchRequest.getInStock()) {
            // 재고 있는 상품만
            products = productRepository.findAvailableProducts(pageable);
        } else {
            // 전체 상품
            products = productRepository.findAll(pageable);
        }
        
        return products.map(this::convertToResponse);
    }
    
    // 카테고리 목록 조회
    public List<String> getCategories() {
        return productRepository.findDistinctCategories();
    }
    
    // 인기 상품 조회 (재고 많은 순으로 임시 구현)
    public List<ProductResponse> getPopularProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findAvailableProducts(pageable)
                .getContent()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // 재고 확인
    public boolean isInStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));
        
        return product.getStock() >= quantity;
    }
    
    // 재고 차감 (주문 시 사용)
    @Transactional
    public void reduceStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));
        
        if (product.getStock() < quantity) {
            throw new RuntimeException("재고가 부족합니다");
        }
        
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
    
    // 재고 복구 (주문 취소 시 사용)
    @Transactional
    public void restoreStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));
        
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }
    
    // Entity -> Response DTO 변환
    private ProductResponse convertToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
