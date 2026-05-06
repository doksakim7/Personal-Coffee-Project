package kr.spartaclub.coffeeproject.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 오류입니다. 점검 후 조치하겠습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),

    // JWT
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "지원하지 않는 토큰입니다."),

    // 회원
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    USER_DUPLICATE_EMAIL(HttpStatus.CONFLICT, "중복된 이메일입니다."),
    USER_INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 틀렸습니다."),

    // 메뉴
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
    MENU_NOT_ACTIVE(HttpStatus.CONFLICT, "현재 판매 중인 메뉴가 아닙니다."),

    // 장바구니
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 상품을 찾을 수 없습니다."),
    CART_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "수량은 1개 이상이어야 합니다."),

    // 주문
    CART_EMPTY(HttpStatus.BAD_REQUEST, "장바구니가 비어있습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 주문입니다."),
    IDEMPOTENCY_KEY_MISSING(HttpStatus.BAD_REQUEST, "Idempotency-Key가 필요합니다."),
    IDEMPOTENCY_KEY_CONFLICT(HttpStatus.CONFLICT, "이미 처리된 요청입니다."),

    // 포인트
    INSUFFICIENT_POINT(HttpStatus.CONFLICT, "포인트가 부족합니다."),
    POINT_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "최대 보유 포인트를 초과했습니다."),
    POINT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "포인트 금액이 올바르지 않습니다."),

    // 동시성 제어
    LOCK_ACQUIRE_FAILED(HttpStatus.TOO_MANY_REQUESTS, "요청이 많아 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");

    // 이 코드 위쪽에 에러 코드 작성

    private final HttpStatus status;
    private final String message;

}
