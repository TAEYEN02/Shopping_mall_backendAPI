package com.backend.shopping.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long id;
    private Long userId;
    private List<CartItemResponse> items;
    private BigDecimal totalPrice;
    private Integer totalQuantity;
    private LocalDateTime createdAt;
    
    // 총 가격 계산
    public BigDecimal calculateTotalPrice() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // 총 수량 계산
    public Integer calculateTotalQuantity() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
        return items.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();
    }
}