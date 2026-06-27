package com.seckill.mall.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.Constants;
import com.seckill.mall.entity.SeckillOrder;
import com.seckill.mall.mapper.SeckillOrderMapper;
import com.seckill.mall.service.OrderCancelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单超时自动取消调度器
 * <p>
 * 每分钟扫描一次超时未支付的订单（创建超过15分钟），自动取消并回滚库存。
 * 使用分布式锁防止多实例重复处理同一订单。
 * 事务逻辑委托给 OrderCancelService（独立 Bean），避免 @Transactional 自调用失效。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final SeckillOrderMapper seckillOrderMapper;
    private final OrderCancelService orderCancelService;
    private final RedissonClient redissonClient;

    /** 订单超时时间（分钟） */
    private static final int ORDER_TIMEOUT_MINUTES = 15;

    /**
     * 每分钟扫描超时未支付订单（带分布式锁，防止多实例重复处理）
     */
    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutOrders() {
        RLock lock = redissonClient.getLock("seckill:timeout:scheduler");
        try {
            // 尝试获取锁，等待0秒，持有30秒自动释放
            if (!lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        try {
            doCancelTimeoutOrders();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void doCancelTimeoutOrders() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);

        // 查询超时订单（限制最多200条，避免极端情况OOM）
        List<SeckillOrder> timeoutOrders = seckillOrderMapper.selectList(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getStatus, Constants.ORDER_UNPAID)
                        .lt(SeckillOrder::getCreateTime, timeout)
                        .last("LIMIT 200"));

        if (timeoutOrders.isEmpty()) {
            return;
        }

        log.info("发现{}个超时订单，开始处理...", timeoutOrders.size());

        int totalCancelled = 0;
        for (SeckillOrder order : timeoutOrders) {
            try {
                orderCancelService.cancelSingleOrder(order);
                totalCancelled++;
            } catch (Exception e) {
                log.error("取消超时订单失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage());
            }
        }

        log.info("超时订单处理完成，共处理{}个", totalCancelled);
    }
}
