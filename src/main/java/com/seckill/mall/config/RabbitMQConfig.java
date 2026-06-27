package com.seckill.mall.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 消息队列配置类
 * <p>
 * 配置秒杀系统的异步下单消息队列，采用Direct交换机模式，支持死信队列和重试机制。
 * </p>
 * <p>
 * 消息流转路径：
 * <ol>
 *   <li>秒杀请求 → Producer发送到 seckill.exchange → seckill.queue</li>
 *   <li>消费失败 → 消息进入 seckill.retry.exchange → seckill.retry.queue（TTL 10秒）</li>
 *   <li>重试队列消息到期 → 回到 seckill.exchange → seckill.queue 重新消费</li>
 *   <li>重试3次仍失败 → 进入 seckill.dlx.exchange → seckill.dlx.queue（人工处理）</li>
 * </ol>
 * </p>
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 主队列 ====================
    public static final String SECKILL_QUEUE = "seckill.queue";
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    public static final String SECKILL_ROUTING_KEY = "seckill.routing.key";

    // ==================== 重试队列 ====================
    public static final String RETRY_QUEUE = "seckill.retry.queue";
    public static final String RETRY_EXCHANGE = "seckill.retry.exchange";
    public static final String RETRY_ROUTING_KEY = "seckill.retry.routing.key";

    // ==================== 死信队列 ====================
    public static final String DLX_QUEUE = "seckill.dlx.queue";
    public static final String DLX_EXCHANGE = "seckill.dlx.exchange";
    public static final String DLX_ROUTING_KEY = "seckill.dlx.routing.key";

    /** 最大重试次数 */
    public static final int MAX_RETRY_COUNT = 3;

    // ==================== 主队列配置 ====================

    /**
     * 声明秒杀主队列
     * 配置死信交换机：消费失败（reject/nack）时消息路由到重试交换机
     */
    @Bean
    public Queue seckillQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", RETRY_EXCHANGE);
        args.put("x-dead-letter-routing-key", RETRY_ROUTING_KEY);
        return new Queue(SECKILL_QUEUE, true, false, false, args);
    }

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(SECKILL_EXCHANGE);
    }

    @Bean
    public Binding seckillBinding(Queue seckillQueue, DirectExchange seckillExchange) {
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with(SECKILL_ROUTING_KEY);
    }

    // ==================== 重试队列配置 ====================

    /**
     * 声明重试队列
     * TTL 10秒后消息到期，自动路由回主交换机重新消费
     */
    @Bean
    public Queue retryQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 10000);
        args.put("x-dead-letter-exchange", SECKILL_EXCHANGE);
        args.put("x-dead-letter-routing-key", SECKILL_ROUTING_KEY);
        return new Queue(RETRY_QUEUE, true, false, false, args);
    }

    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(RETRY_EXCHANGE);
    }

    @Bean
    public Binding retryBinding(Queue retryQueue, DirectExchange retryExchange) {
        return BindingBuilder.bind(retryQueue).to(retryExchange).with(RETRY_ROUTING_KEY);
    }

    // ==================== 死信队列配置（最终失败的消息） ====================

    @Bean
    public Queue dlxQueue() {
        return new Queue(DLX_QUEUE, true);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Binding dlxBinding(Queue dlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(dlxQueue).to(dlxExchange).with(DLX_ROUTING_KEY);
    }

    // ==================== 通用配置 ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
