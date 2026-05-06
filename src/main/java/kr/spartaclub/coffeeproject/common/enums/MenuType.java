package kr.spartaclub.coffeeproject.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MenuType {

    COFFEE("커피"),
    LATTE("라떼"),
    ADE("에이드"),
    TEA("차"),
    DESSERT("디저트"),
    SET("세트 메뉴");

    private final String description;

}
