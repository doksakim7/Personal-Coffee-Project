package kr.spartaclub.coffeeproject.domain.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent {

    private Long orderId;
    private Long userId;
    private Long totalPrice;
    private String status;
    private LocalDateTime occurredAt;

}
