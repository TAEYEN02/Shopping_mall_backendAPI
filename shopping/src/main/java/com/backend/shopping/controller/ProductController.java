package com.backend.shopping.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.shopping.dto.request.ProductRequest;
import com.backend.shopping.dto.request.ProductSearchRequest;
import com.backend.shopping.dto.response.ApiResponse;
import com.backend.shopping.dto.response.ProductResponse;
import com.backend.shopping.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "상품 관련 API")
public class ProductController {
    
    private final ProductService productService;
    
    // 상품 생성 (관리자만)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "상품 생성", description = "새로운 상품을 등록합니다. 관리자만 가능합니다.")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse response = productService.createProduct(request);
            return ResponseEntity.ok(ApiResponse.success("상품이 등록되었습니다", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ProductResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    // 상품 수정 (관리자만)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다. 관리자만 가능합니다.")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @Parameter(description = "상품 ID") @PathVariable(name="id") Long id,
            @Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse response = productService.updateProduct(id, request);
            return ResponseEntity.ok(ApiResponse.success("상품이 수정되었습니다", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ProductResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    // 상품 삭제 (관리자만)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다. 관리자만 가능합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "상품 ID") @PathVariable(name="id") Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.<Void>success("상품이 삭제되었습니다", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    // 상품 단건 조회
    @GetMapping("/{id}")
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @Parameter(description = "상품 ID") @PathVariable(name="id") Long id) {
        try {
            ProductResponse response = productService.getProduct(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ProductResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    // 상품 목록 조회 (검색, 필터링, 페이징)
    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "상품 목록을 조회합니다. 검색, 필터링, 페이징이 가능합니다.")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @Parameter(description = "상품명 검색") @RequestParam(name="name",required = false) String name,
            @Parameter(description = "카테고리 필터") @RequestParam(name="category",required = false) String category,
            @Parameter(description = "재고 있는 상품만") @RequestParam(name="inStock",required = false) Boolean inStock,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(name="page",defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name="size",defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(name="sortBy",defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(name="sortDirection",defaultValue = "DESC") String sortDirection) {
        
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setName(name);
        searchRequest.setCategory(category);
        searchRequest.setInStock(inStock);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(
                "ASC".equalsIgnoreCase(sortDirection) ? 
                org.springframework.data.domain.Sort.Direction.ASC : 
                org.springframework.data.domain.Sort.Direction.DESC
        );
        
        Page<ProductResponse> response = productService.getProducts(searchRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    // 카테고리 목록 조회
    @GetMapping("/categories")
    @Operation(summary = "카테고리 목록 조회", description = "등록된 상품의 카테고리 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = productService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    // 인기 상품 조회
    @GetMapping("/popular")
    @Operation(summary = "인기 상품 조회", description = "인기 상품 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getPopularProducts(
            @Parameter(description = "조회할 상품 수") @RequestParam(name="limit",defaultValue = "10") int limit) {
        List<ProductResponse> response = productService.getPopularProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
