package com.backend.shopping.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.shopping.dto.request.OrderRequest;
import com.backend.shopping.dto.response.OrderResponse;
import com.backend.shopping.dto.response.OrderResponse.OrderItemResponse;
import com.backend.shopping.entity.Order;
import com.backend.shopping.entity.OrderItem;
import com.backend.shopping.entity.OrderStatus;
import com.backend.shopping.entity.Product;
import com.backend.shopping.entity.User;
import com.backend.shopping.exception.CustomException;
import com.backend.shopping.exception.ErrorCode;
import com.backend.shopping.repository.OrderRepository;
import com.backend.shopping.repository.ProductRepository;
import com.backend.shopping.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;

	// 주문 생성
	public OrderResponse createOrder(String username, OrderRequest request) {
		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		// 주문 생성
		Order order = Order.builder()
				.user(user)
				.status(OrderStatus.PENDING)
				.totalPrice(BigDecimal.ZERO)
				.build();

		// 주문 항목 생성
		List<OrderItem> items = request.getOrderItems().stream().map(itemRequest -> {
			Product product = productRepository.findById(itemRequest.getProductId())
					.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

			// 재고 확인
			if (product.getStock() < itemRequest.getQuantity()) {
				throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
			}

			// 재고 차감
			product.setStock(product.getStock() - itemRequest.getQuantity());

			OrderItem orderItem = OrderItem.builder()
											.order(order)
											.product(product)
											.quantity(itemRequest.getQuantity())
											.price(product.getPrice())// 주문 당시 가격으로 저장
											.build();
			orderItem.setOrder(order);
			return orderItem;
			
		}).collect(Collectors.toList());

		 // 총 금액 계산
		order.setOrderItems(items);

		BigDecimal total = items.stream().map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		order.setTotalPrice(total);

		Order savedOrder = orderRepository.save(order);
		log.info("주문 생성 완료: 주문ID={}, 사용자={}", savedOrder.getId(), username);

		return convertToResponse(savedOrder);
	}

	// 주문 조회 (단건)
	@Transactional(readOnly = true)
	public OrderResponse getOrder(String username, Long orderId) {
		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Order order = orderRepository.findByIdAndUser(orderId, user)
				.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		return convertToResponse(order);
	}

	// 사용자 주문 목록 조회 (페이징)
	@Transactional(readOnly = true)
	public Page<OrderResponse> getUserOrders(String username, Pageable pageable) {
		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Page<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user, pageable);
		return orders.map(this::convertToResponse);
	}

	// 주문 취소
	public OrderResponse cancelOrder(String username, Long orderId) {
		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Order order = orderRepository.findByIdAndUser(orderId, user)
				.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		// 취소 가능한 상태인지 확인
		if (order.getStatus() == OrderStatus.SHIPPED 
				|| order.getStatus() == OrderStatus.DELIVERED
				|| order.getStatus() == OrderStatus.CANCELLED) {
			throw new CustomException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
		}

		// 재고 복원
		for (OrderItem item : order.getOrderItems()) {
			Product product = item.getProduct();
			product.setStock(product.getStock() + item.getQuantity());
		}

		order.setStatus(OrderStatus.CANCELLED);
		Order savedOrder = orderRepository.save(order);
		log.info("주문 취소 완료: 주문ID={}, 사용자={}", savedOrder.getId(), username);

		return convertToResponse(savedOrder);
	}

	// 주문 상태 업데이트 (관리자용)
	public OrderResponse updateOrderStatus(Long orderId, String statusStr) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		OrderStatus newStatus;
		try {
			newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELLED);
		}

		OrderStatus oldStatus = order.getStatus();
		order.setStatus(newStatus);

		Order savedOrder = orderRepository.save(order);
		log.info("주문 상태 변경: 주문ID={}, {} -> {}", savedOrder.getId(), oldStatus, newStatus);

		return convertToResponse(savedOrder);
	}

	 // Entity to DTO 변환
	private OrderResponse convertToResponse(Order order) {
		List<OrderItemResponse> items = order.getOrderItems().stream()
				.map(item -> OrderItemResponse.builder()
						.id(item.getId())
						.productId(item.getProduct().getId())
						.productName(item.getProduct().getName())
						.quantity(item.getQuantity()).price(item.getPrice())
						.subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
						.build())
				
				.collect(Collectors.toList());

		return OrderResponse.builder().id(order.getId()).orderNumber("ORD-" + order.getId())
				.status(order.getStatus().name()).totalAmount(order.getTotalPrice())
				.orderItems(items)
				.orderDate(order.getOrderDate())
				.updatedAt(order.getOrderDate()) // updatedAt이 없다면 orderDate 사용
				.build();
	}
}