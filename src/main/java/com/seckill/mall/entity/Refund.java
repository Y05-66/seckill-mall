package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款/退货实体类
 */
@Data
@TableName("t_refund")
public class Refund {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 订单号 */
    private String orderNo;

    /** 退款类型（1-仅退款，2-退货退款） */
    private Integer type;

    /** 退款金额 */
    private BigDecimal amount;

    /** 退款原因 */
    private String reason;

    /** 状态（0-待审核，1-已同意，2-已拒绝，3-已完成） */
    private Integer status;

    /** 管理员备注 */
    private String adminNote;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
