package com.backend.shopping.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.backend.shopping.dto.request.ProductRequest;
import com.backend.shopping.dto.request.ProductSearchRequest;
import com.backend.shopping.dto.response.ProductResponse;
import com.backend.shopping.entity.Product;
import com.backend.shopping.repository.ProductRepository;

//서비스단위테스트
@ExtendWith(MockitoExtension.class)
@Disabled("임시 비활성화 중")
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    private Product testProduct;
    private ProductRequest testRequest;
    
    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
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
        
        testRequest = new ProductRequest();
        testRequest.setName("테스트 상품");
        testRequest.setDescription("테스트 상품 설명");
        testRequest.setPrice(new BigDecimal("10000"));
        testRequest.setStock(100);
        testRequest.setCategory("Electronics");
        testRequest.setImageUrl("http://example.com/image.jpg");
    }
    
    @Test
    @DisplayName("상품 생성 성공")
    void createProduct_Success() {
        // given
        given(productRepository.save(any(Product.class))).willReturn(testProduct);
        
        // when
        ProductResponse response = productService.createProduct(testRequest);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("테스트 상품");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("10000"));
        
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("상품 조회 성공")
    void getProduct_Success() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        
        // when
        ProductResponse response = productService.getProduct(1L);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("테스트 상품");
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 조회 시 예외 발생")
    void getProduct_NotFound() {
        // given
        given(productRepository.findById(999L)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> productService.getProduct(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("상품을 찾을 수 없습니다");
    }
    
    @Test
    @DisplayName("상품 목록 조회 성공")
    void getProducts_Success() {
        // given
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        
        given(productRepository.findAll(any(Pageable.class))).willReturn(productPage);
        
        // when
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        Page<ProductResponse> response = productService.getProducts(searchRequest);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("테스트 상품");
    }
    
    @Test
    @DisplayName("재고 확인 - 충분한 재고")
    void isInStock_Sufficient() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        
        // when
        boolean result = productService.isInStock(1L, 50);
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("재고 확인 - 부족한 재고")
    void isInStock_Insufficient() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        
        // when
        boolean result = productService.isInStock(1L, 150);
        
        // then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("재고 차감 성공")
    void reduceStock_Success() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(productRepository.save(any(Product.class))).willReturn(testProduct);
        
        // when
        productService.reduceStock(1L, 30);
        
        // then
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("재고 차감 실패 - 부족한 재고")
    void reduceStock_InsufficientStock() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        
        // when & then
        assertThatThrownBy(() -> productService.reduceStock(1L, 150))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("재고가 부족합니다");
    }
}
