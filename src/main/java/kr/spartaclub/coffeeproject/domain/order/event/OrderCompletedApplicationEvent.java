package kr.spartaclub.coffeeproject.domain.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCompletedApplicationEvent {

    private final Long orderId;
    private final Long userId;
    private final Long totalPrice;
    private final String status;

}
