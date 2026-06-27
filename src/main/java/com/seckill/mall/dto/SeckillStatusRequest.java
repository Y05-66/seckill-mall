package com.seckill.mall.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 秒杀活动状态变更请求DTO
 * <p>
 * 用于管理员手动切换秒杀活动状态。支持的状态值：
 * <ul>
 *   <li>1 - 开始秒杀（将未开始的秒杀活动手动启动）</li>
 *   <li>2 - 结束秒杀（将进行中的秒杀活动手动终止）</li>
 * </ul>
 * 不允许传入状态0（未开始），因为活动创建后默认未开始状态，无需手动设置。
 * </p>
 */
@Data
public class SeckillStatusRequest {
    /**
     * 目标秒杀状态，1=开始，2=结束
     */
    @NotNull(message = "目标状态不能为空")
    @Min(value = 1, message = "目标状态必须为1（开始）或2（结束）")
    @Max(value = 2, message = "目标状态必须为1（开始）或2（结束）")
    private Integer status;
}
