package kr.spartaclub.coffeeproject.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OrderSummaryResponse {

    private Long orderId;
    private Long totalPrice;
    private String status;
    private LocalDateTime createdAt;

}
