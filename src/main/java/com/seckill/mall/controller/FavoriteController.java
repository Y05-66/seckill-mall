package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.entity.Goods;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.FavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 */
@Api(tags = "收藏模块")
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @ApiOperation("添加收藏")
    @PostMapping("/{goodsId}")
    public Result<?> addFavorite(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long goodsId) {
        favoriteService.addFavorite(loginUser.getUserId(), goodsId);
        return Result.success();
    }

    @ApiOperation("取消收藏")
    @DeleteMapping("/{goodsId}")
    public Result<?> removeFavorite(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long goodsId) {
        favoriteService.removeFavorite(loginUser.getUserId(), goodsId);
        return Result.success();
    }

    @ApiOperation("检查是否已收藏")
    @GetMapping("/check/{goodsId}")
    public Result<Boolean> isFavorite(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long goodsId) {
        return Result.success(favoriteService.isFavorite(loginUser.getUserId(), goodsId));
    }

    @ApiOperation("获取收藏列表")
    @GetMapping("/list")
    public Result<List<Goods>> getFavoriteList(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(favoriteService.getFavoriteList(loginUser.getUserId()));
    }

    @ApiOperation("获取收藏数量")
    @GetMapping("/count")
    public Result<Integer> getFavoriteCount(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(favoriteService.getFavoriteCount(loginUser.getUserId()));
    }
}
