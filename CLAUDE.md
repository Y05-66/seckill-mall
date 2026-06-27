# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A flash sale (秒杀) e-commerce platform with Spring Boot 2.7.18 backend and Vue 3 frontend. The system handles high-concurrency seckill scenarios using a multi-layer protection architecture: local cache → rate limiting → Redis pre-deduction → RabbitMQ async order creation → DB optimistic lock.

## Build & Run Commands

### Backend (requires MySQL, Redis, RabbitMQ on localhost)
```bash
# Build
mvn clean package -DskipTests

# Run (main class: com.seckill.mall.SeckillMallApplication)
mvn spring-boot:run

# Run with test profile (uses seckill_mall_test DB, Redis DB 1)
mvn spring-boot:run -Dspring.profiles.active=test

# API docs (springdoc-openapi)
# http://localhost:8080/swagger-ui.html

# Actuator monitoring
# http://localhost:8080/actuator/health
```

### PC Frontend (seckill-frontend/)
```bash
cd seckill-frontend
npm install
npm run dev        # Vite dev server on port 3000, proxies /api to :8080
npm run build      # Production build to dist/
```

### Mini-app Frontend (seckill-miniapp/)
```bash
cd seckill-miniapp
npm install
npm run dev:mp-weixin    # WeChat mini-program dev
npm run dev:mp-alipay    # Alipay mini-program dev
npm run dev:h5           # H5 dev
npm run build:mp-weixin  # WeChat production build
npm run build:mp-alipay  # Alipay production build
npm run build:h5         # H5 production build
```

### Docker (一键部署)
```bash
# Build frontend first, then:
docker-compose up -d
# Services: app(:8080), mysql(:3306), redis(:6379), rabbitmq(:5672/:15672), nginx(:9090)
# DB auto-initializes via schema.sql mounted to docker-entrypoint-initdb.d
# RabbitMQ management UI: http://localhost:15672 (guest/guest)
# Docker uses SPRING_PROFILES_ACTIVE=prod
```

### Windows One-Click Scripts
```bash
start.bat    # Starts backend (java -jar) + frontend (npm run dev), auto-detects LAN IP for miniapp
stop.bat     # Kills processes on ports 8080, 3000, 5173
```

### Database Setup
Execute `src/main/resources/schema.sql` manually against MySQL. Seeds admin (admin/admin123), test users, products, and seckill items.

## Architecture

### Tech Stack
- **Backend:** Java 17, Spring Boot 2.7.18, Spring Security (JWT), Spring Validation, MyBatis-Plus 3.5.5, MySQL, Redis (Lettuce), RabbitMQ (with DLX/retry), Redisson 3.20.1 (distributed locks), Caffeine (local cache), Guava RateLimiter, springdoc-openapi 1.7.0 (Swagger), Lombok, Hutool 5.8.25 (captcha), Apache POI 5.2.5 (Excel export), DashScope SDK 2.16.4 (AI assistant via Alibaba Cloud)
- **PC Frontend:** Vue 3.4, Vite 5, Vue Router 4, Pinia 2.1, Element Plus 2.5, Axios, ECharts 6
- **Mini-app Frontend:** uni-app (Vue 3, Pinia) — WeChat/Alipay/H5 targets via `@dcloudio/uni-app`
- **Infrastructure:** Nginx, Docker Compose, Logback, Spring Boot Actuator

### Backend Package Structure (`com.seckill.mall`)
| Package | Purpose |
|---|---|
| `annotation` | Custom annotations (`@Log` for AOP operation logging) |
| `aspect` | `LogAspect` — AOP aspect for operation logging |
| `common` | `Result<T>` response wrapper, `ResultCode`, `Constants`, `BusinessException`, `GlobalExceptionHandler` |
| `config` | SecurityConfig, RedisConfig, RabbitMQConfig (with DLX), CacheConfig (Caffeine), RedissonConfig, SwaggerConfig, WebMvcConfig, InitConfig |
| `controller` | User-facing: Goods, Seckill, Order, User, Captcha, Address, Banner, Cart, Coupon, Favorite, Notification, Points, Refund, Review, Export, AiAssistant, OperationLog; Admin: AdminGoods, AdminSeckill, AdminOrder, AdminUser |
| `dto` | Request/response DTOs |
| `entity` | MyBatis-Plus entities with `@TableName("t_xxx")` convention |
| `mapper` | MyBatis-Plus mapper interfaces (annotation-based, no XML) |
| `mq` | SeckillProducer, SeckillConsumer (with retry/DLX), SeckillMessage |
| `redis` | RedisService wrapper with typed key prefixes (GoodsKey, SeckillKey, UserKey, CaptchaKey) |
| `scheduler` | OrderTimeoutScheduler (auto-cancel unpaid orders after 15 min) |
| `security` | JwtUtils, JwtAuthFilter, LoginUser, UserDetailsServiceImpl |
| `service/impl` | Business logic services |
| `util` | PasswordUtils (BCrypt), UUIDUtils |

