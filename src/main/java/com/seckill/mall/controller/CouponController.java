package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.entity.Coupon;
import com.seckill.mall.entity.UserCoupon;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券控制器
 * <p>
 * 处理优惠券相关的HTTP请求，包括查看可领取优惠券、领取优惠券、查看已领取优惠券。
 * 基础路径：/coupon
 * </p>
 */
@Api(tags = "优惠券模块")
@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /**
     * 获取可领取的优惠券列表
     * <p>返回当前时间有效且未领完的优惠券</p>
     */
    @ApiOperation("获取可领取的优惠券列表")
    @GetMapping("/available")
    public Result<List<Coupon>> getAvailableCoupons() {
        return Result.success(couponService.getAvailableCoupons());
    }

    /**
     * 领取优惠券
     * <p>每个用户每张优惠券只能领取一次，领取后不可重复领取</p>
     *
     * @param loginUser 当前登录用户
     * @param couponId  优惠券ID
     */
    @ApiOperation("领取优惠券")
    @PostMapping("/claim/{couponId}")
    public Result<?> claimCoupon(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long couponId) {
        couponService.claimCoupon(loginUser.getUserId(), couponId);
        return Result.success();
    }

    /**
     * 获取当前用户已领取的优惠券列表
     * <p>包括已使用、未使用、已过期的所有优惠券</p>
     *
     * @param loginUser 当前登录用户
     * @return 用户优惠券列表
     */
    @ApiOperation("获取我的优惠券列表")
    @GetMapping("/my")
    public Result<List<UserCoupon>> getMyCoupons(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(couponService.getUserCoupons(loginUser.getUserId()));
    }
}
