package com.seckill.mall.redis.keyprefix;

/**
 * Redis Key前缀抽象基类
 * <p>
 * 实现KeyPrefix接口，提供前缀和过期时间的基础存储。
 * 子类通过静态常量定义具体的Key前缀实例。
 * </p>
 */
public abstract class BaseKeyPrefix implements KeyPrefix {

    /** Key前缀字符串 */
    private final String prefix;

    /** 过期时间（秒），0表示永不过期 */
    private final int expireSeconds;

    protected BaseKeyPrefix(String prefix, int expireSeconds) {
        this.prefix = prefix;
        this.expireSeconds = expireSeconds;
    }

    /** 构造永不过期的Key前缀 */
    protected BaseKeyPrefix(String prefix) {
        this(prefix, 0);
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public int expireSeconds() {
        return this.expireSeconds;
    }
}
