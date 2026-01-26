# JLE | 의류 쇼핑몰 (10개월/5명)
쇼핑몰 플랫폼의 상품 관리 및 주문 처리에서 필요한 기본적인 기능을 구현한 프로젝트입니다.
> http://jleshop.duckdns.org/


## 👨‍💻 주요 담당 기능
### 카테고리
- 카테고리 생성/수정/삭제/조회

### 쿠폰 
- 쿠폰 생성/발급/관리 기능
- 회원별 쿠폰 소유 및 사용 처리
- 쿠폰 할인 금액 계산 로직

### 공통 기능
- 토큰 발급 및 인증 기능
- 주문 사용자/관리자 조회 기능
- 전역 예외 처리

### 기술적 기여
- Docker 기반 배포 환경 구성
- GitHub Actions CI/CD 파이프라인 구축
- API 문서화 (Swagger)

---
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

---
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

---
## 📝 API 문서

서비스 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다
> http://localhost:8080/swagger-ui/index.html

---
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

---
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

---
## 📌 관련 링크

- [Organization](https://github.com/shoppingmall-platform)
- [Frontend Repository](https://github.com/shoppingmall-platform/jle-front)
- [Gateway Service](https://github.com/shoppingmall-platform/gateway-service)
- [Token Service](https://github.com/shoppingmall-platform/token-service)
