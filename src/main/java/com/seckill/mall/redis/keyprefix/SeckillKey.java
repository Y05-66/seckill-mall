package com.seckill.mall.redis.keyprefix;

/**
 * 秒杀模块Redis Key前缀
 */
public class SeckillKey extends BaseKeyPrefix {

    /** 秒杀库存（永不过期，系统启动时初始化） */
    public static final SeckillKey STOCK = new SeckillKey("stock", 0);

    /** 商品售罄标记（永不过期，库存为0时设置） */
    public static final SeckillKey GOODS_OVER = new SeckillKey("goods_over", 0);

    /** 秒杀用户标记（过期时间1小时，用于记录用户是否已参与秒杀） */
    public static final SeckillKey SECKILL_USER = new SeckillKey("seckill_user", 3600);

    /** 秒杀请求用户级限流计数器（过期时间1秒） */
    public static final SeckillKey RATE_LIMIT = new SeckillKey("seckill_rate", 1);

    private SeckillKey(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }
}
