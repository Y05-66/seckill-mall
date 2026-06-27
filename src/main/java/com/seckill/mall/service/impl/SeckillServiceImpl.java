package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.Constants;
import com.seckill.mall.common.ResultCode;
import com.seckill.mall.entity.SeckillGoods;
import com.seckill.mall.entity.SeckillOrder;
import com.seckill.mall.entity.SeckillSuccess;
import com.seckill.mall.entity.OrderDetail;
import com.seckill.mall.entity.Goods;
import com.seckill.mall.mapper.SeckillGoodsMapper;
import com.seckill.mall.mapper.SeckillOrderMapper;
import com.seckill.mall.mapper.SeckillSuccessMapper;
import com.seckill.mall.mapper.OrderDetailMapper;
import com.seckill.mall.mapper.GoodsMapper;
import com.seckill.mall.mq.SeckillMessage;
import com.seckill.mall.mq.SeckillProducer;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.SeckillKey;
import com.seckill.mall.service.SeckillService;
import com.seckill.mall.util.UUIDUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀业务服务实现类
 * <p>
 * 核心秒杀流程：Redis预扣库存 → MQ异步下单 → 数据库乐观锁扣库存
 * 采用多层防护机制确保高并发下的数据一致性：
 * 1. Redis售罄标记 - 快速拦截已售罄商品的请求
 * 2. Redis原子DECR - 高并发库存预扣减
 * 3. 数据库乐观锁 - 最终库存一致性保障
 * 4. 重复购买校验 - 防止同一用户重复下单
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final RedisService redisService;
    private final SeckillGoodsMapper seckillGoodsMapper;
    private final SeckillOrderMapper seckillOrderMapper;
    private final SeckillSuccessMapper seckillSuccessMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final GoodsMapper goodsMapper;
    private final SeckillProducer seckillProducer;
    private final RedissonClient redissonClient;

    /**
     * 秒杀入口
     * <p>
     * 处理流程：
     * 1. 检查Redis售罄标记，已售罄直接拒绝
     * 2. 查询数据库判断是否重复购买
     * 3. Redis原子DECR预扣库存，库存<0则标记售罄
     * 4. 发送MQ消息，异步创建订单
     * </p>
     */
    @Override
    public String doSeckill(Long userId, Long seckillGoodsId) {
        // 1. 售罄标记检查：避免已售罄商品的请求继续往下走
        Boolean isOver = redisService.get(SeckillKey.GOODS_OVER, String.valueOf(seckillGoodsId));
        if (Boolean.TRUE.equals(isOver)) {
            throw new BusinessException(ResultCode.SECKILL_STOCK_EMPTY);
        }

        // 2. 重复购买校验：先查Redis快速拦截，未命中再查数据库
        String userKey = userId + ":" + seckillGoodsId;
        Boolean alreadySeckilled = redisService.get(SeckillKey.SECKILL_USER, userKey);
        if (Boolean.TRUE.equals(alreadySeckilled)) {
            throw new BusinessException(ResultCode.SECKILL_REPEAT);
        }

        // Redis未命中，查数据库（t_seckill_success 有唯一约束 uk_user_goods）
        SeckillSuccess existSuccess = seckillSuccessMapper.selectOne(
                new LambdaQueryWrapper<SeckillSuccess>()
                        .eq(SeckillSuccess::getUserId, userId)
                        .eq(SeckillSuccess::getSeckillGoodsId, seckillGoodsId));
        if (existSuccess != null) {
            // 回写Redis，后续请求直接走Redis拦截
            redisService.set(SeckillKey.SECKILL_USER, userKey, true);
            throw new BusinessException(ResultCode.SECKILL_REPEAT);
        }

        // 3. Redis原子扣库存（DECR是原子操作，保证高并发安全）
        Long stock = redisService.decr(SeckillKey.STOCK, String.valueOf(seckillGoodsId));
        if (stock < 0) {
            // 库存不足，回滚Redis库存（DECR已执行，需INCR恢复）
            redisService.incr(SeckillKey.STOCK, String.valueOf(seckillGoodsId));
            // 设置售罄标记，后续请求直接拦截
            redisService.set(SeckillKey.GOODS_OVER, String.valueOf(seckillGoodsId), true);
            throw new BusinessException(ResultCode.SECKILL_STOCK_EMPTY);
        }

        // 4. 立即设置用户标记，防止同一用户并发请求重复扣库存
        //    createOrder() 中会再次确认并覆盖（带DB持久化），这里仅做快速拦截
        redisService.set(SeckillKey.SECKILL_USER, userKey, true);

        // 5. 发送MQ消息，异步创建订单（削峰填谷，保护数据库）
        SeckillMessage message = new SeckillMessage(userId, seckillGoodsId);
        seckillProducer.sendSeckillMessage(message);

        log.info("秒杀请求已入队: userId={}, seckillGoodsId={}, 剩余库存={}", userId, seckillGoodsId, stock);
        return null; // 返回null表示排队中，前端轮询查询结果
    }

    /**
     * 创建订单（由MQ消费者调用）
     * <p>
     * 在事务中执行：
     * 1. 再次校验重复购买（防御性编程）
     * 2. 数据库乐观锁扣减库存（WHERE stock_count > 0）
     * 3. 创建秒杀订单和订单明细
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Long userId, Long seckillGoodsId) {
        // 1. 再次校验是否重复购买（MQ可能重复消费，需要防御）
        SeckillSuccess existSuccess = seckillSuccessMapper.selectOne(
                new LambdaQueryWrapper<SeckillSuccess>()
                        .eq(SeckillSuccess::getUserId, userId)
                        .eq(SeckillSuccess::getSeckillGoodsId, seckillGoodsId));
        if (existSuccess != null) {
            log.warn("重复下单，忽略并回滚Redis库存: userId={}, seckillGoodsId={}", userId, seckillGoodsId);
            // 回滚 doSeckill() 中已扣减的 Redis 库存，防止库存泄漏
            redisService.incr(SeckillKey.STOCK, String.valueOf(seckillGoodsId));
            return;
        }

        // 2. 校验秒杀商品和库存
        SeckillGoods seckillGoods = seckillGoodsMapper.selectById(seckillGoodsId);
        if (seckillGoods == null || seckillGoods.getStockCount() <= 0) {
            log.warn("库存不足，回滚Redis库存: seckillGoodsId={}", seckillGoodsId);
            redisService.incr(SeckillKey.STOCK, String.valueOf(seckillGoodsId));
            return;
        }

        // 3. 数据库乐观锁扣减库存（WHERE stock_count > 0 保证不会超卖）
        int affected = seckillGoodsMapper.reduceStock(seckillGoodsId);
        if (affected <= 0) {
            log.warn("数据库扣减库存失败，回滚Redis库存: seckillGoodsId={}", seckillGoodsId);
            // 回滚Redis库存（doSeckill中已DECR）
            redisService.incr(SeckillKey.STOCK, String.valueOf(seckillGoodsId));
            // 设置售罄标记
            redisService.set(SeckillKey.GOODS_OVER, String.valueOf(seckillGoodsId), true);
            return;
        }

        // 4. 生成唯一订单号
        String orderNo = UUIDUtils.orderNo();

        // 5. 创建秒杀订单
        SeckillOrder order = new SeckillOrder();
        order.setUserId(userId);
        order.setSeckillGoodsId(seckillGoodsId);
        order.setOrderNo(orderNo);
        order.setStatus(Constants.ORDER_UNPAID);
        order.setCreateTime(LocalDateTime.now());
        seckillOrderMapper.insert(order);

        // 6. 创建订单明细（商品快照）
        Goods goods = goodsMapper.selectById(seckillGoods.getGoodsId());
        OrderDetail detail = new OrderDetail();
        detail.setOrderNo(orderNo);
        detail.setGoodsId(seckillGoods.getGoodsId());
        detail.setGoodsName(goods != null ? goods.getGoodsName() : "");
        detail.setGoodsPrice(seckillGoods.getSeckillPrice());
        detail.setGoodsCount(1);
        detail.setCreateTime(LocalDateTime.now());
        orderDetailMapper.insert(detail);

        // 7. 记录秒杀成功（用于重复购买校验，t_seckill_success 有唯一约束 uk_user_goods）
        SeckillSuccess success = new SeckillSuccess();
        success.setUserId(userId);
        success.setSeckillGoodsId(seckillGoodsId);
        success.setOrderNo(orderNo);
        success.setCreateTime(LocalDateTime.now());
        seckillSuccessMapper.insert(success);

        // 8. 写入Redis用户标记，后续重复请求直接走Redis拦截（TTL 1小时）
        String userKey = userId + ":" + seckillGoodsId;
        redisService.set(SeckillKey.SECKILL_USER, userKey, true);

        log.info("秒杀订单创建成功: userId={}, orderNo={}", userId, orderNo);
    }

    /**
     * 查询秒杀结果（供前端轮询）
     *
     * @return 订单号（成功），"sold_out"（已售罄），null（排队中）
     */
    @Override
    public String getSeckillResult(Long userId, Long seckillGoodsId) {
        // 查询是否已生成订单（只查最新的未支付订单）
        SeckillOrder order = seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getUserId, userId)
                        .eq(SeckillOrder::getSeckillGoodsId, seckillGoodsId)
                        .eq(SeckillOrder::getStatus, Constants.ORDER_UNPAID)
                        .orderByDesc(SeckillOrder::getCreateTime)
                        .last("LIMIT 1"));
        if (order != null) {
            return order.getOrderNo();
        }

        // 检查是否已售罄
        Boolean isOver = redisService.get(SeckillKey.GOODS_OVER, String.valueOf(seckillGoodsId));
        if (Boolean.TRUE.equals(isOver)) {
            return "sold_out";
        }

        return null; // 还在排队
    }

    /**
     * 初始化秒杀库存到Redis
     * <p>
     * 系统启动时调用，将数据库中的秒杀库存预加载到Redis。
     * 使用exists判断避免覆盖已扣减的库存（防止重启导致库存重置）。
     * </p>
     */
    @Override
    public void initSeckillStock() {
        List<SeckillGoods> list = seckillGoodsMapper.selectList(null);
        for (SeckillGoods sg : list) {
            String key = String.valueOf(sg.getId());
            // 只在key不存在时设置（避免覆盖已扣减的库存）
            if (!redisService.exists(SeckillKey.STOCK, key)) {
                redisService.set(SeckillKey.STOCK, key, sg.getStockCount());
                log.info("初始化秒杀库存: seckillGoodsId={}, stock={}", sg.getId(), sg.getStockCount());
            }
        }
    }

    /**
     * 更新秒杀活动状态（管理员手动开始/结束秒杀）
     * <p>
     * 状态转换规则（严格单向流转，防止数据混乱）：
     * <ul>
     *   <li>未开始(0) -- 进行中(1)：手动启动秒杀，初始化Redis库存并清除售罄标记</li>
     *   <li>进行中(1) -- 已结束(2)：手动终止秒杀，设置Redis售罄标记阻止后续购买</li>
     * </ul>
     * 禁止的转换：已结束不能回退、不能跳过中间状态、不能手动设为未开始。
     * </p>
     * <p>
     * Redis同步策略：
     * <ul>
     *   <li>开始秒杀时：初始化Redis库存（仅在key不存在时设置，避免覆盖已扣减值）、清除售罄标记</li>
     *   <li>结束秒杀时：设置售罄标记（即使库存未耗尽也要阻止新购买，因为管理员已手动关闭活动）</li>
     * </ul>
     * Redis操作使用try-catch容错，避免Redis异常影响核心数据库事务。
     * </p>
     *
     * @param id     秒杀商品ID
     * @param status 目标状态（1=开始，2=结束）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillStatus(Long id, Integer status) {
        // 使用分布式锁防止管理员并发操作同一秒杀活动（带超时，防止死锁）
        RLock lock = redissonClient.getLock("seckill:status:" + id);
        try {
            if (!lock.tryLock(5, 30, java.util.concurrent.TimeUnit.SECONDS)) {
                throw new BusinessException("操作频繁，请稍后再试");
            }
            doUpdateSeckillStatus(id, status);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("操作被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void doUpdateSeckillStatus(Long id, Integer status) {
        //1. 查询秒杀商品并校验存在性
        SeckillGoods sg = seckillGoodsMapper.selectById(id);
        if (sg == null) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_NOT_FOUND);
        }

        int currentStatus = sg.getStatus();

        //2.状态转换合法性校验（严格单向流转：0->1->2，不允许回退或跳跃）
        if (currentStatus == Constants.SECKILL_ENDED) {
            //已经结束的活动不能再次开始或修改（秒杀结束后数据已固化，回退会导致订单与活动状态不一致）
            throw new BusinessException(ResultCode.SECKILL_ALREADY_ENDED);
        }
        if (status == Constants.SECKILL_ENDED && currentStatus != Constants.SECKILL_IN_PROGRESS) {
            // 不能从"未开始"直接跳到"已结束"，必须先开始再结束
            throw new BusinessException(ResultCode.SECKILL_STATUS_INVALID);
        }
        if (status == Constants.SECKILL_NOT_START) {
            //不允许手动将状态设置为"未开始"（0是创建时的初始状态，不应手动回退出）
            throw new BusinessException(ResultCode.SECKILL_STATUS_INVALID);
        }
        if (status == currentStatus) {
            //目标状态与当前状态相同，无需处理
            return;
        }

        //3.更新数据库中的秒杀活动状态
        sg.setStatus(status);
        seckillGoodsMapper.updateById(sg);
        log.info("秒杀活动状态变更：id={}, from {} to {}", id, currentStatus, status);

        //4.同步Redis缓存（Redis异常不影响数据库事务的核心完整性）
        String redisKey = String.valueOf(id);
        try {
            if (status == Constants.SECKILL_IN_PROGRESS) {
                //手动开始秒杀：初始化Redis库存、清除售罄标记
                //仅在key不存在时设置库存值，避免覆盖正在扣减中的库存（防止重启或并发场景下的库存重置）
                if (!redisService.exists(SeckillKey.STOCK, redisKey)) {
                    redisService.set(SeckillKey.STOCK, redisKey, sg.getStockCount());
                    log.info("秒杀开始，初始化Redis库存：id={},stock={}", id, sg.getStockCount());
                }
                //清除售罄标记：秒杀开始后，即使之前因库存耗尽而设置售罄标记，现在管理员重新开启了活动
                redisService.delete(SeckillKey.GOODS_OVER, redisKey);
            } else if (status == Constants.SECKILL_ENDED) {
                //手动结束秒杀：设置售罄标记阻止后续购买
                //无论库存是否耗尽，管理员手动结束活动后不应再接受新订单
                redisService.set(SeckillKey.GOODS_OVER, redisKey, true);
                log.info("秒杀结束，设置Redis售罄标记：id={}", id);
            }
        } catch (Exception e) {
            //Redis操作失败不应阻断核心数据库事务的一致性：后续可通过补偿机制同步
            log.warn("Redis同步异常，请检查Redis服务：id={}", id, e);
        }
    }
}
