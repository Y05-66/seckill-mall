package com.seckill.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.Constants;
import com.seckill.mall.entity.SeckillOrder;
import com.seckill.mall.entity.SeckillSuccess;
import com.seckill.mall.mapper.SeckillGoodsMapper;
import com.seckill.mall.mapper.SeckillOrderMapper;
import com.seckill.mall.mapper.SeckillSuccessMapper;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.SeckillKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单取消服务
 * <p>
 * 从 OrderTimeoutScheduler 中提取，解决 Spring @Transactional 自调用失效问题。
 * Spring AOP 通过代理拦截方法调用，同类内部调用（this.method()）绕过代理，
 * 导致 @Transactional 注解不生效。提取到独立的 Service Bean 后，调用走代理，事务正常生效。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancelService {

    private final SeckillOrderMapper seckillOrderMapper;
    private final SeckillGoodsMapper seckillGoodsMapper;
    private final SeckillSuccessMapper seckillSuccessMapper;
    private final RedisService redisService;

    /**
     * 取消单个超时订单并回滚库存（在事务中执行）
     * 使用条件更新保证幂等性：只有状态仍为未支付的订单才会被取消
     *
     * @param order 要取消的订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelSingleOrder(SeckillOrder order) {
        // 1. 条件更新订单状态为已超时（仅未支付状态才能更新，保证幂等性）
        order.setStatus(Constants.ORDER_TIMEOUT);
        int affected = seckillOrderMapper.update(order,
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getId, order.getId())
                        .eq(SeckillOrder::getStatus, Constants.ORDER_UNPAID));
        if (affected == 0) {
            // 状态已变更（可能被用户取消或其他实例处理），跳过
            log.info("订单状态已变更，跳过取消: orderNo={}", order.getOrderNo());
            return;
        }

        // 2. 原子回滚数据库库存
        seckillGoodsMapper.increaseStock(order.getSeckillGoodsId());

        // 3. 同步回滚Redis库存和售罄标记
        try {
            redisService.incr(SeckillKey.STOCK, String.valueOf(order.getSeckillGoodsId()));
            redisService.delete(SeckillKey.GOODS_OVER, String.valueOf(order.getSeckillGoodsId()));
        } catch (Exception e) {
            log.error("Redis库存回滚失败: seckillGoodsId={}, error={}", order.getSeckillGoodsId(), e.getMessage());
        }

        // 4. 删除秒杀成功记录（允许用户再次参与秒杀）
        seckillSuccessMapper.delete(
                new LambdaQueryWrapper<SeckillSuccess>()
                        .eq(SeckillSuccess::getUserId, order.getUserId())
                        .eq(SeckillSuccess::getSeckillGoodsId, order.getSeckillGoodsId()));

        // 5. 删除Redis用户秒杀标记（允许用户重新参与秒杀）
        try {
            redisService.delete(SeckillKey.SECKILL_USER, order.getUserId() + ":" + order.getSeckillGoodsId());
        } catch (Exception e) {
            log.error("删除用户秒杀标记失败: userId={}, seckillGoodsId={}, error={}",
                    order.getUserId(), order.getSeckillGoodsId(), e.getMessage());
        }

        log.info("超时订单已取消: orderNo={}, userId={}, seckillGoodsId={}",
                order.getOrderNo(), order.getUserId(), order.getSeckillGoodsId());
    }
}
