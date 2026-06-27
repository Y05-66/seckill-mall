package com.seckill.mall.redis.keyprefix;

/**
 * 轮播图模块Redis Key前缀
 */
public class BannerKey extends BaseKeyPrefix {

    /** 轮播图列表缓存，过期时间10分钟 */
    public static final BannerKey BANNER_LIST = new BannerKey("banner_list", 600);

    private BannerKey(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }
}
