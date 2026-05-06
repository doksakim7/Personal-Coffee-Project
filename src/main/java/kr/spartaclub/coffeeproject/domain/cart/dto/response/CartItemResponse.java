package kr.spartaclub.coffeeproject.domain.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItemResponse {

    private Long itemId;
    private Long menuId;
    private Integer quantity;

}
