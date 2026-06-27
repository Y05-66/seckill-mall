package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分日志实体类
 */
@Data
@TableName("t_points_log")
public class PointsLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 积分变动 */
    private Integer points;

    /** 类型（earn-获得，use-使用） */
    private String type;

    /** 描述 */
    private String description;

    /** 关联订单号 */
    private String orderNo;

    private LocalDateTime createTime;
}
