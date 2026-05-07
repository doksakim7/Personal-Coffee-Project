# 🔐 인증 API

※ 모든 API는 Base URL(`/api`) 하위에서 동작한다.

---

## 1. 회원가입

| 항목     | 내용             |
| ------ | -------------- |
| Method | POST           |
| URL    | `/auth/signup` |
| 설명     | 회원가입           |
| 인증 | 불필요 |

※ 비밀번호는 영문, 숫자, 특수문자를 각각 최소 1개 이상 포함해야 하며, 8자 이상 12자 이하로 설정한다.

※ 이메일은 도메인을 포함한 올바른 이메일 형식이어야 한다. (ex. test@test.com)

※ 이메일과 비밀번호는 공백을 허용하지 않는다.

※ 회원가입은 중복 이메일을 허용하지 않는다.

---

### Request

| 필드 | 타입 | 설명 |
|------|------|------|
| email | String | 사용자 이메일 |
| password | String | 비밀번호 |

```json
{
    "email": "test@test.com",
    "password": "abcd1234!"
}
```

---

### Response `201 Created`
```json
{
    "message": "회원가입이 완료되었습니다.",
    "data": {
        "userId": 1,
        "email": "test@test.com"
    },
    "timestamp": "2026-05-11T09:00.000000"
}
```

---

### Response `409 Conflict`
```json
{
    "message": "이미 존재하는 이메일입니다.",
    "data": null,
   "timestamp": "2026-05-11T09:00.000000"
}
```

---

## 2. 로그인

| 항목     | 내용                     |
| ------ |------------------------|
| Method | POST                   |
| URL    | `/auth/login` |
| 설명     | 로그인 및 JWT 발급           |
| 인증 | 불필요 |

※ `accessToken`은 이후 요청 시 `Authorization` 헤더에 다음과 같이 포함하여 사용한다.  
`Authorization: Bearer {accessToken}`

---

| 필드 | 타입 | 설명 |
|------|------|------|
| email | String | 사용자 이메일 |
| password | String | 비밀번호 |

### Request
```json
{
    "email": "test@test.com",
    "password": "abcd1234!"
}
```

---

### Response `200 OK`
```json
{
    "message": "로그인에 성공했습니다.",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwiZW1haWwiOiJ0ZXN0N....",
        "tokenType": "Bearer",
        "userId": 1,
        "email": "test@test.com"
    }, 
    "timestamp": "2026-05-11T09:00.000000"
}
```

---

### Response `401 Unauthorized`
```json
{
    "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00.000000"
}
```
