# 👤 회원 API

※ 모든 API는 Base URL(`/api`) 하위에서 동작한다.

※ 인증이 필요한 API이며, JWT를 통해 현재 사용자 정보를 식별한다.

---

## 1. 내 정보 조회

| 항목     | 내용                |
| ------ | ----------------- |
| Method | GET               |
| URL    | `/users/me`       |
| 설명     | 현재 로그인한 사용자 정보 조회 |
| 인증     | 필요                |

---

### Response `200 OK`
```json
{
    "message": "회원 정보 조회 성공",
    "data": {
        "userId": 1,
        "email": "test@test.com"
    },
    "timestamp": "2026-05-11T09:00.00.000000"
}
```

---

### Response `401 Unauthorized`
```json
{
    "message": "인증이 필요합니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00.00.000000"
}
```

---

## 2. 내 정보 수정

| 항목     | 내용           |
| ------ | ------------ |
| Method | PATCH        |
| URL    | `/users/me`  |
| 설명     | 현재 사용자 정보 수정 |
| 인증     | 필요           |

※ 이메일은 수정할 수 없다.

※ 비밀번호는 조건(영문, 숫자, 특수문자 포함 / 8~12자)을 만족해야 한다.

※ 요청에 포함된 필드만 수정된다. (Partial Update)

---

### Request

| 필드       | 타입     | 필수 | 설명       |
| -------- | ------ | -- | -------- |
| password | String | 선택 | 변경할 비밀번호 |

※ 현재는 비밀번호만 수정 가능하며, 향후 추가 필드 확장을 고려하여 Partial Update 방식으로 설계되었다.

```json
{
    "password": "abcd1234!"
}
```

---

### Response `200 OK`
```json
{
    "message": "회원 정보가 수정되었습니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00.00.000000"
}
```

---

### Response `400 Bad Request`
```json
{
    "message": "비밀번호는 8~12자이며, 영문/숫자/특수문자를 각각 최소 1개 포함해야 합니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00.00.000000"
}
```

### Response `401 Unauthorized`
```json
{
    "message": "인증이 필요합니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00.00.000000"
}
```
