package com.seckill.mall.mq;

import com.seckill.mall.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 秒杀消息生产者
 * <p>
 * 负责将秒杀请求封装为消息发送到RabbitMQ的秒杀交换机。
 * 使用DirectExchange，通过RoutingKey路由到秒杀队列。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送秒杀消息到MQ
     *
     * @param message 秒杀消息体（包含userId和seckillGoodsId）
     */
    public void sendSeckillMessage(SeckillMessage message) {
        log.info("发送秒杀消息: userId={}, seckillGoodsId={}", message.getUserId(), message.getSeckillGoodsId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SECKILL_EXCHANGE,
                RabbitMQConfig.SECKILL_ROUTING_KEY,
                message);
    }
}
