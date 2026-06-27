# 秒杀商城 (Seckill Mall)

高并发限时抢购电商平台，采用多层防护架构应对秒杀场景下的高并发压力。

## 🚀 快速开始

### 一键启动（推荐）

```bash
# Windows 双击运行
start.bat    # 启动所有服务
stop.bat     # 停止所有服务
```

启动后访问：
- PC 前端：http://localhost:3000
- 后端 API：http://localhost:8080
- Swagger 文档：http://localhost:8080/swagger-ui.html

### 手动启动

```bash
# 1. 启动后端
mvn clean package -DskipTests
java -jar target/seckill-mall-1.0.0.jar

# 2. 启动 PC 前端
cd seckill-frontend
npm install
npm run dev

# 3. 启动小程序（可选）
cd seckill-miniapp
npm install
npm run dev:mp-weixin  # 微信小程序
npm run dev:h5         # H5 模式
```

### Docker 部署

```bash
# 先构建前端
cd seckill-frontend && npm run build && cd ..

# 一键部署
docker-compose up -d
```

---

## 📁 项目结构

```
seckill-mall/
├── src/main/java/com/seckill/mall/
│   ├── annotation/          # 自定义注解 (@Log)
│   ├── aspect/              # AOP切面 (操作日志)
│   ├── common/              # 公共类 (Result, Constants, 异常处理)
│   ├── config/              # 配置类 (Security, Redis, RabbitMQ, Cache)
│   ├── controller/          # 控制器 (用户/商品/秒杀/订单/管理端)
│   ├── dto/                 # 数据传输对象
│   ├── entity/              # 实体类 (MyBatis-Plus)
│   ├── mapper/              # Mapper接口 (注解方式，无XML)
│   ├── mq/                  # RabbitMQ 生产者/消费者
│   ├── redis/               # Redis服务封装 + Key前缀
│   ├── scheduler/           # 定时任务 (订单超时取消)
│   ├── security/            # JWT认证
│   ├── service/impl/        # 业务服务实现
│   └── util/                # 工具类
├── src/main/resources/
│   ├── application.yml      # 主配置
│   ├── application-test.yml # 测试环境配置
│   ├── schema.sql           # 数据库建表+测试数据
│   └── logback-spring.xml   # 日志配置
├── seckill-frontend/        # PC前端 (Vue 3 + Element Plus)
├── seckill-miniapp/         # 小程序前端 (uni-app)
├── nginx/                   # Nginx配置
├── uploads/                 # 上传文件目录
├── logs/                    # 日志目录
├── start.bat                # 一键启动脚本
├── stop.bat                 # 一键停止脚本
├── docker-compose.yml       # Docker编排
├── Dockerfile               # Docker构建
└── pom.xml                  # Maven配置
```

---

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17, Spring Boot 2.7.18, MyBatis-Plus 3.5.5 |
| 前端 | Vue 3.4, Vite 5, Element Plus 2.5, Pinia 2.1 |
| 小程序 | uni-app (Vue 3), 适配微信/支付宝/H5 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 (Lettuce) |
| 消息队列 | RabbitMQ 3.13.7 (DLX/重试) |
| 分布式锁 | Redisson 3.20.1 |
| 本地缓存 | Caffeine |
| 限流 | Guava RateLimiter |
| API文档 | springdoc-openapi 1.7.0 |
| 监控 | Spring Boot Actuator |

---

## 🔥 核心功能

### 秒杀流程（10层防护）

```
用户请求
  ↓
① 验证码校验（Hutool生成，Redis存储，TTL 5min）
  ↓
② 全局限流（Guava RateLimiter 500 QPS）
  ↓
③ Caffeine本地缓存（5-30秒）
  ↓
④ Redis售罄检查（GOODS_OVER标记）
  ↓
⑤ Redis重复购买检查（SECKILL_USER标记）
  ↓
⑥ Redis原子DECR预扣库存（负数时回滚）
  ↓
⑦ RabbitMQ异步下单（重试3次 + DLX死信队列）
  ↓
⑧ DB乐观锁扣库存
  ↓
⑨ 前端轮询结果
```

### MQ重试机制

