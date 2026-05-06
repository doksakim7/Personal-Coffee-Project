package kr.spartaclub.coffeeproject.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderPayResponse {

    private Long orderId;
    private String status;

}
