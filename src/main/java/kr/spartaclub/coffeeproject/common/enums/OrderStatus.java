package kr.spartaclub.coffeeproject.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    PENDING("결제 대기"),
    ORDERED("결제 완료"),
    CANCELED("주문 취소");

    private final String description;

}
