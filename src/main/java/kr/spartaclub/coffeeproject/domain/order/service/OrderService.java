package kr.spartaclub.coffeeproject.domain.order.service;

import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import kr.spartaclub.coffeeproject.common.security.AuthUser;
import kr.spartaclub.coffeeproject.common.enums.PointType;
import kr.spartaclub.coffeeproject.domain.cart.entity.Cart;
import kr.spartaclub.coffeeproject.domain.cart.entity.CartItem;
import kr.spartaclub.coffeeproject.domain.cart.repository.CartItemRepository;
import kr.spartaclub.coffeeproject.domain.cart.repository.CartRepository;
import kr.spartaclub.coffeeproject.domain.order.dto.response.OrderCancelResponse;
import kr.spartaclub.coffeeproject.domain.order.dto.response.OrderCreateResponse;
import kr.spartaclub.coffeeproject.domain.order.dto.response.OrderDetailResponse;
import kr.spartaclub.coffeeproject.domain.order.dto.response.OrderPayResponse;
import kr.spartaclub.coffeeproject.domain.order.dto.response.OrderSummaryResponse;
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

    // 장바구니 기준으로 주문을 생성하고 PENDING 상태로 저장한다.
    @Transactional
    public OrderCreateResponse createOrder(AuthUser authUser, String idempotencyKey) {
        validateIdempotencyKey(idempotencyKey);

        User user = getUser(authUser);
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

    // PENDING 상태의 주문을 결제하고 ORDERED 상태로 변경한다.
    @Transactional(noRollbackFor = CustomException.class)
    public OrderPayResponse payOrder(AuthUser authUser, Long orderId) {
        User user = getUser(authUser);
        Order order = getOwnedOrder(user, orderId);

        if (order.isOrdered()) {
            // ORDERED 상태 재요청은 동일 결과 반환
            return new OrderPayResponse(order.getId(), order.getStatus().name());
        }

        if (order.isCanceled()) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELED);
        }

        List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);

        // 결제 시점에도 메뉴 상태를 재검증
        for (OrderItem orderItem : orderItems) {
            if (!orderItem.getMenu().isOrderable()) {
                order.cancelBySystem();
                throw new CustomException(ErrorCode.MENU_NOT_ACTIVE);
            }
        }

        // 포인트 부족 시 주문은 시스템 취소 상태로 변경
        if (user.getPoint() < order.getTotalPrice()) {
            order.cancelBySystem();
            throw new CustomException(ErrorCode.INSUFFICIENT_POINT);
        }

        // 포인트 차감
        user.subtractPoint(order.getTotalPrice());

        // 포인트 사용 이력 저장 (부호 포함 저장 정책)
        pointHistoryRepository.save(
                new PointHistory(
                        user,
                        order,
                        -order.getTotalPrice(),
                        PointType.USE
                )
        );

        // 주문 상태 완료
        order.complete();

        // 결제 성공 시 장바구니 비우기
        cartRepository.findByUser(user).ifPresent(cart -> {
            List<CartItem> cartItems = cartItemRepository.findAllByCart(cart);
            cartItemRepository.deleteAll(cartItems);
        });

        return new OrderPayResponse(order.getId(), order.getStatus().name());
    }

    // 현재 로그인한 사용자의 주문 목록을 최신순으로 조회한다.
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOrders(AuthUser authUser, Pageable pageable) {
        User user = getUser(authUser);

        return orderRepository.findAllByUserOrderByCreatedAtDesc(user, pageable)
                .map(order -> new OrderSummaryResponse(
                        order.getId(),
                        order.getTotalPrice(),
                        order.getStatus().name(),
                        order.getCreatedAt()
                ));
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
                            PointType.REFUND
                    )
            );
        }

        // PENDING, ORDERED 모두 사용자 취소 가능
        order.cancelByUser();

        return new OrderCancelResponse(order.getId(), order.getStatus().name());
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
