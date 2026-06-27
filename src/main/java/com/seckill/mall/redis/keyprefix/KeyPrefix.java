package com.seckill.mall.redis.keyprefix;

/**
 * Redis Key前缀接口
 * <p>
 * 定义Redis Key的命名空间前缀和过期时间。
 * 通过不同的实现类（UserKey、GoodsKey、SeckillKey等）为不同业务模块
 * 提供独立的Key命名空间，避免Key冲突。
 * </p>
 */
public interface KeyPrefix {

    /**
     * 获取Key前缀（作为命名空间）
     *
     * @return 前缀字符串，如 "token"、"stock"、"goods_list"
     */
    String getPrefix();

    /**
     * 获取过期时间（秒）
     *
     * @return 过期秒数，0表示永不过期
     */
    int expireSeconds();
}
