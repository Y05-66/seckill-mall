package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.Constants;
import com.seckill.mall.entity.Refund;
import com.seckill.mall.entity.SeckillGoods;
import com.seckill.mall.entity.SeckillOrder;
import com.seckill.mall.entity.SeckillSuccess;
import com.seckill.mall.mapper.RefundMapper;
import com.seckill.mall.mapper.SeckillGoodsMapper;
import com.seckill.mall.mapper.SeckillOrderMapper;
import com.seckill.mall.mapper.SeckillSuccessMapper;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.SeckillKey;
import com.seckill.mall.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款服务实现类
 * <p>
 * 处理退款申请、审批、拒绝等业务逻辑。
 * 退款审批通过时会回滚库存（DB + Redis）并删除秒杀成功记录。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundMapper refundMapper;
    private final SeckillOrderMapper seckillOrderMapper;
    private final SeckillGoodsMapper seckillGoodsMapper;
    private final SeckillSuccessMapper seckillSuccessMapper;
    private final RedisService redisService;

    /**
     * 提交退款申请
     *
     * @param userId 用户ID（校验订单归属）
     * @param refund 退款信息（包含orderNo、type、amount、reason）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long userId, Refund refund) {
        SeckillOrder order = seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getOrderNo, refund.getOrderNo())
                        .eq(SeckillOrder::getUserId, userId));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != Constants.ORDER_PAID) {
            throw new BusinessException("只有已支付订单可以申请退款");
        }

        // 检查是否已申请
        Long count = refundMapper.selectCount(
                new LambdaQueryWrapper<Refund>()
                        .eq(Refund::getOrderNo, refund.getOrderNo())
                        .in(Refund::getStatus, 0, 1));
        if (count > 0) {
            throw new BusinessException("已申请退款，请等待处理");
        }

        refund.setUserId(userId);
        refund.setStatus(0);
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        refundMapper.insert(refund);
    }

    /**
     * 获取用户的退款申请列表
     *
     * @param userId 用户ID
     * @return 退款申请列表，按创建时间倒序
     */
    @Override
    public List<Refund> getUserRefunds(Long userId) {
        return refundMapper.selectList(
                new LambdaQueryWrapper<Refund>()
                        .eq(Refund::getUserId, userId)
                        .orderByDesc(Refund::getCreateTime));
    }

    /**
     * 获取所有退款申请（管理端使用）
     *
     * @return 退款申请列表，按创建时间倒序
     */
    @Override
    public List<Refund> getAllRefunds() {
        return refundMapper.selectList(
                new LambdaQueryWrapper<Refund>().orderByDesc(Refund::getCreateTime));
    }

    /**
     * 审批通过退款申请
     * <p>
     * 流程：更新退款状态 → 更新订单状态为已退款 → 回滚DB库存 → 回滚Redis库存 → 删除秒杀成功记录
     * </p>
     *
     * @param refundId  退款申请ID
     * @param adminNote 管理员备注
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(Long refundId, String adminNote) {
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null || refund.getStatus() != 0) {
            throw new BusinessException("退款申请不存在或已处理");
        }

        // 1. 更新退款状态为已批准
        refund.setStatus(1);
        refund.setAdminNote(adminNote);
        refund.setUpdateTime(LocalDateTime.now());
        refundMapper.updateById(refund);

        // 2. 更新订单状态为已退款（仅已支付的订单可以退款）
        SeckillOrder order = seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getOrderNo, refund.getOrderNo()));
        if (order != null) {
            if (order.getStatus() != Constants.ORDER_PAID) {
                throw new BusinessException("订单当前状态不允许退款");
            }
            order.setStatus(Constants.ORDER_REFUNDED);
            seckillOrderMapper.updateById(order);

            // 3. 回滚秒杀商品库存（DB层原子操作）
            SeckillGoods seckillGoods = seckillGoodsMapper.selectById(order.getSeckillGoodsId());
            if (seckillGoods != null) {
                seckillGoodsMapper.increaseStock(order.getSeckillGoodsId());

                // 4. 回滚Redis中的库存计数
                redisService.incr(SeckillKey.STOCK, String.valueOf(order.getSeckillGoodsId()));
                // 删除售罄标记（如果存在）
                redisService.delete(SeckillKey.GOODS_OVER, String.valueOf(order.getSeckillGoodsId()));

                log.info("退款审批通过，已回滚库存: orderId={}, seckillGoodsId={}", order.getOrderNo(), order.getSeckillGoodsId());
            }

            // 5. 删除秒杀成功记录（允许用户重新参与秒杀）
            seckillSuccessMapper.delete(
                    new LambdaQueryWrapper<SeckillSuccess>()
                            .eq(SeckillSuccess::getUserId, order.getUserId())
                            .eq(SeckillSuccess::getSeckillGoodsId, order.getSeckillGoodsId()));
            // 删除Redis中的用户秒杀标记
            redisService.delete(SeckillKey.SECKILL_USER,
                    order.getUserId() + ":" + order.getSeckillGoodsId());
        }
    }

    /**
     * 拒绝退款申请
     *
     * @param refundId  退款申请ID
     * @param adminNote 管理员备注（拒绝原因）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRefund(Long refundId, String adminNote) {
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null || refund.getStatus() != 0) {
            throw new BusinessException("退款申请不存在或已处理");
        }
        refund.setStatus(2);
        refund.setAdminNote(adminNote);
        refund.setUpdateTime(LocalDateTime.now());
        refundMapper.updateById(refund);
    }
}
