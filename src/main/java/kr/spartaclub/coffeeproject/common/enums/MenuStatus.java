package kr.spartaclub.coffeeproject.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MenuStatus {

    AVAILABLE("판매중"),
    INACTIVE("판매중지"),
    SOLD_OUT("품절");

    private final String description;

}
