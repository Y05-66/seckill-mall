package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.dto.OrderDTO;
import com.seckill.mall.dto.OrderStatisticsDTO;
import com.seckill.mall.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员订单管理控制器
 * <p>
 * 处理后台管理端的订单管理请求，包括全部订单列表查看和订单统计
 * 基础路径：/admin/order
 * 所有接口均需管理员权限（SecurityConfig配置 + @PreAuthorize双重校验）
 * </p>
 * <p>
 * 与普通用户端 OrderController 的职责区分：
 * <ul>
 *   <li>OrderController - 用户端查看自己的订单、支付、取消（按userId筛选）</li>
 *   <li>AdminOrderController - 管理端查看全部订单、按状态/订单号筛选、统计数据</li>
 * </ul>
 * </p>
 */
@Api(tags = "管理员-订单管理模块")
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    /** 订单业务服务，负责订单查询和统计 */
    private final OrderService orderService;

    /**
     * 获取全部订单列表（支持按状态和订单号筛选）
     * <p>
     * 管理端可查看全量订单数据，并可按以下条件筛选：
     * <ul>
     *   <li>status - 按订单状态筛选（0=未支付，1=已支付，2=已取消，3=已超时，不传=全部）</li>
     *   <li>orderNo - 按订单号精确筛选（不传=全部）</li>
     * </ul>
     * 默认按创建时间倒序排列，最新订单排在前面。
     * </p>
     *
     * @param status  订单状态筛选条件（可选，不传则返回全部状态）
     * @param orderNo 订单号筛选条件（可选，不传则返回全部订单）
     * @return 订单DTO列表
     */
    @ApiOperation("全部订单列表")
    @GetMapping("/list")
    public Result<List<OrderDTO>> listAllOrders(
            @ApiParam(value = "订单状态筛选条件（0=未支付，1=已支付，2=已取消，3=已超时）",required = false)
            @RequestParam(required = false) Integer status,
            @ApiParam(value = "订单号筛选条件（精确匹配，不传=全部）",required = false)
            @RequestParam(required = false) String orderNo){
        return Result.success(orderService.listAllOrders(status, orderNo));
    }

    /**
     * 获取订单统计数据
     * <p>
     * 返回订单整体运营数据，包括：
     * <ul>
     *   <li>订单总数和各状态订单数量（未支付、已支付、已取消、已超时）</li>
     *   <li>总营收金额（已支付订单的秒杀成交价之和）</li>
     *   <li>今日新增订单数</li>
     * </ul>
     * 管理端可通过此数据快速了解秒杀活动的整体运营情况。
     * </p>
     *
     * @return 订单统计DTO
     */
    @ApiOperation("订单统计")
    @GetMapping("/statistics")
    public Result<OrderStatisticsDTO> getOrderStatistics(){
        return Result.success(orderService.getOrderStatistics());
    }
}
