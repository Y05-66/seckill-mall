package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.entity.Review;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.ReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评价控制器
 * <p>
 * 处理商品评价相关的HTTP请求，包括添加评价、查看商品评价、查看我的评价。
 * 基础路径：/review
 * </p>
 */
@Api(tags = "评价模块")
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 添加商品评价
     * <p>用户可以对已购买的商品发表评价，包含评分和文字内容</p>
     *
     * @param loginUser 当前登录用户
     * @param review    评价内容（包含goodsId、rating、content）
     */
    @ApiOperation("添加评价")
    @PostMapping
    public Result<?> addReview(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Review review) {
        reviewService.addReview(loginUser.getUserId(), review);
        return Result.success();
    }

    /**
     * 获取商品的评价列表
     * <p>按创建时间倒序返回，无需登录即可访问</p>
     *
     * @param goodsId 商品ID
     * @return 评价列表
     */
    @ApiOperation("获取商品评价列表")
    @GetMapping("/goods/{goodsId}")
    public Result<List<Review>> getGoodsReviews(@PathVariable Long goodsId) {
        return Result.success(reviewService.getGoodsReviews(goodsId));
    }

    /**
     * 获取当前用户的评价列表
     *
     * @param loginUser 当前登录用户
     * @return 用户的评价列表
     */
    @ApiOperation("获取我的评价列表")
    @GetMapping("/my")
    public Result<List<Review>> getMyReviews(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(reviewService.getUserReviews(loginUser.getUserId()));
    }
}
