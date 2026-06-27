package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.Constants;
import com.seckill.mall.common.ResultCode;
import com.seckill.mall.dto.OrderDTO;
import com.seckill.mall.dto.OrderStatisticsDTO;
import com.seckill.mall.entity.OrderDetail;
import com.seckill.mall.entity.SeckillOrder;
import com.seckill.mall.entity.SeckillSuccess;
import com.seckill.mall.mapper.OrderDetailMapper;
import com.seckill.mall.mapper.SeckillGoodsMapper;
import com.seckill.mall.mapper.SeckillSuccessMapper;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.SeckillKey;
import com.seckill.mall.mapper.SeckillOrderMapper;
import com.seckill.mall.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单业务服务实现类
 * <p>实现订单查询、支付、取消等业务逻辑</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final SeckillOrderMapper seckillOrderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final SeckillGoodsMapper seckillGoodsMapper;
    private final SeckillSuccessMapper seckillSuccessMapper;
    private final RedisService redisService;

    /** 查询用户的订单列表（按创建时间倒序，使用批量查询避免N+1） */
    @Override
    public List<OrderDTO> listUserOrders(Long userId) {
        List<SeckillOrder> orders = seckillOrderMapper.selectList(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getUserId, userId)
                        .orderByDesc(SeckillOrder::getCreateTime));
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        return batchConvertToOrderDTO(orders);
    }

    /** 查询订单详情（校验订单归属） */
    @Override
    public OrderDTO getOrderDetail(String orderNo, Long userId) {
        SeckillOrder order = seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getOrderNo, orderNo)
                        .eq(SeckillOrder::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return toOrderDTO(order);
    }

    /**
     * 模拟支付订单
     * 校验订单归属和状态后，更新为已支付
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo, Long userId) {
        SeckillOrder order = seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getOrderNo, orderNo)
                        .eq(SeckillOrder::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != Constants.ORDER_UNPAID) {
            throw new BusinessException(ResultCode.ORDER_PAID);
        }

        order.setStatus(Constants.ORDER_PAID);
        order.setPayTime(LocalDateTime.now());
        seckillOrderMapper.updateById(order);
    }

    /**
     * 取消订单
     * 校验订单归属和状态后，更新为已取消并回滚秒杀库存
     * 使用条件更新保证幂等性，防止与超时取消并发导致重复回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, Long userId) {
        SeckillOrder order = seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getOrderNo, orderNo)
                        .eq(SeckillOrder::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != Constants.ORDER_UNPAID) {
            throw new BusinessException("只能取消未支付的订单");
        }

        // 条件更新订单状态（仅未支付→已取消），保证幂等性
        order.setStatus(Constants.ORDER_CANCELLED);
        int affected = seckillOrderMapper.update(order,
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getId, order.getId())
                        .eq(SeckillOrder::getStatus, Constants.ORDER_UNPAID));
        if (affected == 0) {
            throw new BusinessException("订单状态已变更，请刷新后重试");
        }

        // 回滚秒杀库存（数据库原子自增）
        // 使用原子SQL在数据库层面回滚库存，避免 read-modify-write 竞态
        seckillGoodsMapper.increaseStock(order.getSeckillGoodsId());

        // 同步回滚 Redis 中的库存和售罄标记，否则 Redis 中库存可能仍为0导致售罄状态未清除
        try {
            // 原子自增 Redis 库存计数
            redisService.incr(SeckillKey.STOCK, String.valueOf(order.getSeckillGoodsId()));
            // 删除售罄标记（如果存在）
            redisService.delete(SeckillKey.GOODS_OVER, String.valueOf(order.getSeckillGoodsId()));
        } catch (Exception e) {
            // Redis 操作不应影响订单取消的核心事务
            log.error("Redis操作异常，请检查Redis服务: seckillGoodsId={}, error={}", order.getSeckillGoodsId(), e.getMessage());
        }

        // 删除秒杀成功记录（允许用户重新参与秒杀）
        seckillSuccessMapper.delete(
                new LambdaQueryWrapper<SeckillSuccess>()
                        .eq(SeckillSuccess::getUserId, userId)
                        .eq(SeckillSuccess::getSeckillGoodsId, order.getSeckillGoodsId()));

        // 删除 Redis 中的用户秒杀标记
        try {
            redisService.delete(SeckillKey.SECKILL_USER, userId + ":" + order.getSeckillGoodsId());
        } catch (Exception e) {
            log.error("删除用户秒杀标记失败: userId={}, seckillGoodsId={}, error={}",
                    userId, order.getSeckillGoodsId(), e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(String orderNo, Long userId) {
        SeckillOrder order = seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getOrderNo, orderNo)
                        .eq(SeckillOrder::getUserId, userId));
        if (order == null) {
            throw new BusinessException("订单不存在或不属于当前用户");
        }
        // 只允许删除已取消、已超时、已退款的订单
        if (order.getStatus() == Constants.ORDER_UNPAID || order.getStatus() == Constants.ORDER_PAID) {
            throw new BusinessException("未支付或已支付的订单不能删除，请先取消或申请退款");
        }
        // 删除订单详情
        orderDetailMapper.delete(
                new LambdaQueryWrapper<OrderDetail>()
                        .eq(OrderDetail::getOrderNo, orderNo));
        // 删除订单
        seckillOrderMapper.deleteById(order.getId());
    }

    /**
     * 获取全部订单列表（管理端使用，支持按状态和订单号筛选）
     * <p>
     * 使用MyBatis-Plus的LambdaQueryWrapper动态构建查询条件：
     * - status非null时按状态精确匹配
     * - orderNo非null且非空时按订单号精确匹配（订单号是系统生成的唯一值，无需模糊匹配）
     * - 默认按创建时间倒序排序，最新订单排在前面
     * </p>
     *
     * @param status 订单状态筛选条件（null=全部）
     * @param orderNo 订单号筛选条件（null=全部）
     * @return 订单DTO列表
     */
    @Override
    public List<OrderDTO> listAllOrders(Integer status, String orderNo){
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();

        //动态条件构建：status非null时添加状态筛选
        if(status != null){
            wrapper.eq(SeckillOrder::getStatus, status);
        }

        //动态条件构建：orderNo非null且非空时添加订单号精确筛选
        if(orderNo != null && !orderNo.trim().isEmpty()){
            wrapper.eq(SeckillOrder::getOrderNo,orderNo.trim());
        }

        //按创建时间倒序，管理端优先看到最新订单
        wrapper.orderByDesc(SeckillOrder::getCreateTime);

        List<SeckillOrder> orders = seckillOrderMapper.selectList(wrapper);
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        return batchConvertToOrderDTO(orders);
    }
    /**
     * 获取订单统计时间（管理端使用）
     * <p>
     * 统计逻辑说明：
     * 1.各状态订单数量：直接从t_seckill_order按status分组统计
     * 2.总营收：查询所有已支付订单关联的订单明细，累加goodsPrice * goodsCount
     * 3.今日订单数：按create——time筛选当天创建的订单统计
     * </p>
     * <p>
     * 注意：营收计算从t_order_detaill表取价格，因为该表存储了下单时的秒杀价快照
     * 即使商品后来调价，历史订单的营收数据也不会受影响
     * </p>
     *
     * @return 订单统计DTO
     */
    @Override
    public OrderStatisticsDTO getOrderStatistics() {
        // 使用聚合SQL查询，将8次独立查询优化为2次
        Map<String, Object> statsMap = seckillOrderMapper.getOrderStatistics();
        BigDecimal totalRevenue = seckillOrderMapper.getTotalRevenue();

        OrderStatisticsDTO stats = new OrderStatisticsDTO();
        stats.setTotalOrders(toLong(statsMap.get("total_orders")));
        stats.setUnpaidCount(toLong(statsMap.get("unpaid_count")));
        stats.setPaidCount(toLong(statsMap.get("paid_count")));
        stats.setCancelledCount(toLong(statsMap.get("cancelled_count")));
        stats.setTimeOutCount(toLong(statsMap.get("timeout_count")));
        stats.setTodayOrders(toLong(statsMap.get("today_orders")));
        stats.setTotalRevenue(totalRevenue);

        return stats;
    }

    /**
     * 安全转换为Long类型（处理MySQL返回的BigInteger等类型）
     */
    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    /**
     * 批量转换订单DTO（避免N+1查询）
     * 先批量查询所有订单明细，再逐个转换
     */
    private List<OrderDTO> batchConvertToOrderDTO(List<SeckillOrder> orders) {
        // 批量查询所有订单的明细
        List<String> orderNos = orders.stream()
                .map(SeckillOrder::getOrderNo)
                .collect(Collectors.toList());
        Map<String, OrderDetail> detailMap = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().in(OrderDetail::getOrderNo, orderNos))
                .stream()
                .collect(Collectors.toMap(OrderDetail::getOrderNo, d -> d));

        return orders.stream()
                .map(order -> toOrderDTO(order, detailMap.get(order.getOrderNo())))
                .collect(Collectors.toList());
    }

    /**
     * 实体转DTO：订单（单个查询版本，用于详情查询）
     */
    private OrderDTO toOrderDTO(SeckillOrder order) {
        OrderDetail detail = orderDetailMapper.selectOne(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderNo, order.getOrderNo()));
        return toOrderDTO(order, detail);
    }

    /**
     * 实体转DTO：订单（批量查询版本，避免N+1查询）
     */
    private OrderDTO toOrderDTO(SeckillOrder order, OrderDetail detail) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setSeckillGoodsId(order.getSeckillGoodsId());
        dto.setStatus(order.getStatus());
        dto.setCreateTime(order.getCreateTime());
        dto.setPayTime(order.getPayTime());

        // 填充订单明细中的商品信息
        if (detail != null) {
            dto.setGoodsName(detail.getGoodsName());
            dto.setSeckillPrice(detail.getGoodsPrice());
            dto.setGoodsCount(detail.getGoodsCount());
        }

        // 状态码转中文描述
        switch (order.getStatus()) {
            case Constants.ORDER_UNPAID: dto.setStatusDesc("待支付"); break;
            case Constants.ORDER_PAID: dto.setStatusDesc("已支付"); break;
            case Constants.ORDER_CANCELLED: dto.setStatusDesc("已取消"); break;
            case Constants.ORDER_TIMEOUT: dto.setStatusDesc("已超时"); break;
            case Constants.ORDER_REFUNDED: dto.setStatusDesc("已退款"); break;
            default: dto.setStatusDesc("未知");
        }

        return dto;
    }
}
