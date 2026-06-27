package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体类
 */
@Data
@TableName("t_notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 类型（order-订单，system-系统，promotion-促销） */
    private String type;

    /** 是否已读（0-未读，1-已读） */
    private Integer isRead;

    /** 关联ID（如订单号） */
    private String refId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
