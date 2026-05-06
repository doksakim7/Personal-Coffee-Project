package kr.spartaclub.coffeeproject.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCreateResponse {

    private Long orderId;
    private Long totalPrice;
    private String status;

}
