package com.backend.shopping.controller;

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

import com.backend.shopping.dto.request.CartItemRequest;
import com.backend.shopping.dto.response.ApiResponse;
import com.backend.shopping.dto.response.CartResponse;
import com.backend.shopping.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Cart", description = "장바구니 관련 API")
public class CartController {
    
    private final CartService cartService;
    
    // 장바구니에 상품 추가
    @PostMapping("/items")
    @Operation(summary = "장바구니에 상품 추가", description = "장바구니에 새로운 상품을 추가하거나 기존 상품의 수량을 증가시킵니다.")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(
            @Valid @RequestBody CartItemRequest request) {
        try {
            CartResponse response = cartService.addItemToCart(request);
            return ResponseEntity.ok(ApiResponse.success("상품이 장바구니에 추가되었습니다", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<CartResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    // 장바구니 아이템 수량 수정
    @PutMapping("/items/{itemId}")
    @Operation(summary = "장바구니 아이템 수량 수정", description = "장바구니에 있는 특정 아이템의 수량을 수정합니다.")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @Parameter(description = "장바구니 아이템 ID") @PathVariable(name="itemId") Long itemId,
            @Parameter(description = "변경할 수량") @RequestParam(name = "quantity")  Integer quantity) {
        try {
            if (quantity <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<CartResponse>builder()
                                .success(false)
                                .message("수량은 1개 이상이어야 합니다")
                                .build());
            }
            
            CartResponse response = cartService.updateCartItem(itemId, quantity);
            return ResponseEntity.ok(ApiResponse.success("수량이 수정되었습니다", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<CartResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    // 장바구니에서 아이템 제거
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "장바구니 아이템 제거", description = "장바구니에서 특정 아이템을 제거합니다.")
    public ResponseEntity<ApiResponse<CartResponse>> removeItemFromCart(
            @Parameter(description = "장바구니 아이템 ID") @PathVariable(name="itemId") Long itemId) {
        try {
            CartResponse response = cartService.removeItemFromCart(itemId);
            return ResponseEntity.ok(ApiResponse.success("상품이 장바구니에서 제거되었습니다", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<CartResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    // 장바구니 전체 비우기
    @DeleteMapping
    @Operation(summary = "장바구니 전체 비우기", description = "장바구니의 모든 아이템을 제거합니다.")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        try {
            cartService.clearCart();
            return ResponseEntity.ok(ApiResponse.<Void>success("장바구니가 비워졌습니다", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    // 내 장바구니 조회
    @GetMapping
    @Operation(summary = "내 장바구니 조회", description = "현재 사용자의 장바구니 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart() {
        try {
            CartResponse response = cartService.getMyCart();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<CartResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    // 장바구니 아이템 수 조회
    @GetMapping("/count")
    @Operation(summary = "장바구니 아이템 수 조회", description = "장바구니에 있는 전체 아이템 수량을 조회합니다.")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount() {
        try {
            Integer count = cartService.getCartItemCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Integer>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
}
