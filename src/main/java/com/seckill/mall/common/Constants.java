package com.seckill.mall.common;

/**
 * 系统常量定义类
 * <p>
 * 集中管理整个系统中使用的常量值，包括：
 * <ul>
 *   <li>JWT Token相关常量</li>
 *   <li>秒杀活动状态常量</li>
 *   <li>订单状态常量</li>
 *   <li>Redis缓存Key前缀</li>
 *   <li>限流配置常量</li>
 * </ul>
 * </p>
 */
public class Constants {

    // ==================== Token相关 ====================

    /** HTTP请求头中Token的Key名 */
    public static final String TOKEN_HEADER = "Authorization";

    /** Token前缀，Bearer + 空格，客户端传递Token时需要携带此前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    // ==================== 秒杀活动状态 ====================

    /** 秒杀未开始（当前时间早于秒杀开始时间） */
    public static final int SECKILL_NOT_START = 0;

    /** 秒杀进行中（当前时间在秒杀时间窗口内） */
    public static final int SECKILL_IN_PROGRESS = 1;

    /** 秒杀已结束（当前时间晚于秒杀结束时间） */
    public static final int SECKILL_ENDED = 2;

    // ==================== 订单状态 ====================

    /** 订单未支付（刚创建的订单） */
    public static final int ORDER_UNPAID = 0;

    /** 订单已支付 */
    public static final int ORDER_PAID = 1;

    /** 订单已取消（用户主动取消或超时未支付） */
    public static final int ORDER_CANCELLED = 2;

    /** 订单超时未支付（系统自动取消） */
    public static final int ORDER_TIMEOUT = 3;

    /** 订单已退款 */
    public static final int ORDER_REFUNDED = 4;

    // ==================== Redis缓存相关 ====================

    /** 秒杀库存初始化标记的Redis Key前缀，用于应用启动时判断是否已初始化库存 */
    public static final String SECKILL_STOCK_INIT = "seckill:stock:init:";

    // ==================== 限流相关 ====================

    /** 秒杀接口限流QPS（每秒最大请求数），使用Guava RateLimiter实现 */
    public static final int SECKILL_LIMIT_QPS = 500;

    /** 私有构造方法，防止实例化工具类 */
    private Constants() {}
}
