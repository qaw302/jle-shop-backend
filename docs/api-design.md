# API 설계서

전자상거래 핵심 도메인(회원/상품/주문/결제/쿠폰/할인/장바구니/카테고리)을 제공하는 API 설계 요약입니다.

## 기본 정보
- Base URL: `http://localhost:8080`
- Content-Type: `application/json`
- 문서: Swagger UI `http://localhost:8080/swagger-ui/index.html`

## 인증/권한 헤더
- 사용자 요청: `X-MEMBER-ID: <memberId>`
- 관리자 요청: `X-MEMBER-ID: <memberId>`, `ROLE: ADMIN`
- 관리자 전용 API는 `@AdminOnly`로 보호됩니다.

## API 목록

### Member
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/v1/members` | Public | 회원 생성 |
| GET | `/v1/members/me` | User | 내 정보 조회 |
| GET | `/v1/members` | Public | 회원 전체 조회 |
| POST | `/v1/members/search` | Public | 회원 검색 |
| POST | `/v1/members/me/update` | User | 내 정보 수정 |
| POST | `/v1/members/me/update/auth` | User | 비밀번호 수정 |
| POST | `/v1/members/me/withdraw` | User | 회원 탈퇴 |
| POST | `/v1/members/{id}/withdraw` | Admin | 관리자 회원 탈퇴 |
| GET | `/v1/members/credential/{id}` | Public | 로그인 정보 조회 |

### Address
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/v1/address` | User | 배송지 등록 |
| GET | `/v1/address` | User | 배송지 조회 |
| POST | `/v1/update-address` | User | 배송지 수정 |
| POST | `/v1/delete-address` | User | 배송지 삭제 |

### Category
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET | `/v1/categories` | Public | 카테고리 목록 조회 |
| GET | `/v1/categories/{category-id}` | Public | 카테고리 단건 조회 |
| POST | `/v1/categories` | Admin | 카테고리 등록 |
| POST | `/v1/categories/update-category` | Admin | 카테고리 수정 |
| POST | `/v1/categories/delete-category` | Admin | 카테고리 삭제 |

### Product
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET | `/v1/products/{productId}` | Public | 상품 단건 조회 |
| POST | `/v1/products` | Admin | 상품 등록 |
| POST | `/v1/products/update-product` | Admin | 상품 수정 |
| DELETE | `/v1/products/{productId}` | Admin | 상품 삭제(soft delete) |
| POST | `/v1/{category-id}/products` | Public | 사용자용 상품 목록/검색 |
| POST | `/v1/products/read` | Admin | 관리자용 상품 목록/검색 |
| GET | `/v1/products/tags` | Admin | 태그 목록 조회 |
| GET | `/v1/product-category` | Admin | 상품-카테고리 매핑 조회 |

### Option
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/v1/options` | Public | 옵션 타입 등록 |
| GET | `/v1/options` | Public | 옵션 목록 조회 |
| POST | `/v1/options/delete-option` | Admin | 옵션 삭제 |

### Discount
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET | `/v1/discounts` | Public | 할인 목록 조회 (조건 검색) |
| POST | `/v1/discounts` | Public | 할인 등록 |
| POST | `/v1/discounts/delete-discount` | Public | 할인 삭제 |

### Coupon
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/v1/coupons` | Admin | 쿠폰 생성 |
| POST | `/v1/coupons/search` | Admin | 쿠폰 검색 |
| POST | `/v1/coupons/delete` | Admin | 쿠폰 삭제 |

### Member Coupon
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/v1/coupons/issue` | User | 쿠폰 발급 |
| GET | `/v1/members/me/coupons` | User | 보유 쿠폰 조회 |

### Cart
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/v1/members/cart` | User | 장바구니 추가 |
| GET | `/v1/members/cart` | User | 장바구니 조회 |
| POST | `/v1/members/cart/option-update` | User | 장바구니 옵션 수정 |
| POST | `/v1/members/cart/delete` | User | 장바구니 삭제 |

### Order
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/v1/orders` | User | 주문 생성 |
| GET | `/v1/orders/{orderId}` | User | 주문 상세 조회 |
| DELETE | `/v1/orders/{orderId}` | User | 주문 취소 |
| POST | `/v1/orders/search` | User | 주문 검색/목록 |

### Admin Order
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/v1/admin/orders/search` | Admin | 관리자 주문 검색 |
| POST | `/v1/admin/orders/status` | Admin | 관리자 주문 상태 변경 |
| GET | `/v1/admin/orders/{orderId}` | Admin | 관리자 주문 상세 조회 |

### Payment
| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/v1/payments/confirm` | Public | 결제 승인 + 주문 처리 |

## 참고
- 도메인별 상세 요청/응답 예시는 `http/` 폴더의 시나리오 파일을 참고하세요.
- 회원 API 변경사항은 `docs/member-api-changes.md`에 정리되어 있습니다.
