# Product Service

쇼핑몰 플랫폼의 상품 관리 및 주문 처리를 담당하는 백엔드 서비스입니다.

## 👨‍💻 담당 업무 및 기여도

### 전체 프로젝트 기여율
- **전체 기능**: 30% (5명 중 1명)
- **백엔드 기능**: 40% (백엔드 3명 중)

### 주요 담당 기능
#### 쿠폰 시스템 
- 쿠폰 생성/발급/관리 기능
- 회원별 쿠폰 소유 및 사용 처리
- 쿠폰 할인 금액 계산 로직

#### 공통 기능
- 토큰 발급 및 인증 기능
- 주문 사용자/관리자 조회 기능
- 전역 예외 처리

### 기술적 기여
- Docker 기반 배포 환경 구성
- GitHub Actions CI/CD 파이프라인 구축
- QueryDSL 설정 및 복잡한 검색 쿼리 구현
- API 문서화 (Swagger)

## 🛠 사용 기술
### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.10
- **ORM**: Spring Data JPA, QueryDSL 5.0.0
- **Database**: MySQL 8.0 (Production), H2 (Development)
- **Security**: Spring Security, BCrypt
- **Build Tool**: Gradle 8.10.2

### Infrastructure
- **Container**: Docker
- **CI/CD**: GitHub Actions
- **Cloud**: Oracle Cloud Infrastructure
- **Cache**: Redis

### Documentation
- **API Docs**: Swagger (SpringDoc OpenAPI 2.0.2)


## 🏗 배포 환경

### Production
```yaml
Server: Oracle Cloud Infrastructure (ARM64)
Database: MySQL 8.0
Container: Docker
Reverse Proxy: Nginx
CI/CD: GitHub Actions
```

### Development
```yaml
Database: H2 In-Memory
Port: 8080
Profile: dev
```

## 🔧 서비스 구조

### Architecture
```
[Client] 
   ↓
[Nginx]
   ↓
[Gateway Service (Docker Container)]   →   [Token Service (Docker Container)]   →   [Redis]
   ↓
[Product Service (Docker Container)]
   ↓
[MySQL Database]
```

### CI/CD Pipeline
```
1. Push to main branch
   ↓
2. GitHub Actions triggered
   ↓
3. Build with Gradle
   ↓
4. Build Docker Image (linux/arm64)
   ↓
5. Push to Docker Hub
   ↓
6. SSH to Oracle Cloud Server
   ↓
7. Pull latest image & Deploy
```

### Package Structure
```
com.smplatform.product_service
├── domain
│   ├── cart          # 장바구니
│   ├── category      # 카테고리
│   ├── coupon        # 쿠폰
│   ├── discount      # 할인
│   ├── member        # 회원
│   ├── option        # 옵션
│   ├── order         # 주문
│   └── product       # 상품
├── config            # 설정 (Security, JPA, QueryDSL 등)
├── exception         # 전역 예외 처리
└── aop              # AOP (권한 검증)
```

## 🚀 실행 방법

### Local 개발 환경
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Docker 배포
```bash
# 이미지 빌드
docker build -t product-service .

# 컨테이너 실행
docker run -p 8080:8080 \
  -e MYSQL_USERNAME=your_username \
  -e MYSQL_PASSWORD=your_password \
  -e PAYMENT_TOSS_API=your_api_key \
  product-service
```

## 📝 API 문서

서비스 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
```
http://localhost:8080/swagger-ui/index.html
```

## 🔑 주요 구현 내용

### 1. 계층형 카테고리 구조
- 대/중/소 분류 3단계 카테고리
- 상위 카테고리 조회 시 하위 카테고리 상품까지 포함

### 2. 할인 정책 자동 적용
- 상품 등록 시 카테고리별/전체 할인 자동 적용
- 할인 우선순위: 개별 상품 할인 > 카테고리 할인 > 전체 할인

### 3. 결제 통합 처리
- 토스페이먼츠 결제 승인과 주문 처리를 하나의 트랜잭션으로 처리
- 결제 성공 시 자동으로 주문 상태 변경 및 재고 차감
- 멱등성 보장으로 중복 결제 방지

### 4. QueryDSL 기반 동적 쿼리
- 복잡한 검색 조건을 처리하는 동적 쿼리 구현
- 관리자/사용자별 다른 검색 조건 지원

### 5. 권한 기반 접근 제어
- AOP를 활용한 관리자 권한 검증
- 사용자별 접근 가능한 API 분리

## 📌 관련 링크

- [Organization](https://github.com/shoppingmall-platform)
- [Frontend Repository](https://github.com/shoppingmall-platform/jle-front)
- [Gateway Service](https://github.com/shoppingmall-platform/gateway-service)
- [Token Service](https://github.com/shoppingmall-platform/token-service)
