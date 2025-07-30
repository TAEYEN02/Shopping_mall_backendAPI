package com.backend.shopping.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.shopping.dto.request.CartItemRequest;
import com.backend.shopping.dto.response.CartItemResponse;
import com.backend.shopping.dto.response.CartResponse;
import com.backend.shopping.entity.Cart;
import com.backend.shopping.entity.CartItem;
import com.backend.shopping.entity.Product;
import com.backend.shopping.entity.User;
import com.backend.shopping.repository.CartItemRepository;
import com.backend.shopping.repository.CartRepository;
import com.backend.shopping.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AuthService authService;
    
    // 장바구니에 상품 추가
    @Transactional
    public CartResponse addItemToCart(CartItemRequest request) {
        User currentUser = authService.getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));
        
        // 재고 확인
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("재고가 부족합니다");
        }
        
        // 이미 장바구니에 있는 상품인지 확인
        CartItem existingItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);
        
        if (existingItem != null) {
            // 기존 아이템의 수량 업데이트
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("재고가 부족합니다");
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            // 새로운 아이템 추가
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }
        
        return getCartResponse(cart);
    }
    
    // 장바구니 아이템 수량 수정
    @Transactional
    public CartResponse updateCartItem(Long itemId, Integer quantity) {
        User currentUser = authService.getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("장바구니 아이템을 찾을 수 없습니다"));
        
        // 권한 확인 (자신의 장바구니인지)
        if (!cartItem.getCart().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("권한이 없습니다");
        }
        
        // 재고 확인
        if (cartItem.getProduct().getStock() < quantity) {
            throw new RuntimeException("재고가 부족합니다");
        }
        
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        
        return getCartResponse(cart);
    }
    
    // 장바구니에서 아이템 제거
    @Transactional
    public CartResponse removeItemFromCart(Long itemId) {
        User currentUser = authService.getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("장바구니 아이템을 찾을 수 없습니다"));
        
        // 권한 확인
        if (!cartItem.getCart().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("권한이 없습니다");
        }
        
        cartItemRepository.delete(cartItem);
        
        return getCartResponse(cart);
    }
    
    // 장바구니 전체 비우기
    @Transactional
    public void clearCart() {
        User currentUser = authService.getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        cartItemRepository.deleteByCart(cart);
    }
    
    // 내 장바구니 조회
    public CartResponse getMyCart() {
        User currentUser = authService.getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        return getCartResponse(cart);
    }
    
    // 장바구니 아이템 수 조회
    public Integer getCartItemCount() {
        User currentUser = authService.getCurrentUser();
        Cart cart = cartRepository.findByUser(currentUser).orElse(null);
        
        if (cart == null) {
            return 0;
        }
        
        List<CartItem> items = cartItemRepository.findByCart(cart);
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    // 장바구니 존재 여부 확인 및 생성
    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }
    
    // Cart Entity -> CartResponse 변환
    private CartResponse getCartResponse(Cart cart) {
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());
        
        CartResponse response = CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemResponses)
                .createdAt(cart.getCreatedAt())
                .build();
        
        // 총 가격과 수량 계산
        response.setTotalPrice(response.calculateTotalPrice());
        response.setTotalQuantity(response.calculateTotalQuantity());
        
        return response;
    }
    
    // CartItem Entity -> CartItemResponse 변환
    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .productPrice(product.getPrice())
                .productStock(product.getStock())
                .quantity(cartItem.getQuantity())
                .createdAt(cartItem.getCreatedAt())
                .build();
    }
}
