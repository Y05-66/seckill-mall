-- ========================================
-- 秒杀商城数据库建表脚本
-- ========================================

CREATE DATABASE IF NOT EXISTS seckill_mall DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE seckill_mall;

-- 1. 用户表
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname    VARCHAR(50)  DEFAULT '' COMMENT '昵称',
    phone       VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    email       VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    avatar      VARCHAR(255) DEFAULT '' COMMENT '头像URL',
    role        TINYINT      DEFAULT 0 COMMENT '角色：0-普通用户 1-管理员',
    status      TINYINT      DEFAULT 0 COMMENT '状态：0-启用 1-禁用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 商品表
DROP TABLE IF EXISTS t_goods;
CREATE TABLE t_goods (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_name  VARCHAR(200) NOT NULL COMMENT '商品名称',
    goods_title VARCHAR(500) DEFAULT '' COMMENT '商品标题',
    goods_img   VARCHAR(500) DEFAULT '' COMMENT '商品图片',
    goods_price DECIMAL(10,2) NOT NULL COMMENT '商品原价',
    goods_stock INT          NOT NULL DEFAULT 0 COMMENT '商品库存',
    goods_desc  TEXT COMMENT '商品描述',
    status      TINYINT      DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 3. 秒杀商品表
DROP TABLE IF EXISTS t_seckill_goods;
CREATE TABLE t_seckill_goods (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id      BIGINT       NOT NULL COMMENT '关联商品ID',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    stock_count   INT          NOT NULL COMMENT '秒杀库存',
    start_time    DATETIME     NOT NULL COMMENT '秒杀开始时间',
    end_time      DATETIME     NOT NULL COMMENT '秒杀结束时间',
    status        TINYINT      DEFAULT 1 COMMENT '状态：0-未开始 1-进行中 2-已结束',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goods_id) REFERENCES t_goods(id),
    INDEX idx_goods_id (goods_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- 4. 秒杀订单表
DROP TABLE IF EXISTS t_seckill_order;
CREATE TABLE t_seckill_order (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT      NOT NULL COMMENT '用户ID',
    seckill_goods_id BIGINT      NOT NULL COMMENT '秒杀商品ID',
    order_no         VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    status           TINYINT     DEFAULT 0 COMMENT '状态：0-未支付 1-已支付 2-已取消 3-已超时',
    create_time      DATETIME    DEFAULT CURRENT_TIMESTAMP,
    pay_time         DATETIME    COMMENT '支付时间',
    INDEX idx_user_goods (user_id, seckill_goods_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status_create (status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- 5. 订单详情表
DROP TABLE IF EXISTS t_order_detail;
CREATE TABLE t_order_detail (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no    VARCHAR(64)   NOT NULL COMMENT '订单号',
    goods_id    BIGINT        NOT NULL COMMENT '商品ID',
    goods_name  VARCHAR(200)  COMMENT '商品名称',
    goods_price DECIMAL(10,2) COMMENT '秒杀价',
    goods_count INT           DEFAULT 1 COMMENT '数量',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';

-- 6. 秒杀成功记录表
DROP TABLE IF EXISTS t_seckill_success;
CREATE TABLE t_seckill_success (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT      NOT NULL COMMENT '用户ID',
    seckill_goods_id BIGINT      NOT NULL COMMENT '秒杀商品ID',
    order_no         VARCHAR(64) COMMENT '订单号',
    create_time      DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_goods (user_id, seckill_goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀成功记录表';

-- 7. 购物车表
DROP TABLE IF EXISTS t_cart;
CREATE TABLE t_cart (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    goods_id    BIGINT NOT NULL COMMENT '商品ID',
    goods_name  VARCHAR(200) DEFAULT '' COMMENT '商品名称',
    goods_img   VARCHAR(500) DEFAULT '' COMMENT '商品图片',
    goods_price DECIMAL(10,2) DEFAULT 0 COMMENT '商品价格',
    quantity    INT DEFAULT 1 COMMENT '购买数量',
    checked     TINYINT DEFAULT 1 COMMENT '是否选中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 8. 收货地址表
DROP TABLE IF EXISTS t_address;
CREATE TABLE t_address (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT NOT NULL COMMENT '用户ID',
    receiver_name  VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    province       VARCHAR(50) DEFAULT '' COMMENT '省份',
    city           VARCHAR(50) DEFAULT '' COMMENT '城市',
    district       VARCHAR(50) DEFAULT '' COMMENT '区县',
    detail_address VARCHAR(200) DEFAULT '' COMMENT '详细地址',
    is_default     TINYINT DEFAULT 0 COMMENT '是否默认地址',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 9. 收藏表
DROP TABLE IF EXISTS t_favorite;
CREATE TABLE t_favorite (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    goods_id    BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_goods (user_id, goods_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 10. 浏览历史表
DROP TABLE IF EXISTS t_browse_history;
CREATE TABLE t_browse_history (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    goods_id    BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览历史表';

-- 11. 评价表
DROP TABLE IF EXISTS t_review;
CREATE TABLE t_review (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    order_no    VARCHAR(50) DEFAULT '' COMMENT '订单号',
    goods_id    BIGINT NOT NULL COMMENT '商品ID',
    rating      INT DEFAULT 5 COMMENT '评分1-5',
    content     VARCHAR(500) DEFAULT '' COMMENT '评价内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_goods_id (goods_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 12. 消息通知表
DROP TABLE IF EXISTS t_notification;
CREATE TABLE t_notification (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    title       VARCHAR(100) DEFAULT '' COMMENT '标题',
    content     VARCHAR(500) DEFAULT '' COMMENT '内容',
    type        VARCHAR(20) DEFAULT 'system' COMMENT '类型',
    is_read     TINYINT DEFAULT 0 COMMENT '是否已读',
    ref_id      VARCHAR(50) DEFAULT '' COMMENT '关联ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 13. 轮播图表
DROP TABLE IF EXISTS t_banner;
CREATE TABLE t_banner (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(100) DEFAULT '' COMMENT '标题',
    image_url   VARCHAR(500) DEFAULT '' COMMENT '图片URL',
    link_url    VARCHAR(500) DEFAULT '' COMMENT '跳转链接',
    sort_order  INT DEFAULT 0 COMMENT '排序',
    status      TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 14. 优惠券表
DROP TABLE IF EXISTS t_coupon;
CREATE TABLE t_coupon (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(100) DEFAULT '' COMMENT '优惠券名称',
    type           INT DEFAULT 1 COMMENT '类型:1满减,2折扣',
    value          DECIMAL(10,2) DEFAULT 0 COMMENT '面值/折扣率',
    min_amount     DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费',
    total_count    INT DEFAULT 0 COMMENT '总数量',
    claimed_count  INT DEFAULT 0 COMMENT '已领取数量',
    start_time     DATETIME COMMENT '开始时间',
    end_time       DATETIME COMMENT '结束时间',
    status         TINYINT DEFAULT 1 COMMENT '状态',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 15. 用户优惠券表
DROP TABLE IF EXISTS t_user_coupon;
CREATE TABLE t_user_coupon (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT NOT NULL COMMENT '用户ID',
    coupon_id    BIGINT NOT NULL COMMENT '优惠券ID',
    coupon_name  VARCHAR(100) DEFAULT '' COMMENT '优惠券名称',
    type         INT DEFAULT 1 COMMENT '类型',
    value        DECIMAL(10,2) DEFAULT 0 COMMENT '面值',
    min_amount   DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费',
    status       TINYINT DEFAULT 0 COMMENT '状态:0未使用,1已使用,2已过期',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    use_time     DATETIME COMMENT '使用时间',
    INDEX idx_user_id (user_id),
    INDEX idx_coupon_id (coupon_id),
    UNIQUE KEY uk_user_coupon (user_id, coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 16. 用户积分表
DROP TABLE IF EXISTS t_user_points;
CREATE TABLE t_user_points (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    points       INT DEFAULT 0 COMMENT '积分余额',
    total_earned INT DEFAULT 0 COMMENT '累计获得',
    total_used   INT DEFAULT 0 COMMENT '累计使用',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';

-- 17. 积分日志表
DROP TABLE IF EXISTS t_points_log;
CREATE TABLE t_points_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    points      INT DEFAULT 0 COMMENT '积分变动',
    type        VARCHAR(20) DEFAULT '' COMMENT '类型:earn/use',
    description VARCHAR(200) DEFAULT '' COMMENT '描述',
    order_no    VARCHAR(50) DEFAULT '' COMMENT '关联订单号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分日志表';

-- 18. 退款表
DROP TABLE IF EXISTS t_refund;
CREATE TABLE t_refund (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    order_no    VARCHAR(50) NOT NULL COMMENT '订单号',
    type        INT DEFAULT 1 COMMENT '类型:1仅退款,2退货退款',
    amount      DECIMAL(10,2) DEFAULT 0 COMMENT '退款金额',
    reason      VARCHAR(500) DEFAULT '' COMMENT '退款原因',
    status      TINYINT DEFAULT 0 COMMENT '状态:0待审核,1已同意,2已拒绝,3已完成',
    admin_note  VARCHAR(500) DEFAULT '' COMMENT '管理员备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款表';

-- 19. 会员等级表
DROP TABLE IF EXISTS t_member_level;
CREATE TABLE t_member_level (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(50) NOT NULL COMMENT '等级名称',
    level           INT NOT NULL COMMENT '等级值',
    required_points INT DEFAULT 0 COMMENT '所需积分',
    discount        DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣率',
    icon            VARCHAR(50) DEFAULT '' COMMENT '等级图标'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- 20. 操作日志表
DROP TABLE IF EXISTS t_operation_log;
CREATE TABLE t_operation_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT COMMENT '操作人ID',
    username    VARCHAR(50) DEFAULT '' COMMENT '操作人用户名',
    operation   VARCHAR(100) DEFAULT '' COMMENT '操作类型',
    method      VARCHAR(50) DEFAULT '' COMMENT '请求方法',
    url         VARCHAR(200) DEFAULT '' COMMENT '请求URL',
    params      TEXT COMMENT '请求参数',
    ip          VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
    result      TINYINT DEFAULT 1 COMMENT '结果:0失败,1成功',
    cost_time   BIGINT DEFAULT 0 COMMENT '耗时ms',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- AI会话表
DROP TABLE IF EXISTS t_ai_conversation;
CREATE TABLE t_ai_conversation (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    title       VARCHAR(100) DEFAULT '新对话' COMMENT '会话标题',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话表';

-- AI消息表
DROP TABLE IF EXISTS t_ai_message;
CREATE TABLE t_ai_message (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    role            VARCHAR(20) NOT NULL COMMENT '角色: user/assistant',
    content         TEXT NOT NULL COMMENT '消息内容',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conv_id (conversation_id),
    INDEX idx_conv_time (conversation_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息表';

-- ========================================
-- 测试数据
-- ========================================

-- 管理员账号: admin / admin123
INSERT INTO t_user (username, password, nickname, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 1, 0);

-- 普通用户: user1 / 123456, user2 / 123456
INSERT INTO t_user (username, password, nickname, role, status) VALUES
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张三', 0, 0),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李四', 0, 0);

-- 测试商品（使用本地图片）
INSERT INTO t_goods (goods_name, goods_title, goods_img, goods_price, goods_stock, goods_desc, status) VALUES
('iPhone 15 Pro', 'Apple iPhone 15 Pro 256GB 钛金属设计', '/uploads/products/product_1.jpg', 8999.00, 100, '苹果最新旗舰手机，A17 Pro芯片，钛金属设计，4800万像素主摄', 1),
('MacBook Pro 14', 'Apple MacBook Pro 14寸 M3 Pro芯片', '/uploads/products/product_2.jpg', 16999.00, 50, '专业级笔记本电脑，Liquid Retina XDR显示屏', 1),
('AirPods Pro 2', 'Apple AirPods Pro 第二代 USB-C', '/uploads/products/product_3.jpg', 1899.00, 200, '主动降噪无线耳机，自适应音频，个性化空间音频', 1),
('iPad Air', 'Apple iPad Air M1芯片 256GB', '/uploads/products/product_4.jpg', 5499.00, 80, '轻薄平板电脑，10.9英寸Liquid Retina显示屏', 1),
('小米14 Ultra', '小米14 Ultra 骁龙8 Gen3 徕卡影像', '/uploads/products/product_5.jpg', 6499.00, 150, '小米影像旗舰，徕卡光学Summilux镜头', 1);

-- 秒杀商品（当前时间之后，方便测试）
INSERT INTO t_seckill_goods (goods_id, seckill_price, stock_count, start_time, end_time, status) VALUES
(1, 5999.00, 50, '2026-06-25 00:00:00', '2026-12-31 23:59:59', 1),
(2, 11999.00, 20, '2026-06-25 00:00:00', '2026-12-31 23:59:59', 1),
(3, 999.00, 100, '2026-06-25 00:00:00', '2026-12-31 23:59:59', 1),
(4, 2999.00, 30, '2026-06-25 00:00:00', '2026-12-31 23:59:59', 1);

-- 测试优惠券
INSERT INTO t_coupon (name, type, value, min_amount, total_count, claimed_count, start_time, end_time, status) VALUES
('新人满100减20', 1, 20.00, 100.00, 1000, 0, '2026-01-01', '2026-12-31', 1),
('满200减50', 1, 50.00, 200.00, 500, 0, '2026-01-01', '2026-12-31', 1),
('全场9折券', 2, 0.90, 0.00, 2000, 0, '2026-01-01', '2026-12-31', 1);

-- 会员等级
INSERT INTO t_member_level (name, level, required_points, discount, icon) VALUES
('普通会员', 0, 0, 1.00, '🌱'),
('银卡会员', 1, 1000, 0.98, '🥈'),
('金卡会员', 2, 5000, 0.95, '🥇'),
('钻石会员', 3, 20000, 0.90, '💎');
