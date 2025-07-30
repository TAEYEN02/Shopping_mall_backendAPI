package com.backend.shopping.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.shopping.dto.request.CartItemRequest;
import com.backend.shopping.dto.response.CartResponse;
import com.backend.shopping.entity.Cart;
import com.backend.shopping.entity.CartItem;
import com.backend.shopping.entity.Product;
import com.backend.shopping.entity.Role;
import com.backend.shopping.entity.User;
import com.backend.shopping.repository.CartItemRepository;
import com.backend.shopping.repository.CartRepository;
import com.backend.shopping.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    
    @Mock
    private CartRepository cartRepository;
    
    @Mock
    private CartItemRepository cartItemRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private AuthService authService;
    
    @InjectMocks
    private CartService cartService;
    
    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;
    private CartItemRequest testRequest;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스트유저")
                .role(Role.USER)
                .build();
        
        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();
        
        testProduct = Product.builder()
                .id(1L)
                .name("테스트 상품")
                .price(new BigDecimal("10000"))
                .stock(100)
                .category("Electronics")
                .build();
        
        testCartItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .createdAt(LocalDateTime.now())
                .build();
        
        testRequest = new CartItemRequest();
        testRequest.setProductId(1L);
        testRequest.setQuantity(3);
    }
    
    @Test
    @DisplayName("장바구니에 새 상품 추가 성공")
    void addItemToCart_NewItem_Success() {
        // given
        given(authService.getCurrentUser()).willReturn(testUser);
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(cartItemRepository.findByCartAndProduct(testCart, testProduct))
                .willReturn(Optional.empty());
        given(cartItemRepository.save(any(CartItem.class))).willReturn(testCartItem);
        given(cartItemRepository.findByCart(testCart)).willReturn(Arrays.asList(testCartItem));
        
        // when
        CartResponse response = cartService.addItemToCart(testRequest);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getItems()).hasSize(1);
        
        verify(cartItemRepository).save(any(CartItem.class));
    }
    
    @Test
    @DisplayName("장바구니에 기존 상품 추가 - 수량 증가")
    void addItemToCart_ExistingItem_QuantityIncrease() {
        // given
        given(authService.getCurrentUser()).willReturn(testUser);
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(cartItemRepository.findByCartAndProduct(testCart, testProduct))
                .willReturn(Optional.of(testCartItem));
        given(cartItemRepository.save(any(CartItem.class))).willReturn(testCartItem);
        given(cartItemRepository.findByCart(testCart)).willReturn(Arrays.asList(testCartItem));
        
        // when
        CartResponse response = cartService.addItemToCart(testRequest);
        
        // then
        assertThat(response).isNotNull();
        verify(cartItemRepository).save(testCartItem);
        // 기존 수량(2) + 요청 수량(3) = 5
        assertThat(testCartItem.getQuantity()).isEqualTo(5);
    }
    
    @Test
    @DisplayName("재고 부족 시 장바구니 추가 실패")
    void addItemToCart_InsufficientStock_ThrowsException() {
        // given
        testProduct.setStock(1); // 재고를 1개로 설정
        
        given(authService.getCurrentUser()).willReturn(testUser);
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        
        // when & then
        assertThatThrownBy(() -> cartService.addItemToCart(testRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("재고가 부족합니다");
    }
    
    @Test
    @DisplayName("장바구니 아이템 수량 수정 성공")
    void updateCartItem_Success() {
        // given
        given(authService.getCurrentUser()).willReturn(testUser);
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(cartItemRepository.findById(1L)).willReturn(Optional.of(testCartItem));
        given(cartItemRepository.save(any(CartItem.class))).willReturn(testCartItem);
        given(cartItemRepository.findByCart(testCart)).willReturn(Arrays.asList(testCartItem));
        
        // when
        CartResponse response = cartService.updateCartItem(1L, 5);
        
        // then
        assertThat(response).isNotNull();
        verify(cartItemRepository).save(testCartItem);
        assertThat(testCartItem.getQuantity()).isEqualTo(5);
    }
    
    @Test
    @DisplayName("권한 없는 사용자의 장바구니 아이템 수정 시도")
    void updateCartItem_UnauthorizedUser_ThrowsException() {
        // given
        User otherUser = User.builder()
                .id(2L)
                .email("other@test.com")
                .name("다른유저")
                .role(Role.USER)
                .build();
        
        given(authService.getCurrentUser()).willReturn(otherUser);
        given(cartRepository.findByUser(otherUser)).willReturn(Optional.of(testCart));
        given(cartItemRepository.findById(1L)).willReturn(Optional.of(testCartItem));
        
        // when & then
        assertThatThrownBy(() -> cartService.updateCartItem(1L, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("권한이 없습니다");
    }
    
    @Test
    @DisplayName("장바구니 아이템 제거 성공")
    void removeItemFromCart_Success() {
        // given
        given(authService.getCurrentUser()).willReturn(testUser);
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(cartItemRepository.findById(1L)).willReturn(Optional.of(testCartItem));
        given(cartItemRepository.findByCart(testCart)).willReturn(Arrays.asList());
        
        // when
        CartResponse response = cartService.removeItemFromCart(1L);
        
        // then
        assertThat(response).isNotNull();
        verify(cartItemRepository).delete(testCartItem);
    }
    
    @Test
    @DisplayName("장바구니 전체 비우기 성공")
    void clearCart_Success() {
        // given
        given(authService.getCurrentUser()).willReturn(testUser);
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        
        // when
        cartService.clearCart();
        
        // then
        verify(cartItemRepository).deleteByCart(testCart);
    }
    
    @Test
    @DisplayName("내 장바구니 조회 성공")
    void getMyCart_Success() {
        // given
        given(authService.getCurrentUser()).willReturn(testUser);
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(cartItemRepository.findByCart(testCart)).willReturn(Arrays.asList(testCartItem));
        
        // when
        CartResponse response = cartService.getMyCart();
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalQuantity()).isEqualTo(2);
        assertThat(response.getTotalPrice()).isEqualTo(new BigDecimal("20000"));
    }
    
    @Test
    @DisplayName("장바구니 아이템 수 조회")
    void getCartItemCount_Success() {
        // given
        given(authService.getCurrentUser()).willReturn(testUser);
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(cartItemRepository.findByCart(testCart)).willReturn(Arrays.asList(testCartItem));
        
        // when
        Integer count = cartService.getCartItemCount();
        
        // then
        assertThat(count).isEqualTo(2); // testCartItem의 수량이 2
    }
}
