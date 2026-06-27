package com.seckill.mall.service;

import com.seckill.mall.common.BusinessException;

/**
 * 秒杀业务服务接口
 * <p>
 * 定义秒杀核心业务方法，采用Redis预扣库存 + MQ异步下单的架构：
 * 1. doSeckill() - 秒杀入口，Redis原子扣库存，发送MQ消息
 * 2. createOrder() - MQ消费者调用，数据库扣库存 + 创建订单
 * 3. getSeckillResult() - 前端轮询查询秒杀结果
 * 4. initSeckillStock() - 系统启动时初始化库存到Redis
 * </p>
 */
public interface SeckillService {

    /**
     * 秒杀入口（Redis预减库存 + MQ异步下单）
     *
     * @param userId         用户ID
     * @param seckillGoodsId 秒杀商品ID
     * @return 订单号（同步成功时），null表示已进入MQ排队
     * @throws BusinessException 库存不足或重复抢购时抛出异常
     */
    String doSeckill(Long userId, Long seckillGoodsId);

    /**
     * 创建订单（由MQ消费者调用）
     * <p>
     * 在事务中执行：数据库乐观锁扣库存 → 创建秒杀订单 → 创建订单明细
     * </p>
     *
     * @param userId         用户ID
     * @param seckillGoodsId 秒杀商品ID
     */
    void createOrder(Long userId, Long seckillGoodsId);

    /**
     * 查询秒杀结果
     *
     * @param userId         用户ID
     * @param seckillGoodsId 秒杀商品ID
     * @return 订单号（成功），"sold_out"（已售罄），null（排队中）
     */
    String getSeckillResult(Long userId, Long seckillGoodsId);

    /**
     * 初始化秒杀库存到Redis（系统启动时调用）
     * <p>将数据库中的秒杀库存数据预加载到Redis，只在Key不存在时设置</p>
     */
    void initSeckillStock();

    /**
     * 更新秒杀活动状态（管理员手动开始/结束秒杀）
     * <p>
     * 允许的状态转换：
     * <ul>
     *   <li>未开始（0）-- 进行中（1）：手动开始秒杀，初始化Redis库存清除售罄标记</li>
     *   <li>进行中（1）-- 已结束（2）：手动结束秒杀，设置Redis售罄标记阻止后续购买</li>
     * </ul>
     * 不允许的转换：已经结束（2）不能回退、不能从未开始直接跳到已结束，不能将状态设为未开始（0).
     * </p>
     *
     * @param id     秒杀商品ID
     * @param status 目标状态（1=开始，2=结束）
     * @throws BusinessException 秒杀商品不存在、状态不合法抛出异常
     */
    void updateSeckillStatus(Long id, Integer status);
}
