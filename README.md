# 🛒 Shopping Mall API

Spring Boot 기반의 RESTful 쇼핑몰 API 서버입니다. 사용자 인증, 상품 관리, 장바구니, 주문 기능을 제공합니다.  
Swagger를 활용한 문서 자동화와 테스트 환경도 구축되어 있어 협업 및 확장에 용이합니다.

---

## 📋 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 개발 기간 | 2025.07.28 ~  |
| 기술 스택 | Spring Boot, JPA, MySQL, Spring Security, JWT, Swagger |
| API 문서 | Swagger UI (OpenAPI 3.0) |
| 테스트 | postman |

---

## 🛠 기술 스택

- **Backend**: Spring Boot 3.x, Spring Data JPA, Spring Security
- **Authentication**: JWT
- **Database**: MySQL 8.0
- **Documentation**: Swagger (OpenAPI 3.0)
- **Testing**: postman
- **Build Tool**: Gradle

---

## 🧩 프로젝트 구조
```jsx
shopping-mall-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── shoppingmall/
│   │   │               ├── ShoppingMallApplication.java
│   │   │               ├── config/
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── SwaggerConfig.java
│   │   │               │   └── JwtConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── UserController.java
│   │   │               │   ├── ProductController.java
│   │   │               │   ├── CartController.java
│   │   │               │   └── OrderController.java
│   │   │               ├── service/
│   │   │               │   ├── AuthService.java
│   │   │               │   ├── UserService.java
│   │   │               │   ├── ProductService.java
│   │   │               │   ├── CartService.java
│   │   │               │   └── OrderService.java
│   │   │               ├── repository/
│   │   │               │   ├── UserRepository.java
│   │   │               │   ├── ProductRepository.java
│   │   │               │   ├── CartRepository.java
│   │   │               │   └── OrderRepository.java
│   │   │               ├── entity/
│   │   │               │   ├── User.java
│   │   │               │   ├── Product.java
│   │   │               │   ├── Cart.java
│   │   │               │   ├── CartItem.java
│   │   │               │   ├── Order.java
│   │   │               │   └── OrderItem.java
│   │   │               ├── dto/
│   │   │               │   ├── request/
│   │   │               │   │   ├── LoginRequest.java
│   │   │               │   │   ├── RegisterRequest.java
│   │   │               │   │   ├── ProductRequest.java
│   │   │               │   │   ├── CartItemRequest.java
│   │   │               │   │   └── OrderRequest.java
│   │   │               │   └── response/
│   │   │               │       ├── ApiResponse.java
│   │   │               │       ├── AuthResponse.java
│   │   │               │       ├── ProductResponse.java
│   │   │               │       ├── CartResponse.java
│   │   │               │       └── OrderResponse.java
│   │   │               ├── security/
│   │   │               │   ├── JwtTokenProvider.java
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   └── CustomUserDetailsService.java
│   │   │               ├── exception/
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   ├── CustomException.java
│   │   │               │   └── ErrorCode.java
│   │   │               └── util/
│   │   │                   └── ResponseUtil.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── data.sql
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── shoppingmall/
│                       ├── controller/
│                       ├── service/
│                       └── repository/
├── build.gradle
├── README.md
└── docs/
    ├── api-docs.md
    └── postman/
        └── shopping-mall-api.postman_collection.json
```
---

## 🔐 인증 & 권한

- JWT 기반 인증 (로그인 성공 시 토큰 발급)
- Swagger에서도 Bearer 토큰 입력 가능
- 관리자 전용 API 권한 분리 (`hasRole("ADMIN")`)

---

## 📦 Swagger API 문서

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)  
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### ✅ Swagger 설정 예시 (`SwaggerConfig.java`)
```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("bearer-jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")))
            .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
            .info(new Info().title("Shopping Mall API")
                            .description("쇼핑몰 REST API 문서")
                            .version("1.0.0"));
    }
}
```
