package kr.spartaclub.coffeeproject.domain.order.service;


import kr.spartaclub.coffeeproject.common.enums.PointType;
import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import kr.spartaclub.coffeeproject.domain.cart.entity.CartItem;
import kr.spartaclub.coffeeproject.domain.cart.repository.CartItemRepository;
import kr.spartaclub.coffeeproject.domain.cart.repository.CartRepository;
import kr.spartaclub.coffeeproject.domain.order.dto.response.OrderPayResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderPaymentService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    // 주문 상태와 포인트를 검증한 뒤 결제를 처리하고, 성공 시 ORDERED 상태로 변경한다.
    @Transactional(noRollbackFor = CustomException.class)
    public OrderPayResponse payOrderInternal(Long userId, Long orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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
                        user.getPoint(),
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

    private Order getOwnedOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
        }

        return order;
    }

}
