package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 */
@Data
@TableName("t_coupon")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 优惠券名称 */
    private String name;

    /** 类型（1-满减，2-折扣） */
    private Integer type;

    /** 面值/折扣率 */
    private BigDecimal value;

    /** 最低消费金额 */
    private BigDecimal minAmount;

    /** 总数量 */
    private Integer totalCount;

    /** 已领取数量 */
    private Integer claimedCount;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 状态（0-禁用，1-启用） */
    private Integer status;

    private LocalDateTime createTime;
}
