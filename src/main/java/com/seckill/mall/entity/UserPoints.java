package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户积分实体类
 */
@Data
@TableName("t_user_points")
public class UserPoints {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 积分余额 */
    private Integer points;

    /** 累计获得 */
    private Integer totalEarned;

    /** 累计使用 */
    private Integer totalUsed;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
