package com.backend.shopping.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.shopping.config.SecurityConfig;
import com.backend.shopping.dto.request.CartItemRequest;
import com.backend.shopping.dto.response.CartItemResponse;
import com.backend.shopping.dto.response.CartResponse;
import com.backend.shopping.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Disabled("임시 비활성화 중")
@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class CartControllerTest {
    
	@Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private CartResponse testCartResponse;
    private CartItemRequest testCartItemRequest;
    
    @BeforeEach
    void setUp() {
        CartItemResponse cartItemResponse = CartItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("테스트 상품")
                .productPrice(new BigDecimal("10000"))
                .productStock(100)
                .quantity(2)
                .createdAt(LocalDateTime.now())
                .build();
        
        testCartResponse = CartResponse.builder()
                .id(1L)
                .userId(1L)
                .items(Arrays.asList(cartItemResponse))
                .totalPrice(new BigDecimal("20000"))
                .totalQuantity(2)
                .createdAt(LocalDateTime.now())
                .build();
        
        testCartItemRequest = new CartItemRequest();
        testCartItemRequest.setProductId(1L);
        testCartItemRequest.setQuantity(3);
    }
    
    @Test
    @DisplayName("장바구니에 상품 추가")
    @WithMockUser
    void addItemToCart() throws Exception {
        // given
        given(cartService.addItemToCart(any(CartItemRequest.class)))
                .willReturn(testCartResponse);
        
        // when & then
        mockMvc.perform(post("/api/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCartItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.totalQuantity").value(2))
                .andExpect(jsonPath("$.data.totalPrice").value(20000));
    }
    
    @Test
    @DisplayName("유효하지 않은 데이터로 장바구니 추가 실패")
    @WithMockUser
    void addItemToCart_InvalidData() throws Exception {
        // given - 수량이 0인 잘못된 요청
        CartItemRequest invalidRequest = new CartItemRequest();
        invalidRequest.setProductId(1L);
        invalidRequest.setQuantity(0); // 유효하지 않은 수량
        
        // when & then
        mockMvc.perform(post("/api/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다"));
    }
    
    @Test
    @DisplayName("장바구니 아이템 수량 수정")
    @WithMockUser
    void updateCartItem() throws Exception {
        // given
        given(cartService.updateCartItem(1L, 5)).willReturn(testCartResponse);
        
        // when & then
        mockMvc.perform(put("/api/cart/items/1")
                        .with(csrf())
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("수량이 수정되었습니다"));
    }
    
    @Test
    @DisplayName("잘못된 수량으로 아이템 수정 실패")
    @WithMockUser
    void updateCartItem_InvalidQuantity() throws Exception {
        // when & then
        mockMvc.perform(put("/api/cart/items/1")
                        .with(csrf())
                        .param("quantity", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("수량은 1개 이상이어야 합니다"));
    }
    
    @Test
    @DisplayName("장바구니 아이템 제거")
    @WithMockUser
    void removeItemFromCart() throws Exception {
        // given
        given(cartService.removeItemFromCart(1L)).willReturn(testCartResponse);
        
        // when & then
        mockMvc.perform(delete("/api/cart/items/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품이 장바구니에서 제거되었습니다"));
    }
    
    @Test
    @DisplayName("장바구니 전체 비우기")
    @WithMockUser
    void clearCart() throws Exception {
        // given
        doNothing().when(cartService).clearCart();
        
        // when & then
        mockMvc.perform(delete("/api/cart")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니가 비워졌습니다"));
    }
    
    @Test
    @DisplayName("내 장바구니 조회")
    @WithMockUser
    void getMyCart() throws Exception {
        // given
        given(cartService.getMyCart()).willReturn(testCartResponse);
        
        // when & then
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].productName").value("테스트 상품"));
    }
    
    @Test
    @DisplayName("장바구니 아이템 수 조회")
    @WithMockUser
    void getCartItemCount() throws Exception {
        // given
        given(cartService.getCartItemCount()).willReturn(5);
        
        // when & then
        mockMvc.perform(get("/api/cart/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }
    
    @Test
    @DisplayName("인증되지 않은 사용자의 장바구니 접근 거부")
    void accessCart_Unauthenticated() throws Exception {
        // when & then
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }
}
