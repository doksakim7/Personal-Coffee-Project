# ☕ 커피 주문 시스템

## 📌 1. 프로젝트 개요

본 시스템은 다수의 서버 환경에서도 안정적으로 동작하는 커피 주문 시스템을 목표로 한다.  
특히 **동시성 제어**, **데이터 일관성**, **확장성**을 고려하여 설계하였다.

---

## 2. 기술 스택

### Backend
- Java 17
- Spring Boot 3.5.x
- Spring Data JPA
- Spring Security
- JWT

### Database / Infra
- MySQL
- Redis
- Redisson
- Apache Kafka
- Docker / Docker Compose

### Test / Docs / Tools
- Postman
- k6
- QueryDSL
- IntelliJ IDEA

---

## 3. 요구사항 분석 및 비즈니스 정책

### 🔍 주요 문제 정의

1. **포인트 차감 시 동시성 문제**
   - 동일 사용자의 다중 요청으로 포인트가 중복 차감될 수 있음
   - 최악의 경우 포인트가 음수가 되는 문제 발생

2. **인기 메뉴 집계 성능 문제**
   - DB 기반 실시간 집계 시 성능 저하 발생

3. **주문 이벤트 비동기 처리 필요**
   - 주문 데이터를 외부 시스템(로그/분석)으로 전달 필요

---

### 📌 해결 전략 및 비즈니스 정책

- 포인트 기반 결제 시스템
- Redis 분산락 기반 동시성 제어
- Kafka 비동기 이벤트 처리

👉 자세한 정책은 아래 문서를 참고하세요.   
👉 [business-policy.md](docs/policy/business-policy.md)

---

## 4. 기능 목록

### 🔐 인증
- 회원가입
- 로그인 (JWT 기반 간단 인증)

### 👤 회원
- 내 정보 조회
- 내 정보 수정

### ☕ 메뉴 (더미 데이터)
- 메뉴 목록 조회
- 메뉴 상세 조회

### 📊 인기 메뉴
- 인기 메뉴 집계 (Redis ZSet 기반)
- 인기 메뉴 조회

### 🛒 장바구니
- 장바구니 조회
- 장바구니 상품 추가
- 장바구니 상품 수량 변경
- 장바구니 상품 삭제

### 💰 포인트
- 포인트 충전
- 포인트 환전
- 포인트 차감 (주문 결제 시)
- 현재 포인트 조회
- 포인트 내역 조회

### 📦 주문
- 장바구니 기반 주문 생성
- 주문 결제
- 주문 목록 조회
- 주문 상세 조회
- 주문 취소
- 주문 상태 관리
- 주문 이벤트 Kafka 전송

---

## 5. 아키텍처 설계

### 🏗 전체 구조

```text
[Client]
   ↓
[Spring Boot App (멀티 인스턴스 확장 고려)]
   ├─ Redis
   │   ├─ 분산락
   │   └─ ZSet
   ├─ MySQL
   └─ Kafka
        ↓
   [Consumer (로그/분석)]
```

---

### 🔥 주문 처리 흐름

주문 처리 흐름은 전체 요청의 실행 순서를 설명한다.

1. Client → 주문 생성 요청
2. 주문 생성 (`PENDING` 상태, 별도 API)

--- 결제 흐름 (별도 단계) ---
1. Client → 결제 요청
2. Redis 분산락 획득 (userId 기준)
3. 포인트 조회 
4. 포인트 검증 및 차감 (`POINT_HISTORY`에 `USE` 이력 기록)
5. 주문 상태 변경 (`PENDING` → `ORDERED`)
6. 장바구니 초기화 
7. Redis ZSet 증가 (인기 메뉴 집계, `ORDERED` 기준)
8. 트랜잭션 커밋 이후 이벤트를 발행하고, Kafka를 통해 비동기로 처리한다. 
9. 락 해제

---

### ⚙️ 기술 선택

#### ✅ Redis
- Redis 분산락을 활용하여 동시성 문제를 해결
- ZSet → 인기 메뉴 실시간 집계
- ZSet key는 "popular:menus"로 관리

