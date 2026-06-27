package com.seckill.mall.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单统计信息响应DTO
 * <p>
 * 用于返回给管理端的订单统计数据，包含各状态订单数量和总营收
 * 管理端可通过此数据了解秒杀活动的整体运营情况
 * </p>
 */
@Data
public class OrderStatisticsDTO {
    /** 订单总数（所有状态的订单合计） */
    private Long totalOrders;
    /** 未支付订单数（status=0，等待用户支付或等超时取消 */
    private Long unpaidCount;
    /** 已支付订单数（status=1，成功完成的订单） */
    private Long paidCount;
    /** 已取消订单数（status=2，用户取消的订单） */
    private Long cancelledCount;
    /** 已超时订单数（status=3，系统自动取消） */
    private Long timeOutCount;
    /** 订单总营收（仅已支付订单的秒杀成交价之和） */
    private BigDecimal totalRevenue;
    /** 今日新增订单数（当天创建的订单和计） */
    private Long todayOrders;
}
