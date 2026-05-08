package kr.spartaclub.coffeeproject.domain.order.service;

import kr.spartaclub.coffeeproject.common.enums.OrderStatus;
import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import kr.spartaclub.coffeeproject.common.lock.DistributedLockManager;
import kr.spartaclub.coffeeproject.common.security.AuthUser;
import kr.spartaclub.coffeeproject.common.enums.PointType;
import kr.spartaclub.coffeeproject.domain.cart.entity.Cart;
import kr.spartaclub.coffeeproject.domain.cart.entity.CartItem;
import kr.spartaclub.coffeeproject.domain.cart.repository.CartItemRepository;
import kr.spartaclub.coffeeproject.domain.cart.repository.CartRepository;
import kr.spartaclub.coffeeproject.domain.order.dto.response.*;
import kr.spartaclub.coffeeproject.domain.order.entity.Order;
import kr.spartaclub.coffeeproject.domain.order.entity.OrderItem;
import kr.spartaclub.coffeeproject.domain.order.repository.OrderItemRepository;
import kr.spartaclub.coffeeproject.domain.order.repository.OrderRepository;
import kr.spartaclub.coffeeproject.domain.point.entity.PointHistory;
import kr.spartaclub.coffeeproject.domain.point.repository.PointHistoryRepository;
import kr.spartaclub.coffeeproject.domain.user.entity.User;
import kr.spartaclub.coffeeproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final OrderPaymentService orderPaymentService;
    private final DistributedLockManager distributedLockManager;

    // 장바구니 기준으로 주문을 생성하고 PENDING 상태로 저장한다.
    @Transactional
    public OrderCreateResponse createOrder(AuthUser authUser, String idempotencyKey) {
        validateIdempotencyKey(idempotencyKey);

        User user = getUser(authUser);

        // 동일 사용자 기준 PENDING 주문은 1개만 허용
        if (orderRepository.existsByUserAndStatus(user, OrderStatus.PENDING)) {
            throw new CustomException(ErrorCode.ORDER_PENDING_ALREADY_EXISTS);
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_EMPTY));

        List<CartItem> cartItems = cartItemRepository.findAllByCart(cart);
        if (cartItems.isEmpty()) {
            throw new CustomException(ErrorCode.CART_EMPTY);
        }

        // 주문 생성 시점에 주문 가능한 메뉴만 담을 수 있도록 검증
        for (CartItem cartItem : cartItems) {
            if (!cartItem.getMenu().isOrderable()) {
                throw new CustomException(ErrorCode.MENU_NOT_ACTIVE);
            }
        }

        Long totalPrice = cartItems.stream()
                .mapToLong(CartItem::getTotalPrice)
                .sum();

        Order order = new Order(user, totalPrice);
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> new OrderItem(
                        savedOrder,
                        cartItem.getMenu(),
                        cartItem.getMenu().getPrice(), // 주문 시점 가격 스냅샷 저장
                        cartItem.getQuantity()
                ))
                .toList();

        orderItemRepository.saveAll(orderItems);

        return new OrderCreateResponse(
                savedOrder.getId(),
                savedOrder.getTotalPrice(),
                savedOrder.getStatus().name()
        );
    }

    // 현재 로그인한 사용자의 주문 목록을 최신순으로 조회한다.
    @Transactional(readOnly = true)
    public OrderListResponse getOrders(AuthUser authUser, Pageable pageable) {
        User user = getUser(authUser);

        Page<Order> pageResult = orderRepository.findAllByUserOrderByCreatedAtDesc(user, pageable);

        List<OrderListResponse.OrderSummary> content = pageResult.getContent().stream()
                .map(order -> new OrderListResponse.OrderSummary(
                        order.getId(),
                        order.getTotalPrice(),
                        order.getStatus().name(),
                        order.getCreatedAt()
                ))
                .toList();

        return new OrderListResponse(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    // 현재 로그인한 사용자의 주문 상세 정보를 조회한다.
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(AuthUser authUser, Long orderId) {
        User user = getUser(authUser);
        Order order = getOwnedOrder(user, orderId);

        List<OrderDetailResponse.OrderItemDetail> items = orderItemRepository.findAllByOrder(order)
                .stream()
                .map(orderItem -> new OrderDetailResponse.OrderItemDetail(
                        orderItem.getMenu().getId(),
                        orderItem.getMenu().getName(),
                        orderItem.getPrice(),
                        orderItem.getQuantity()
                ))
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                items,
                order.getTotalPrice(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }

    // 주문을 취소하고 필요 시 포인트를 복구한다.
    @Transactional
    public OrderCancelResponse cancelOrder(AuthUser authUser, Long orderId) {
        User user = getUser(authUser);
        Order order = getOwnedOrder(user, orderId);

        if (order.isCanceled()) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELED);
        }

        // ORDERED 상태 취소면 포인트 복구 + REFUND 이력 저장
        if (order.isOrdered()) {
            user.addPoint(order.getTotalPrice());

            pointHistoryRepository.save(
                    new PointHistory(
                            user,
                            order,
                            order.getTotalPrice(),
                            user.getPoint(),
                            PointType.REFUND
                    )
            );
        }

        // PENDING, ORDERED 모두 사용자 취소 가능
        order.cancelByUser();

        return new OrderCancelResponse(order.getId(), order.getStatus().name());
    }

    // 동일 사용자에 대한 결제 요청을 Redis 분산락으로 직렬화한 뒤 실제 결제 로직을 수행한다.
    public OrderPayResponse payOrder(AuthUser authUser, Long orderId) {
        Long userId = authUser.getId();
        String lockKey = "lock:user:" + userId;

        return distributedLockManager.executeWithLock(
                lockKey,
                3,
                5,
                TimeUnit.SECONDS,
                () -> orderPaymentService.payOrderInternal(userId, orderId)
        );
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new CustomException(ErrorCode.IDEMPOTENCY_KEY_MISSING);
        }
    }

    private User getUser(AuthUser authUser) {
        return userRepository.findById(authUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Order getOwnedOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
        }

        return order;
    }

}