```
主队列 seckill.queue
  ↓ 消费失败
重试队列 seckill.retry.queue (TTL 10s)
  ↓ 过期后回到主队列
  ↓ 重试3次仍失败
死信队列 seckill.dlx.queue
  ↓ 回滚Redis库存
人工处理
```

### 订单超时取消

- `OrderTimeoutScheduler` 每60秒扫描一次
- 自动取消超过15分钟未支付的订单
- 回滚数据库库存 + Redis库存
- 删除秒杀成功记录

### AI购物助手

- 基于阿里云百练平台（通义千问大模型）
- 支持三种模型切换：qwen-turbo（极速）、qwen-plus（增强）、qwen-max（旗舰）
- 多会话管理，消息持久化到数据库
- 支持复制消息、重新生成回复
- 服务不可用时自动降级为关键词匹配回复
- PC端：浮动聊天窗口，支持Markdown渲染
- 小程序端：独立全屏页面，原生滚动

### 用户认证与安全

- JWT无状态认证，Token有效期24小时
- 服务端登出：Redis Token黑名单，即时失效
- 密码重置：忘记密码时通过用户名重置
- 统一设置页：个人信息、密码修改、账号信息

---

## 📊 数据库设计

### 核心表

| 表名 | 说明 |
|------|------|
| `t_user` | 用户表（BCrypt加密） |
| `t_goods` | 商品表 |
| `t_seckill_goods` | 秒杀商品表 |
| `t_seckill_order` | 秒杀订单表 |
| `t_order_detail` | 订单详情表 |
| `t_seckill_success` | 秒杀成功记录（唯一约束防重复） |
| `t_cart` | 购物车 |
| `t_address` | 收货地址 |
| `t_favorite` | 收藏 |
| `t_review` | 评价 |
| `t_coupon` / `t_user_coupon` | 优惠券 |
| `t_user_points` / `t_points_log` | 积分系统 |
| `t_refund` | 退款 |
| `t_notification` | 消息通知 |
| `t_banner` | 轮播图 |
| `t_operation_log` | 操作日志 |
| `t_ai_conversation` | AI会话 |
| `t_ai_message` | AI消息（持久化） |

### 订单状态流转

```
未支付(0) ──支付──→ 已支付(1)
    │
    ├──取消──→ 已取消(2)
    │
    ├──超时──→ 已超时(3)
    │
    └──退款──→ 已退款(4)
```

---

## 🔐 Redis Key 设计

| Key 前缀 | 说明 | TTL |
|---------|------|-----|
| `stock:{id}` | 秒杀库存 | 无 |
| `goods_over:{id}` | 售罄标记 | 无 |
| `seckill_user:{uid}:{gid}` | 用户秒杀标记 | 1小时 |
| `captcha:{id}` | 验证码答案 | 5分钟 |
| `login_rate:{ip}` | 登录限流 | 1分钟 |
| `seckill_rate:user:{uid}` | 秒杀限流 | 1秒 |
| `goods_list:all` | 商品列表缓存 | 60秒 |
| `goods_detail:{id}` | 商品详情缓存 | 60秒 |
| `cart_list:{uid}` | 购物车缓存 | 5分钟 |
| `favorite_list:{uid}` | 收藏列表缓存 | 5分钟 |
| `favorite_check:{uid}:{gid}` | 收藏状态缓存 | 5分钟 |
| `coupon_available:all` | 可用优惠券缓存 | 5分钟 |
| `banner_list:active` | 轮播图缓存 | 10分钟 |

---

## 🔌 API 接口

### 用户模块 `/user`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/user/register` | 注册 | ❌ |
| POST | `/user/login` | 登录 | ❌ |
| POST | `/user/logout` | 退出登录（Token加入黑名单） | ✅ |
| GET | `/user/info` | 获取用户信息 | ✅ |
| PUT | `/user/info` | 修改个人信息 | ✅ |
| PUT | `/user/password` | 修改密码 | ✅ |
| POST | `/user/reset-password` | 重置密码（忘记密码） | ❌ |
| POST | `/user/avatar` | 上传头像 | ✅ |

