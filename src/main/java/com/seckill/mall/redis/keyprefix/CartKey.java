package com.seckill.mall.redis.keyprefix;

/**
 * 购物车模块Redis Key前缀
 */
public class CartKey extends BaseKeyPrefix {

    /** 用户购物车列表缓存，过期时间5分钟 */
    public static final CartKey CART_LIST = new CartKey("cart_list", 300);

    /** 用户购物车数量缓存，过期时间5分钟 */
    public static final CartKey CART_COUNT = new CartKey("cart_count", 300);

    private CartKey(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }
}
