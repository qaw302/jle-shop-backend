# Product Service

상품, 주문, 쿠폰, 할인, 결제까지 전자상거래 핵심 도메인을 다루는 Spring Boot 기반 백엔드 서비스입니다.


## 프로젝트 개요
- 목적: 커머스 핵심 기능을 통합한 상품 서비스 구축
- 특징: 도메인 중심 설계, QueryDSL 기반 복잡 조회, 결제/주문 트랜잭션 통합 처리
- 실행 환경: Java 17, Spring Boot 3.2.x, Gradle

## 핵심 기능
- 상품/카테고리: 상품 등록, 옵션/이미지/태그 관리, 카테고리 계층 구조 지원
- 할인/쿠폰: 전체/카테고리/상품 단위 할인, 쿠폰 발급/검색/보유 조회
- 주문/결제: 주문 생성, 주문 조회/관리자 상태 변경, 토스 결제 승인 통합
- 장바구니: 옵션별 장바구니 등록 및 수량 관리
- 회원/주소/배송: 회원/주소 관리, 탈퇴 처리, 배송 정보 관리
- API 문서: SpringDoc OpenAPI + Swagger UI 제공

## 기술 스택
- Language: Java 17
- Framework: Spring Boot 3.2.10, Spring MVC, Spring Validation
- Data: Spring Data JPA, QueryDSL, H2 (dev), MySQL (prod)
- Security: Spring Security (CORS, Header 기반 권한 체크)
- Docs: springdoc-openapi 2.0.2
- Build/Deploy: Gradle, Docker

## 도메인 구조
`src/main/java/com/smplatform/product_service/domain` 아래에 기능별로 분리되어 있습니다.
- product: 상품, 옵션, 이미지, 태그
- category: 카테고리 계층 구조
- discount: 할인 정책 및 대상
- coupon: 쿠폰 발급/보유/검색
- cart: 장바구니
- order: 주문/결제/주문검색
- member: 회원/주소/배송

## 실행 방법 (로컬)
```bash
./gradlew bootRun
```

기본 프로파일은 `dev`이며 H2 인메모리 DB를 사용합니다.
- 서비스 포트: `8080`
- H2 콘솔: `http://localhost:8080/h2-console`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`

관리자 기능은 요청 헤더로 권한을 구분합니다.
- `X-MEMBER-ID: <memberId>`
- `ROLE: ADMIN`

## 프로파일/환경 변수
`application-dev.yml`과 `application-prod.yml`을 사용합니다.

### dev (H2)
- `jdbc:h2:mem:testdb`
- `ddl-auto: create-drop`

### prod (MySQL)
필요한 환경 변수:
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `PAYMENT_TOSS_API`
- `BACKEND_SERVER_PORT`

## 테스트 및 샘플 요청
`http/` 폴더에 도메인별 테스트 시나리오가 정리되어 있습니다.
- `http/product-api-test.http`
- `http/order-api-test.http`
- `http/payment-test.http`
- `http/coupon.http`
- `http/cart.http`
- `http/member.http`

테스트 실행:
```bash
./gradlew test
```

## DB 스키마
MySQL 초기 스키마는 `src/main/resources/schema-mysql.sql`에 있습니다.

## Docker 실행
```bash
docker build -t product-service .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MYSQL_USERNAME=... \
  -e MYSQL_PASSWORD=... \
  -e PAYMENT_TOSS_API=... \
  -e BACKEND_SERVER_PORT=8080 \
  product-service
```

## 참고 문서
- API 설계서: `docs/api-design.md`
- 회원 API 변경사항: `docs/member-api-changes.md`

## 프로젝트 구조 요약
```
src
├─ main
│  ├─ java/com/smplatform/product_service
│  │  ├─ config
│  │  ├─ domain
│  │  ├─ advice
│  │  ├─ aop
│  │  └─ exception
│  └─ resources
│     ├─ application.yml
│     ├─ application-dev.yml
│     ├─ application-prod.yml
│     └─ schema-mysql.sql
└─ test
```
