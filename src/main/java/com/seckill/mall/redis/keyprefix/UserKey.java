package com.seckill.mall.redis.keyprefix;

/**
 * 用户模块Redis Key前缀
 * TODO [功能缺失] TOKEN 和 USER_INFO 前缀已定义但代码中从未使用。
 *   JWT Token 是自包含的无需Redis存储，但 USER_INFO 可用于缓存用户信息减少数据库查询。
 */
public class UserKey extends BaseKeyPrefix {

    /** 用户Token缓存，过期时间24小时 */
    public static final UserKey TOKEN = new UserKey("token", 3600 * 24);

    /** 用户信息缓存，过期时间1小时 */
    public static final UserKey USER_INFO = new UserKey("user", 3600);

    /** Token黑名单（登出后失效），过期时间25小时（比Token多1小时） */
    public static final UserKey TOKEN_BLACKLIST = new UserKey("token_blacklist", 3600 * 25);

    private UserKey(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }
}
