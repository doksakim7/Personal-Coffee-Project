package kr.spartaclub.coffeeproject.domain.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponse {

    private Long cartId;
    private List<CartItemDetail> items;
    private Long totalPrice;

    @Getter
    @AllArgsConstructor
    public static class CartItemDetail {
        private Long itemId;
        private Long menuId;
        private String name;
        private Long price;
        private Integer quantity;
        private Long totalPrice;
        private String status;
    }

}