### Flash Sale Flow (critical path — 10-layer protection)
1. **Captcha verification** — math captcha via Hutool, answer stored in Redis (TTL 5min)
2. **Global rate limit** — Guava RateLimiter (500 QPS) at controller level
3. **User-level rate limit** — Redis fixed-window counter (5 req/user/sec)
4. **Caffeine local cache** — seckill goods list/detail cached locally (5-30s)
5. **Redis sold-out check** — fast reject if `SeckillKey.GOODS_OVER:{goodsId}` is set
6. **Redis duplicate purchase check** — `SeckillKey.SECKILL_USER:{userId}:{goodsId}` (TTL 1h), fallback to DB
7. **Redis atomic DECR** — pre-deducts stock via `SeckillKey.STOCK:{goodsId}`
8. **RabbitMQ async** — sends SeckillMessage, consumer creates order with retry (max 3, DLX for failures)
9. **DB optimistic lock** — `UPDATE t_seckill_goods SET stock_count = stock_count - 1 WHERE id = ? AND stock_count > 0`
10. **Frontend polling** — client polls `GET /seckill/result/{goodsId}` for queuing/success/fail status

### MQ Retry & Dead Letter
- Main queue `seckill.queue` → consumer fails → retry queue `seckill.retry.queue` (TTL 10s) → back to main queue
- After 3 retries → dead letter queue `seckill.dlx.queue` (manual processing)
- Consumer also rolls back Redis stock on final failure

### Order Timeout
- `OrderTimeoutScheduler` runs every 60s, scans orders with `status=0` and `create_time > 15min ago`
- Auto-cancels with DB stock rollback + Redis stock rollback + deletes `t_seckill_success` record
- Order status flow: unpaid(0) → paid(1) | cancelled(2) | timeout(3) | refunded(4)

### Authentication
- Stateless JWT via `JwtAuthFilter` (Bearer token in Authorization header)
- `LoginUser` extends Spring Security's `User` with `userId` field
- Public (in SecurityConfig): `/user/login`, `/user/register`, `/captcha`, `GET /goods/**`, `/ai/**`, `/uploads/**`, `/actuator/health`, `/actuator/info`, Swagger paths
- Admin-only: `/admin/**` requires `ROLE_ADMIN`; sensitive Actuator endpoints (`/actuator/**`) also require ADMIN

### Response Format
All endpoints return `Result<T>`: `{ "code": 200, "msg": "...", "data": ... }`

### Redis Key Strategy
Keys use `prefix:businessKey` format via `KeyPrefix` interface with configurable TTLs:
- `SeckillKey.STOCK:{id}` — stock count (no TTL, initialized at startup via `InitConfig`)
- `SeckillKey.GOODS_OVER:{id}` — sold-out marker (no TTL)
- `SeckillKey.SECKILL_USER:{userId}:{goodsId}` — duplicate purchase marker (1h TTL)
- `GoodsKey.GOODS_LIST` / `GOODS_DETAIL:{id}` — 60s cache
- `CaptchaKey.CAPTCHA:{captchaId}` — captcha answer (5min TTL)
- `CaptchaKey.RATE_LIMIT:user:{userId}` — rate limit counter (1s TTL)

