package com.seckill.mall.redis.keyprefix;

/**
 * 优惠券模块Redis Key前缀
 */
public class CouponKey extends BaseKeyPrefix {

    /** 可用优惠券列表缓存，过期时间5分钟 */
    public static final CouponKey AVAILABLE_LIST = new CouponKey("coupon_available", 300);

    /** 用户优惠券列表缓存，过期时间2分钟 */
    public static final CouponKey USER_LIST = new CouponKey("coupon_user", 120);

    private CouponKey(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }
}
