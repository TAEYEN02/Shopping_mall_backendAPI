package com.backend.shopping.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.shopping.dto.request.ProductRequest;
import com.backend.shopping.dto.response.ProductResponse;
import com.backend.shopping.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Disabled("임시 비활성화 중")
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProductService productService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ProductResponse testProductResponse;
    private ProductRequest testProductRequest;
    
    @BeforeEach
    void setUp() {
        testProductResponse = ProductResponse.builder()
                .id(1L)
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(new BigDecimal("10000"))
                .stock(100)
                .category("Electronics")
                .imageUrl("http://example.com/image.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        testProductRequest = new ProductRequest();
        testProductRequest.setName("테스트 상품");
        testProductRequest.setDescription("테스트 상품 설명");
        testProductRequest.setPrice(new BigDecimal("10000"));
        testProductRequest.setStock(100);
        testProductRequest.setCategory("Electronics");
        testProductRequest.setImageUrl("http://example.com/image.jpg");
    }
    
    @Test
    @DisplayName("상품 생성 - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void createProduct_WithAdminRole() throws Exception {
        // given
        given(productService.createProduct(any(ProductRequest.class)))
                .willReturn(testProductResponse);
        
        // when & then
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("테스트 상품"))
                .andExpect(jsonPath("$.data.price").value(10000));
    }
    
    @Test
    @DisplayName("상품 생성 - 일반 사용자 권한 (접근 거부)")
    @WithMockUser(roles = "USER")
    void createProduct_WithUserRole_AccessDenied() throws Exception {
        // when & then
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("상품 단건 조회")
    void getProduct() throws Exception {
        // given
        given(productService.getProduct(1L)).willReturn(testProductResponse);
        
        // when & then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("테스트 상품"));
    }
    
    @Test
    @DisplayName("상품 목록 조회")
    void getProducts() throws Exception {
        // given
        List<ProductResponse> products = Arrays.asList(testProductResponse);
        Page<ProductResponse> productPage = new PageImpl<>(products);
        
        given(productService.getProducts(any())).willReturn(productPage);
        
        // when & then
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("테스트 상품"));
    }
    
    @Test
    @DisplayName("카테고리 목록 조회")
    void getCategories() throws Exception {
        // given
        List<String> categories = Arrays.asList("Electronics", "Fashion", "Books");
        given(productService.getCategories()).willReturn(categories);
        
        // when & then
        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0]").value("Electronics"));
    }
    
    @Test
    @DisplayName("인기 상품 조회")
    void getPopularProducts() throws Exception {
        // given
        List<ProductResponse> popularProducts = Arrays.asList(testProductResponse);
        given(productService.getPopularProducts(10)).willReturn(popularProducts);
        
        // when & then
        mockMvc.perform(get("/api/products/popular")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("테스트 상품"));
    }
    
    @Test
    @DisplayName("유효하지 않은 상품 데이터로 생성 시 실패")
    @WithMockUser(roles = "ADMIN")
    void createProduct_InvalidData() throws Exception {
        // given - 빈 이름으로 잘못된 요청
        ProductRequest invalidRequest = new ProductRequest();
        invalidRequest.setName(""); // 빈 이름
        invalidRequest.setPrice(new BigDecimal("10000"));
        invalidRequest.setStock(100);
        
        // when & then
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다"));
    }
}
