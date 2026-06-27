package com.seckill.mall.service;

import com.seckill.mall.entity.Coupon;
import com.seckill.mall.entity.UserCoupon;

import java.util.List;

public interface CouponService {
    List<Coupon> getAvailableCoupons();
    void claimCoupon(Long userId, Long couponId);
    List<UserCoupon> getUserCoupons(Long userId);
    void useCoupon(Long userId, Long userCouponId);
}
