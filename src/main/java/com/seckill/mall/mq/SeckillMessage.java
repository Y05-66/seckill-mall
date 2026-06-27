package com.seckill.mall.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀消息体
 * <p>
 * 通过RabbitMQ传输的秒杀请求消息DTO。
 * 包含用户ID和秒杀商品ID，消费者收到消息后据此创建订单。
 * 实现Serializable接口以支持JSON序列化。
 * </p>
 */
@Data
public class SeckillMessage implements Serializable {

    /** 秒杀用户ID */
    private Long userId;

    /** 秒杀商品ID */
    private Long seckillGoodsId;

    public SeckillMessage() {}

    public SeckillMessage(Long userId, Long seckillGoodsId) {
        this.userId = userId;
        this.seckillGoodsId = seckillGoodsId;
    }
}
