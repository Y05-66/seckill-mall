package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.dto.OrderDTO;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 * <p>
 * 处理订单相关的HTTP请求，包括查看订单列表、订单详情、模拟支付和取消订单。
 * 基础路径：/order
 * 所有接口均需登录认证。
 * </p>
 */
@Api(tags = "订单模块")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    /** 订单业务服务 */
    private final OrderService orderService;

    /**
     * 获取当前用户的订单列表
     *
     * @param loginUser 当前登录用户
     * @return 订单DTO列表
     */
    @ApiOperation("我的订单列表")
    @GetMapping("/list")
    public Result<List<OrderDTO>> listOrders(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(orderService.listUserOrders(loginUser.getUserId()));
    }

    /**
     * 获取订单详情
     *
     * @param orderNo 订单编号（路径参数）
     * @return 订单详情DTO
     */
    @ApiOperation("订单详情")
    @GetMapping("/{orderNo}")
    public Result<OrderDTO> getOrder(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable String orderNo) {
        return Result.success(orderService.getOrderDetail(orderNo, loginUser.getUserId()));
    }

    /**
     * 模拟支付接口
     * <p>
     * 模拟用户支付流程，将订单状态从"未支付"更新为"已支付"。
     * 实际项目中应对接第三方支付平台（如支付宝、微信支付）。
     * </p>
     *
     * @param loginUser 当前登录用户（用于校验订单归属）
     * @param orderNo   订单编号
     * @return 支付成功返回成功结果
     */
    @ApiOperation("模拟支付")
    @PostMapping("/pay/{orderNo}")
    public Result<?> payOrder(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable String orderNo) {
        orderService.payOrder(orderNo, loginUser.getUserId());
        return Result.success();
    }

    /**
     * 取消订单接口
     * <p>
     * 用户主动取消订单，会释放对应的秒杀库存。
     * 会校验订单是否属于当前登录用户，防止越权操作。
     * </p>
     *
     * @param loginUser 当前登录用户（用于校验订单归属）
     * @param orderNo   订单编号
     * @return 取消成功返回成功结果
     */
    @ApiOperation("取消订单")
    @PostMapping("/cancel/{orderNo}")
    public Result<?> cancelOrder(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable String orderNo) {
        orderService.cancelOrder(orderNo, loginUser.getUserId());
        return Result.success();
    }

    /**
     * 删除订单接口
     * <p>
     * 仅允许删除已取消、已超时、已退款的订单。
     * 未支付和已支付的订单不能删除，需先取消或申请退款。
     * </p>
     *
     * @param loginUser 当前登录用户（用于校验订单归属）
     * @param orderNo   订单编号
     * @return 删除成功返回成功结果
     */
    @ApiOperation("删除订单")
    @DeleteMapping("/{orderNo}")
    public Result<?> deleteOrder(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable String orderNo) {
        orderService.deleteOrder(orderNo, loginUser.getUserId());
        return Result.success();
    }
}
