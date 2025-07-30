package com.backend.shopping.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;


import com.backend.shopping.dto.request.OrderRequest;
import com.backend.shopping.dto.response.ApiResponse;
import com.backend.shopping.dto.response.OrderResponse;
import com.backend.shopping.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 관리 API")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("주문이 성공적으로 생성되었습니다.", response));
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 조회", description = "특정 주문의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "주문 ID") @PathVariable(name="orderId") Long orderId) {
        OrderResponse response = orderService.getOrder(userDetails.getUsername(), orderId);
        return ResponseEntity.ok(ApiResponse.success("주문 조회 성공", response));
    }
    
    @GetMapping
    @Operation(summary = "내 주문 목록 조회", description = "현재 사용자의 주문 목록을 페이징으로 조회합니다.")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<OrderResponse> response = orderService.getUserOrders(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success("주문 목록 조회 성공", response));
    }
    
    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "주문 ID") @PathVariable(name="orderId") Long orderId) {
        OrderResponse response = orderService.cancelOrder(userDetails.getUsername(), orderId);
        return ResponseEntity.ok(ApiResponse.success("주문이 취소되었습니다.", response));
    }
    
    // 관리자용 API
    @PutMapping("/{orderId}/status")
    @Operation(summary = "주문 상태 변경", description = "주문 상태를 변경합니다. (관리자용)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @Parameter(description = "주문 ID") @PathVariable(name="orderId") Long orderId,
            @RequestParam(name="status") String status) {
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success("주문 상태가 변경되었습니다.", response));
    }
}
