package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员等级实体类
 */
@Data
@TableName("t_member_level")
public class MemberLevel {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 等级名称 */
    private String name;

    /** 等级值 */
    private Integer level;

    /** 所需积分 */
    private Integer requiredPoints;

    /** 折扣率 */
    private BigDecimal discount;

    /** 等级图标 */
    private String icon;
}
