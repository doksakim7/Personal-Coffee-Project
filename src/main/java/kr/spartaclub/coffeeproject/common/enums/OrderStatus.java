package kr.spartaclub.coffeeproject.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    PENDING("결제 대기"),
    ORDERED("결제 완료"),
    CANCELED_BY_USER("사용자 취소"),
    CANCELED_BY_SYSTEM("결제 실패");

    private final String description;

}
