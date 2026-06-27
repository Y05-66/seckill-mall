package com.seckill.mall.redis.keyprefix;

/**
 * 验证码模块Redis Key前缀
 */
public class CaptchaKey extends BaseKeyPrefix {

    /** 验证码答案缓存，过期时间5分钟 */
    public static final CaptchaKey CAPTCHA = new CaptchaKey("captcha", 300);

    /** 用户级限流计数器，过期时间1秒（滑动窗口） */
    public static final CaptchaKey RATE_LIMIT = new CaptchaKey("rate_limit", 1);

    private CaptchaKey(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }
}
