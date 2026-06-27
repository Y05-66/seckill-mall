package com.seckill.mall.controller;

import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.Result;
import com.seckill.mall.entity.Cart;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@Api(tags = "购物车模块")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @ApiOperation("添加商品到购物车")
    @PostMapping("/add")
    public Result<?> addToCart(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, Object> body) {
        if (body.get("goodsId") == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Long goodsId;
        Integer quantity;
        try {
            goodsId = Long.valueOf(body.get("goodsId").toString());
            quantity = body.get("quantity") != null ? Integer.valueOf(body.get("quantity").toString()) : 1;
        } catch (NumberFormatException e) {
            throw new BusinessException("参数格式错误");
        }
        cartService.addToCart(loginUser.getUserId(), goodsId, quantity);
        return Result.success();
    }

    @ApiOperation("获取购物车列表")
    @GetMapping("/list")
    public Result<List<Cart>> getCartList(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cartService.getCartList(loginUser.getUserId()));
    }

    @ApiOperation("更新购物车商品数量")
    @PutMapping("/update")
    public Result<?> updateQuantity(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, Object> body) {
        if (body.get("cartId") == null) {
            throw new BusinessException("购物车ID不能为空");
        }
        if (body.get("quantity") == null) {
            throw new BusinessException("数量不能为空");
        }
        Long cartId;
        Integer quantity;
        try {
            cartId = Long.valueOf(body.get("cartId").toString());
            quantity = Integer.valueOf(body.get("quantity").toString());
        } catch (NumberFormatException e) {
            throw new BusinessException("参数格式错误");
        }
        cartService.updateQuantity(loginUser.getUserId(), cartId, quantity);
        return Result.success();
    }

    @ApiOperation("删除购物车商品")
    @DeleteMapping("/{cartId}")
    public Result<?> deleteCartItem(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long cartId) {
        cartService.deleteCartItem(loginUser.getUserId(), cartId);
        return Result.success();
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clear")
    public Result<?> clearCart(@AuthenticationPrincipal LoginUser loginUser) {
        cartService.clearCart(loginUser.getUserId());
        return Result.success();
    }

    @ApiOperation("选中/取消选中购物车商品")
    @PutMapping("/check")
    public Result<?> toggleCheck(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, Object> body) {
        if (body.get("cartId") == null) {
            throw new BusinessException("购物车ID不能为空");
        }
        if (body.get("checked") == null) {
            throw new BusinessException("选中状态不能为空");
        }
        Long cartId = Long.valueOf(body.get("cartId").toString());
        Integer checked = Integer.valueOf(body.get("checked").toString());
        cartService.toggleCheck(loginUser.getUserId(), cartId, checked);
        return Result.success();
    }

    @ApiOperation("全选/取消全选")
    @PutMapping("/check-all")
    public Result<?> toggleCheckAll(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, Object> body) {
        if (body.get("checked") == null) {
            throw new BusinessException("选中状态不能为空");
        }
        Integer checked = Integer.valueOf(body.get("checked").toString());
        cartService.toggleCheckAll(loginUser.getUserId(), checked);
        return Result.success();
    }

    @ApiOperation("获取购物车商品数量")
    @GetMapping("/count")
    public Result<Integer> getCartCount(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cartService.getCartCount(loginUser.getUserId()));
    }
}
