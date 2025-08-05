package com.backend.shopping.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.backend.shopping.dto.request.CartItemRequest;
import com.backend.shopping.dto.request.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@Disabled("임시 비활성화 중")
public class CartIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("장바구니 전체 플로우 테스트")
    void cartFullFlowTest() throws Exception {
        // 1. 로그인
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("user123");
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String loginResponse = loginResult.getResponse().getContentAsString();
        // JWT 토큰 추출 로직 (실제 구현 시 JSON 파싱 필요)
        String token = "Bearer extracted_jwt_token";
        
        // 2. 장바구니에 상품 추가
        CartItemRequest addRequest = new CartItemRequest();
        addRequest.setProductId(1L);
        addRequest.setQuantity(2);
        
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        // 3. 장바구니 조회
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalQuantity").value(2));
        
        // 4. 수량 수정
        mockMvc.perform(put("/api/cart/items/1")
                        .header("Authorization", token)
                        .param("quantity", "3"))
                .andExpect(status().isOk());
        
        // 5. 수정된 장바구니 확인
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalQuantity").value(3));
        
        // 6. 아이템 제거
        mockMvc.perform(delete("/api/cart/items/1")
                        .header("Authorization", token))
                .andExpect(status().isOk());
        
        // 7. 빈 장바구니 확인
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalQuantity").value(0));
    }
}