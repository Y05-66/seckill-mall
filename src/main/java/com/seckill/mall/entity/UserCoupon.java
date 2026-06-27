package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体类
 */
@Data
@TableName("t_user_coupon")
public class UserCoupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long couponId;

    /** 优惠券名称 */
    private String couponName;

    /** 类型 */
    private Integer type;

    /** 面值/折扣率 */
    private BigDecimal value;

    /** 最低消费 */
    private BigDecimal minAmount;

    /** 状态（0-未使用，1-已使用，2-已过期） */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime useTime;
}
