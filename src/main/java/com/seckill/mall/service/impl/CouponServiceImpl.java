package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.entity.Coupon;
import com.seckill.mall.entity.UserCoupon;
import com.seckill.mall.mapper.CouponMapper;
import com.seckill.mall.mapper.UserCouponMapper;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.CouponKey;
import com.seckill.mall.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券服务实现类
 * <p>
 * 处理优惠券的查询、领取、使用等业务逻辑。
 * 使用Redis缓存减少数据库访问，使用原子SQL防止并发超领。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final RedisService redisService;

    /**
     * 获取可领取的优惠券列表
     * <p>返回当前时间有效且未领完的优惠券，使用Redis缓存（5分钟TTL）</p>
     *
     * @return 可领取的优惠券列表
     */
    @Override
    public List<Coupon> getAvailableCoupons() {
        // 优先从Redis缓存获取
        List<Coupon> cached = redisService.get(CouponKey.AVAILABLE_LIST, "all");
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<Coupon> list = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, 1)
                        .le(Coupon::getStartTime, LocalDateTime.now())
                        .ge(Coupon::getEndTime, LocalDateTime.now())
                        .apply("claimed_count < total_count"));

        // 写入Redis缓存
        redisService.set(CouponKey.AVAILABLE_LIST, "all", list);
        return list;
    }

    /**
     * 领取优惠券
     * <p>
     * 领取流程：
     * 1. 验证优惠券存在且有效
     * 2. 检查用户是否已领取
     * 3. 原子更新已领取数量（防止并发超领）
     * 4. 创建用户优惠券记录
     * 5. 清除相关缓存
     * </p>
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @throws BusinessException 优惠券不存在、已过期、已领取或已领完时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimCoupon(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券不存在或已下架");
        }
        if (coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }

        // 检查是否已领取
        Long count = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, couponId));
        if (count > 0) {
            throw new BusinessException("已领取过该优惠券");
        }

        // 原子更新已领取数量（防止并发超领）
        int affected = couponMapper.update(null,
                new LambdaUpdateWrapper<Coupon>()
                        .eq(Coupon::getId, couponId)
                        .apply("claimed_count < total_count")
                        .setSql("claimed_count = claimed_count + 1"));
        if (affected == 0) {
            throw new BusinessException("优惠券已领完");
        }

        // 创建用户优惠券记录
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setCouponName(coupon.getName());
        userCoupon.setType(coupon.getType());
        userCoupon.setValue(coupon.getValue());
        userCoupon.setMinAmount(coupon.getMinAmount());
        userCoupon.setStatus(0);
        userCoupon.setCreateTime(LocalDateTime.now());
        userCouponMapper.insert(userCoupon);

        // 清除用户优惠券缓存和可用优惠券缓存
        redisService.delete(CouponKey.USER_LIST, String.valueOf(userId));
        redisService.delete(CouponKey.AVAILABLE_LIST, "all");
    }

    /**
     * 获取用户已领取的优惠券列表
     * <p>使用Redis缓存（2分钟TTL）</p>
     *
     * @param userId 用户ID
     * @return 用户优惠券列表
     */
    @Override
    public List<UserCoupon> getUserCoupons(Long userId) {
        // 优先从Redis缓存获取
        List<UserCoupon> cached = redisService.get(CouponKey.USER_LIST, String.valueOf(userId));
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<UserCoupon> list = userCouponMapper.selectList(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .orderByDesc(UserCoupon::getCreateTime));

        // 写入Redis缓存
        redisService.set(CouponKey.USER_LIST, String.valueOf(userId), list);
        return list;
    }

    /**
     * 使用优惠券
     * <p>将优惠券状态从"未使用"改为"已使用"，并记录使用时间</p>
     *
     * @param userId       用户ID
     * @param userCouponId 用户优惠券ID
     * @throws BusinessException 优惠券不存在或已使用时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(Long userId, Long userCouponId) {
        UserCoupon coupon = userCouponMapper.selectOne(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getId, userCouponId)
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getStatus, 0));
        if (coupon == null) {
            throw new BusinessException("优惠券不存在或已使用");
        }
        coupon.setStatus(1);
        coupon.setUseTime(LocalDateTime.now());
        userCouponMapper.updateById(coupon);

        // 清除用户优惠券缓存
        redisService.delete(CouponKey.USER_LIST, String.valueOf(userId));
    }
}