### Frontend Architecture
- **PC Frontend** (`seckill-frontend/`): Vue 3 SPA with Element Plus, uses `src/api/*.js` modules for backend calls, `src/store/user.js` for Pinia state, `src/views/*.vue` for pages, `src/views/admin/*.vue` for admin pages
- **Mini-app** (`seckill-miniapp/`): uni-app with `src/pages/*.vue` for main pages, `src/pages-admin/*.vue` for admin, `src/components/*.vue` for shared components. API host configured in `src/config.js`
- Both frontends use axios wrappers (`src/utils/request.js`) that add JWT token and handle `{code, msg, data}` responses
- Vite dev proxy: frontend calls `/api/xxx` → rewritten to `http://localhost:8080/xxx` (strips `/api` prefix); `/uploads` is also proxied directly

## Key Files
| Purpose | Path |
|---|---|
| Maven POM | `pom.xml` |
| Main class | `src/main/java/com/seckill/mall/SeckillMallApplication.java` |
| Application config | `src/main/resources/application.yml` |
| Test profile | `src/main/resources/application-test.yml` |
| Logback config | `src/main/resources/logback-spring.xml` |
| Database schema + seed data | `src/main/resources/schema.sql` |
| Security config | `src/main/java/com/seckill/mall/config/SecurityConfig.java` |
| RabbitMQ config (DLX/retry) | `src/main/java/com/seckill/mall/config/RabbitMQConfig.java` |
| Caffeine cache config | `src/main/java/com/seckill/mall/config/CacheConfig.java` |
| Redisson config | `src/main/java/com/seckill/mall/config/RedissonConfig.java` |
| Seckill core logic | `src/main/java/com/seckill/mall/service/impl/SeckillServiceImpl.java` |
| Order timeout scheduler | `src/main/java/com/seckill/mall/scheduler/OrderTimeoutScheduler.java` |
| Captcha controller | `src/main/java/com/seckill/mall/controller/CaptchaController.java` |
| Redis service | `src/main/java/com/seckill/mall/redis/RedisService.java` |
| JWT utils | `src/main/java/com/seckill/mall/security/JwtUtils.java` |
| Constants | `src/main/java/com/seckill/mall/common/Constants.java` |
| AOP logging | `src/main/java/com/seckill/mall/aspect/LogAspect.java` |
| Docker Compose | `docker-compose.yml` |
| PC frontend API proxy | `seckill-frontend/vite.config.js` |
| Mini-app API host config | `seckill-miniapp/src/config.js` |
| Uploaded files directory | `uploads/` |

## Conventions
- All SQL is annotation-based MyBatis-Plus (no XML mappers despite config pointing to empty directory)
- Entities use `@TableName("t_xxx")` naming convention
- Redis keys are defined as static constants in `GoodsKey`, `SeckillKey`, `UserKey`, `CaptchaKey`
- No tests exist yet — high-priority tech debt (pom.xml includes `spring-boot-starter-test` but no test classes)
- Hutool is used for captcha generation (`CaptchaUtil.createLineCaptcha`)
- Redisson is used for distributed locks in admin operations (status change, stock replenishment)
- Caffeine local cache uses Spring `@Cacheable`/`@CacheEvict` annotations
- Controller methods use `@AuthenticationPrincipal LoginUser loginUser` to get current user
- Admin endpoints use `@PreAuthorize("hasRole('ADMIN')")` or `SecurityConfig` path-based rules
- Use `@Log` annotation on controller methods for AOP operation logging
- DTO validation uses `@Valid` + Jakarta Validation annotations (`@NotBlank`, `@NotNull`, `@Size`, etc.)

## Known Issues
- `application.yml` references `mapper-locations: classpath:mapper/*.xml` but no XML files exist (all SQL is annotation-based)
- MyBatis-Plus logical delete config (`deleted` field) is enabled in `application.yml` but no entity has this field
- `Constants.SECKILL_LIMIT_QPS` (10) doesn't match actual `RateLimiter.create(500)` in SeckillController
- `application.yml` contains hardcoded credentials (MySQL password `123456`, JWT secret, AI API key) — the `.example` template uses env var placeholders
- Potential concurrency bug in `cancelOrder()` — needs investigation
- `uploads/` directory stores user-uploaded files (avatars); served via `/uploads/**` (public in SecurityConfig, proxied in Vite, mounted in Nginx)