### 商品模块 `/goods`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/goods/list` | 商品列表 | ❌ |
| GET | `/goods/{id}` | 商品详情 | ❌ |
| GET | `/goods/seckill/list` | 秒杀商品列表 | ❌ |
| GET | `/goods/seckill/{id}` | 秒杀商品详情 | ❌ |

### 秒杀模块 `/seckill`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/seckill/do` | 执行秒杀 | ✅ |
| GET | `/seckill/result/{id}` | 查询秒杀结果 | ✅ |

### 订单模块 `/order`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/order/list` | 我的订单 | ✅ |
| GET | `/order/{orderNo}` | 订单详情 | ✅ |
| POST | `/order/pay/{orderNo}` | 模拟支付 | ✅ |
| POST | `/order/cancel/{orderNo}` | 取消订单 | ✅ |
| DELETE | `/order/{orderNo}` | 删除订单（已取消/超时/退款） | ✅ |

### 购物车 `/cart`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/cart/add` | 添加商品 | ✅ |
| GET | `/cart/list` | 购物车列表 | ✅ |
| PUT | `/cart/update` | 更新数量 | ✅ |
| DELETE | `/cart/{id}` | 删除商品 | ✅ |

### 管理端 `/admin`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/goods/list` | 商品管理 |
| POST | `/admin/goods` | 新增秒杀商品 |
| PUT | `/admin/goods/{id}/stock` | 补充库存 |
| GET | `/admin/seckill/list` | 秒杀活动管理 |
| PUT | `/admin/seckill/{id}/status` | 修改活动状态 |
| GET | `/admin/order/list` | 订单管理 |
| GET | `/admin/order/statistics` | 订单统计 |
| GET | `/admin/user/list` | 用户管理 |
| PUT | `/admin/user/{id}/role` | 修改角色 |
| PUT | `/admin/user/{id}/status` | 启用/禁用 |

### AI助手模块 `/ai`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/ai/chat` | 发送消息 | ❌ |
| GET | `/ai/models` | 获取可用模型列表 | ❌ |
| POST | `/ai/conversation` | 创建新会话 | ✅ |
| GET | `/ai/conversations` | 获取会话列表 | ✅ |
| DELETE | `/ai/conversation/{id}` | 删除会话 | ✅ |
| GET | `/ai/history` | 获取会话历史 | ✅ |

### 验证码 `/captcha`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/captcha` | 获取验证码（Base64图片+captchaId） | ❌ |

---

## 👤 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| user1 | 123456 | 普通用户 |
| user2 | 123456 | 普通用户 |

---

## ⚙️ 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0
- Redis 7.x
- RabbitMQ 3.13+

---

## 🐳 Docker 服务

| 服务 | 端口 | 说明 |
|------|------|------|
| app | 8080 | Spring Boot 应用 |
| mysql | 3306 | MySQL 数据库 |
| redis | 6379 | Redis 缓存 |
| rabbitmq | 5672/15672 | RabbitMQ 消息队列 |
| nginx | 9090 | Nginx 反向代理 |

---

## 📝 开发规范

### 代码规范
- 实体类使用 `@TableName("t_xxx")` 命名
- Redis Key 定义在 `redis/keyprefix/` 下的常量类中
- 所有 SQL 使用 MyBatis-Plus 注解方式（无 XML）
- Controller 使用 `@AuthenticationPrincipal LoginUser` 获取当前用户
- 管理端接口使用 `@PreAuthorize("hasRole('ADMIN')")` 权限控制

### Git 提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具相关
```

---

## ❓ 常见问题

### Redis 连接失败
```bash
redis-cli ping  # 检查Redis是否启动
```

### RabbitMQ 连接失败
```bash
rabbitmqctl status  # 检查RabbitMQ状态
```

### 数据库表不存在
执行 `src/main/resources/schema.sql`

### 前端启动失败
```bash
cd seckill-frontend
rm -rf node_modules
npm install
```

### 小程序无法访问API
1. 确保手机和电脑在同一局域网
2. 运行 `start.bat` 自动获取IP并配置
3. 或手动修改 `seckill-miniapp/src/config.js` 中的 `API_HOST`

---

## 📄 License

MIT License
