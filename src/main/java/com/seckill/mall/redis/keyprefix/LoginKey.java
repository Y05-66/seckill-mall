package com.seckill.mall.redis.keyprefix;

/**
 * 登录模块Redis Key前缀
 */
public class LoginKey extends BaseKeyPrefix {

    /** 登录限流计数器，过期时间1分钟（每IP每分钟最多5次尝试） */
    public static final LoginKey RATE_LIMIT = new LoginKey("login_rate", 60);

    private LoginKey(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }
}
