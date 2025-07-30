package com.backend.shopping.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal productPrice;
    private Integer productStock;
    private Integer quantity;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
    
    // 소계 계산 (가격 × 수량)
    public BigDecimal getSubtotal() {
        if (productPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return productPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    // 재고 부족 여부 확인
    public boolean isOutOfStock() {
        return productStock == null || productStock < quantity;
    }
}
