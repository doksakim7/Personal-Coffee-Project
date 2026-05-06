package kr.spartaclub.coffeeproject.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointType {

    CHARGE("충전"),
    USE("사용"),
    REFUND("환불"),
    EXCHANGE("현금 교환");

    private final String description;

}
