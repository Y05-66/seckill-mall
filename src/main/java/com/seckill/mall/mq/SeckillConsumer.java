package com.seckill.mall.mq;

import com.rabbitmq.client.Channel;
import com.seckill.mall.config.RabbitMQConfig;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.SeckillKey;
import com.seckill.mall.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 秒杀消息消费者
 * <p>
 * 监听秒杀队列，接收秒杀请求消息并调用SeckillService.createOrder()异步创建订单。
 * 支持重试机制：消费失败时最多重试3次（每次间隔10秒），超过后进入死信队列。
 * </p>
 * <p>
 * 重试策略（使用自定义 x-retry-count header，不依赖 RabbitMQ 内部 x-death）：
 * <ol>
 *   <li>消费成功 → ACK</li>
 *   <li>消费失败 + retryCount < 3 → 手动发布到重试队列（带递增的 x-retry-count），ACK原消息</li>
 *   <li>消费失败 + retryCount >= 3 → 发送到死信队列，ACK原消息，回滚Redis库存</li>
 * </ol>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillConsumer {

    private final SeckillService seckillService;
    private final RedisService redisService;
    private final RabbitTemplate rabbitTemplate;

    /** 自定义重试次数 header 名称 */
    private static final String RETRY_COUNT_HEADER = "x-retry-count";

    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void receiveSeckillMessage(SeckillMessage message, Channel channel, Message amqpMessage) throws IOException {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        log.info("收到秒杀消息: userId={}, seckillGoodsId={}, deliveryTag={}",
                message.getUserId(), message.getSeckillGoodsId(), deliveryTag);

        try {
            seckillService.createOrder(message.getUserId(), message.getSeckillGoodsId());
            // 消费成功，ACK确认
            channel.basicAck(deliveryTag, false);
            log.info("秒杀消息消费成功: userId={}, seckillGoodsId={}", message.getUserId(), message.getSeckillGoodsId());
        } catch (Exception e) {
            log.error("秒杀下单失败: userId={}, seckillGoodsId={}, error={}",
                    message.getUserId(), message.getSeckillGoodsId(), e.getMessage());

            // 获取当前重试次数（从自定义 header 中读取）
            long retryCount = getRetryCount(amqpMessage);

            if (retryCount < RabbitMQConfig.MAX_RETRY_COUNT) {
                // 还有重试机会，先发送到重试队列，再ACK原消息（防止消息丢失）
                log.warn("消息将进入重试队列，当前重试次数: {}/{}", retryCount, RabbitMQConfig.MAX_RETRY_COUNT);
                sendToRetryQueue(message, retryCount + 1);
                channel.basicAck(deliveryTag, false);
            } else {
                // 重试次数用尽，先回滚Redis库存，再发送到死信队列，最后ACK
                log.error("重试次数用尽，消息进入死信队列: userId={}, seckillGoodsId={}",
                        message.getUserId(), message.getSeckillGoodsId());

                // 回滚Redis库存（DECR后失败需要INCR回补）
                rollbackRedisStock(message.getSeckillGoodsId());

                // 发送到死信队列
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DLX_EXCHANGE,
                        RabbitMQConfig.DLX_ROUTING_KEY,
                        message);

                // 最后ACK原消息
                channel.basicAck(deliveryTag, false);
            }
        }
    }

    /**
     * 手动发送消息到重试队列，携带递增的重试计数
     */
    private void sendToRetryQueue(SeckillMessage message, long retryCount) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.RETRY_EXCHANGE,
                RabbitMQConfig.RETRY_ROUTING_KEY,
                message,
                msg -> {
                    msg.getMessageProperties().getHeaders().put(RETRY_COUNT_HEADER, retryCount);
                    return msg;
                });
    }

    /**
     * 从自定义 header 中获取重试次数
     */
    private long getRetryCount(Message message) {
        try {
            Object count = message.getMessageProperties().getHeaders().get(RETRY_COUNT_HEADER);
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
        } catch (Exception e) {
            log.warn("解析重试次数失败: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * 回滚Redis库存
     */
    private void rollbackRedisStock(Long seckillGoodsId) {
        try {
            Long stock = redisService.incr(SeckillKey.STOCK, String.valueOf(seckillGoodsId));
            // 只有库存大于0时才删除售罄标记，防止误删真正售罄的商品
            if (stock != null && stock > 0) {
                redisService.delete(SeckillKey.GOODS_OVER, String.valueOf(seckillGoodsId));
            }
            log.info("Redis库存已回滚: seckillGoodsId={}, 当前库存={}", seckillGoodsId, stock);
        } catch (Exception e) {
            log.error("Redis库存回滚失败: seckillGoodsId={}, error={}", seckillGoodsId, e.getMessage());
        }
    }
}
