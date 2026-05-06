package kr.spartaclub.coffeeproject.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 추후 관리자 기능 확장을 위해 생성
@Getter
@RequiredArgsConstructor
public enum UserRole {

    USER("일반 사용자"),
    ADMIN("관리자");

    private final String description;
    
}
