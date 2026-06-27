package com.seckill.mall.redis.keyprefix;

/**
 * 商品模块Redis Key前缀
 * GOODS_LIST 和 GOODS_DETAIL 已在 GoodsServiceImpl 中使用（Redis缓存 + Caffeine本地缓存双层）
 */
public class GoodsKey extends BaseKeyPrefix {

    /** 商品列表缓存，过期时间60秒 */
    public static final GoodsKey GOODS_LIST = new GoodsKey("goods_list", 60);

    /** 商品详情缓存，过期时间60秒 */
    public static final GoodsKey GOODS_DETAIL = new GoodsKey("goods_detail", 60);

    private GoodsKey(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }
}