#### ✅ Kafka
- 주문 처리와 데이터 수집을 분리하기 위해 사용
- 비동기 처리로 성능 저하 방지 및 확장성 확보
- 이벤트 발행 실패 시 로그를 기록하고, 추후 재처리 전략을 확장할 수 있도록 설계하였다.

#### ✅ k6
- 동시 요청 테스트
- Race Condition 검증

---

## 6. 주요 기술 적용 포인트

### Redis 분산락
- 동일 사용자 기준 주문 결제 요청을 직렬화하여 포인트 중복 차감 문제를 방지하였다.
- Redisson 기반 분산락을 적용하고, `tryLock`의 대기 시간과 TTL을 설정하여 무한 대기와 데드락을 방지하였다.

### Redis ZSet
- 인기 메뉴 집계를 위해 Redis ZSet을 활용하였다.
- 결제 성공 시 score를 증가시키고, `ORDERED` 상태 주문 취소 시 score를 감소시켜 정책과 실제 집계 결과를 일치시켰다.

### Kafka
- 주문 완료 이벤트를 Kafka로 발행하여 비동기 처리 구조를 구성하였다.
- `@TransactionalEventListener(phase = AFTER_COMMIT)`를 적용하여 트랜잭션 커밋 이후에만 이벤트가 발행되도록 설계하였다.

### k6
- 동일 사용자 기준 결제 요청을 동시에 발생시키는 부하 테스트를 통해 포인트, 주문 상태, 포인트 이력, 인기 메뉴 정합성을 검증하였다.

---

## 7. 동시성 및 핵심 로직 설계

### ⚠️ 동시성 문제 정의

동일 사용자가 동시에 여러 주문 요청을 보낼 경우,  
포인트 차감 과정에서 race condition이 발생할 수 있다.

이로 인해 포인트가 중복 차감되거나 음수가 되는 문제가 발생할 수 있다.

---

### 🛠 해결 전략

Redis 분산락은 userId 기준으로 적용하여 동일 사용자에 대한 동시 요청을 직렬화하고,
TTL과 대기 시간(tryLock)을 설정하여 데드락 및 무한 대기를 방지한다.

---

### 🔄 트랜잭션 처리

포인트 차감과 주문 상태 변경은 하나의 트랜잭션으로 처리하여  
데이터 일관성을 보장한다.

즉, 하나라도 실패할 경우 전체 작업은 롤백된다.

---

### 🔁 주문 처리 로직

주문 처리 로직은 트랜잭션 경계와 내부 처리 방식을 정의한다.    
그리고 다음과 같은 트랜잭션 단위로 구성된다.

- 결제 과정에서 포인트 차감과 주문 상태 변경은 하나의 트랜잭션으로 처리된다.
- Redis 락은 트랜잭션 외부에서 획득하여 동시성을 제어한다.
- Kafka 이벤트는 비동기로 처리된다.

---

### ⚠️ 예외 처리 전략

- tryLock 기반으로 대기 시간 내 락 획득을 시도하고, 실패 시 예외 처리한다.
- Kafka 전송 실패 시 로그를 기록하도록 처리하였다.

---

## 8. 부하 테스트

k6를 활용해 동시 요청 환경에서 시스템의 안정성을 검증한다.

- 동일 사용자 다중 요청 시 포인트 정합성 유지 확인
- 중복 결제 및 음수 포인트 발생 방지 검증

---

## 9. 실행 방법

### 1) 인프라 실행
프로젝트 루트에서 아래 명령어를 실행한다.

```bash
docker compose up -d
```

실행 후 사용 가능한 서비스:

- Kafka: `localhost:9092`
- Kafka UI: `localhost:8088`
- Redis: `localhost:6379`
- RedisInsight: `localhost:5540`

### 2) 애플리케이션 실행
Spring Boot 애플리케이션을 실행한다.

- main class: `CoffeeProjectApplication`
- profile: `local`


