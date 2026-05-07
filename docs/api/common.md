# 📌 API 명세서

## 📎 공통 규칙
| 항목           | 내용                                   |
| ------------ |--------------------------------------|
| Base URL     | `/api`                               |
| 인증 방식        | `Authorization: Bearer {JWT}` (필요 시) |
| Content-Type | `application/json`                   |

※ 일부 API는 인증이 필요하지 않다. (회원가입, 로그인 등)

※ 모든 API는 Base URL(`/api`) 하위에서 동작한다.  
예: `/api/auth/login`, `/api/orders`

※ 단, `/actuator` 경로는 Spring Boot 관리 엔드포인트이며, 비즈니스 API(`/api`)와 별도로 동작한다.

---

## 📎 공통 응답 형식

※ message는 상황에 맞는 의미 있는 메시지를 반환한다.   

※ timestamp는 서버에서 응답을 생성한 시각이다. (ISO-8601 형식, Asia/Seoul 기준)

※ data는 응답 데이터가 없을 경우 null을 반환한다.    

※ 모든 API 응답은 동일한 구조(message, data, timestamp)를 따른다.

※ HTTP Status Code는 응답 Body에 포함하지 않고, Header의 상태 코드로 전달한다.

※ 단, `/actuator` 응답은 Spring Boot 기본 포맷을 따르며, 공통 응답 형식과 다를 수 있다.

---

### 성공 응답

```json
{
    "message": "성공 메시지",
    "data": {
        "exampleKey": "exampleValue"
    },
    "timestamp": "2026-05-11T09:00.000000"
}
```

---

### 실패 응답

```json
{
    "message": "에러 메시지",
    "data": null,
    "timestamp": "2026-05-11T09:00.000000"
}
```

---

## 📎 에러 코드 정의

| 코드 | 의미                                               |
| --- |--------------------------------------------------|
| `400 Bad Request` | 클라이언트가 잘못된 요청을 보낸 경우 (형식 오류, 유효성 실패 등)           |
| `401 Unauthorized` | 인증이 안 된 경우 (토큰 없음, 만료 등)                         |
| `403 Forbidden` | 인증은 됐지만 권한이 없는 경우                                |
| `404 Not Found` | 요청한 리소스가 존재하지 않는 경우                              |
| `409 Conflict` | 서버 상태와 충돌하는 경우 (중복 요청, 상태 불일치 등)                 |
| `429 Too Many Requests` | 동시 요청으로 인해 Redis 분산락 획득 실패 시 반환 (서버에서 일정 횟수 재시도 후에도 실패한 경우) |
| `500 Internal Server Error` | 서버 내부 오류 발생 시                                    |

※ HTTP Status Code를 기준으로 성공/실패를 판단하며, message는 상황 설명을 위한 보조 정보이다.

---

## 서버 상태 모니터링

| 항목 | 내용 |
| --- | --- |
| **Method** | `GET` |
| **URL** | `/actuator/health` |
| **인증** | 불필요 |
| **설명** | 서버 상태를 체크한다. |

※ /actuator/health 응답은 Spring Boot Actuator 기본 포맷을 따른다.

---

### Response `200 OK`
```json
{
    "status": "UP"
}
```
