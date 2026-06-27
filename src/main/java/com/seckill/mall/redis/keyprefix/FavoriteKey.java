package com.seckill.mall.redis.keyprefix;

/**
 * 收藏模块Redis Key前缀
 */
public class FavoriteKey extends BaseKeyPrefix {

    /** 用户收藏列表缓存，过期时间5分钟 */
    public static final FavoriteKey FAVORITE_LIST = new FavoriteKey("favorite_list", 300);

    /** 用户单品收藏状态缓存，过期时间5分钟（用于商品详情页快速判断） */
    public static final FavoriteKey FAVORITE_CHECK = new FavoriteKey("favorite_check", 300);

    private FavoriteKey(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }
}