### 3) 설정 파일 준비
실제 실행 시에는 `application.yml`에 로컬 환경에 맞는 DB, JWT, Kafka, Redis 설정이 필요하다.
예시 파일은 `src/main/resources/application-example.yml`을 참고한다.

### 4) API 테스트
애플리케이션 실행 후 Postman을 통해 API를 테스트할 수 있다.

---

## 10. 프로젝트 구조

```text
Coffee-Project
├─ docs
│  ├─ api
│  ├─ erd
│  ├─ image
│  └─ policy
├─ k6
│  └─ order-pay-test.js
├─ src
│  ├─ main
│  │  ├─ java/kr/spartaclub/coffeeproject
│  │  │  ├─ common
│  │  │  │  ├─ config
│  │  │  │  ├─ entity
│  │  │  │  ├─ enums
│  │  │  │  ├─ exception
│  │  │  │  ├─ lock
│  │  │  │  ├─ response
│  │  │  │  └─ security
│  │  │  └─ domain
│  │  │     ├─ auth
│  │  │     ├─ cart
│  │  │     ├─ menu
│  │  │     ├─ order
│  │  │     ├─ point
│  │  │     └─ user
│  │  └─ resources
│  │     ├─ application.yml
│  │     └─ application-example.yml
│  └─ test
├─ docker-compose.yml
├─ build.gradle
└─ README.md
```

### 구조 설명
- `common` : 공통 설정, 예외 처리, 보안, 분산락 등 공통 모듈
- `domain` : 도메인 중심 구조로 인증, 메뉴, 장바구니, 주문, 포인트, 회원 기능 구성
- `docs` : API 명세서, ERD, 비즈니스 정책 문서 정리
- `k6` : 부하 및 동시성 테스트 스크립트 관리

---

## 11. 테스트 및 검증

### Postman 검증
- 전체 API 흐름을 Postman으로 검증하였다.
- 주문 생성 → 결제 → 취소 → 포인트 이력 → 인기 메뉴 반영까지 정상 동작을 확인하였다.

### Kafka 검증
- 주문 결제 트랜잭션 커밋 이후 `order-completed` 토픽으로 이벤트가 발행되는 것을 확인하였다.
- Consumer 로그를 통해 이벤트 수신 및 역직렬화가 정상 동작함을 확인하였다.

### Redis 검증
- Redis CLI 및 RedisInsight를 통해 `popular:menus` ZSet score 반영 결과를 확인하였다.
- 결제 성공 시 score 증가, 주문 취소 시 score 감소를 검증하였다.

### k6 검증
- 10 VUs / 5초 조건에서 동일 사용자 기준 결제 API에 동시 요청을 보내는 테스트를 수행하였다.
- 테스트 이후 포인트, 포인트 이력, 주문 상태, 인기 메뉴 집계 결과가 정상적으로 유지되는 것을 확인하였다.

---

## 12. ERD 및 데이터 설계

👉 자세한 ERD 및 데이터 설계 정책은 아래 문서를 참고하세요.    
👉 [erd.md](docs/erd/erd.md) 

---

## 13. API 명세서

👉 자세한 API 명세서는 아래 문서를 참고하세요.    
👉 [api](docs/api)

---

## 14. 트러블 슈팅

> 트러블 슈팅 정리는 추후 블로그 작성 후 링크를 추가할 예정입니다.

---

## 15. 향후 개선 사항

- Kafka 이벤트 발행 실패에 대한 재시도 및 재처리 전략을 고도화할 예정이다.
- 주문 완료 외에도 주문 취소, 포인트 변경 등 추가 도메인 이벤트를 확장할 예정이다.
- Redis 락 획득 실패(`429 Too Many Requests`)에 대한 자동화 테스트를 보강할 예정이다.
- 운영 환경을 고려하여 Kafka, Redis, DB 설정을 프로파일별로 분리하고 외부 설정 관리 방식을 개선할 예정이다.
- 현재는 단일 인스턴스 기반 개발 환경이므로, 향후 멀티 인스턴스 및 장애 상황을 고려한 검증 범위를 확장할 예정이다.

---

# ⚖️ License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
