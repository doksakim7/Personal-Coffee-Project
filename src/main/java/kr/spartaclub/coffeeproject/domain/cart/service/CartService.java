package kr.spartaclub.coffeeproject.domain.cart.service;

import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import kr.spartaclub.coffeeproject.common.security.AuthUser;
import kr.spartaclub.coffeeproject.domain.cart.dto.request.CartItemAddRequest;
import kr.spartaclub.coffeeproject.domain.cart.dto.request.CartItemQuantityUpdateRequest;
import kr.spartaclub.coffeeproject.domain.cart.dto.response.CartItemResponse;
import kr.spartaclub.coffeeproject.domain.cart.dto.response.CartResponse;
import kr.spartaclub.coffeeproject.domain.cart.entity.Cart;
import kr.spartaclub.coffeeproject.domain.cart.entity.CartItem;
import kr.spartaclub.coffeeproject.domain.cart.repository.CartItemRepository;
import kr.spartaclub.coffeeproject.domain.cart.repository.CartRepository;
import kr.spartaclub.coffeeproject.domain.menu.entity.Menu;
import kr.spartaclub.coffeeproject.domain.menu.repository.MenuRepository;
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
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    // 사용자 장바구니가 없으면 빈 장바구니를 생성한 뒤 조회한다.
    @Transactional
    public CartResponse getCart(AuthUser authUser) {
        User user = getUser(authUser);
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));

        List<CartItem> cartItems = cartItemRepository.findAllByCart(cart);

        List<CartResponse.CartItemDetail> items = cartItems.stream()
                .map(item -> new CartResponse.CartItemDetail(
                        item.getId(),
                        item.getMenu().getId(),
                        item.getMenu().getName(),
                        item.getMenu().getPrice(),
                        item.getQuantity(),
                        item.getTotalPrice(),
                        item.getMenu().getStatus().name()
                ))
                .toList();

        Long totalPrice = items.stream()
                .mapToLong(CartResponse.CartItemDetail::getTotalPrice)
                .sum();

        return new CartResponse(cart.getId(), items, totalPrice);
    }

    // 동일 메뉴가 이미 담겨 있으면 새 row를 만들지 않고 수량만 증가시킨다.
    @Transactional
    public CartItemResponse addCartItem(AuthUser authUser, CartItemAddRequest request) {
        User user = getUser(authUser);
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));

        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        if (!menu.isOrderable()) {
            throw new CustomException(ErrorCode.MENU_NOT_ACTIVE);
        }

        CartItem cartItem = cartItemRepository.findByCartAndMenu(cart, menu)
                .map(existingItem -> {
                    existingItem.addQuantity(request.getQuantity());
                    return existingItem;
                })
                .orElseGet(() -> cartItemRepository.save(
                        new CartItem(cart, menu, request.getQuantity())
                ));

        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getMenu().getId(),
                cartItem.getQuantity()
        );
    }

    // 요청한 장바구니 상품이 현재 로그인한 사용자의 것인지 확인한 뒤 수량을 수정한다.
    @Transactional
    public CartItemResponse updateCartItemQuantity(AuthUser authUser, Long itemId, CartItemQuantityUpdateRequest request) {
        User user = getUser(authUser);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));

        validateCartOwner(cartItem, user);

        cartItem.changeQuantity(request.getQuantity());

        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getMenu().getId(),
                cartItem.getQuantity()
        );
    }

    // 삭제 API는 멱등하게 처리하므로, 이미 없는 상품이면 예외 없이 종료한다.
    @Transactional
    public void deleteCartItem(AuthUser authUser, Long itemId) {
        User user = getUser(authUser);

        cartItemRepository.findById(itemId).ifPresent(cartItem -> {
            validateCartOwner(cartItem, user);
            cartItemRepository.delete(cartItem);
        });
    }

    private User getUser(AuthUser authUser) {
        return userRepository.findById(authUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 다른 사용자의 장바구니 상품 존재 여부를 노출하지 않기 위해, 소유자가 아니면 동일하게 CART_ITEM_NOT_FOUND를 반환한다.
    private void validateCartOwner(CartItem cartItem, User user) {
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

}
