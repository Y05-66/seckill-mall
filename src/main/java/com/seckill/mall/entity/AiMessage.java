package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI消息实体
 * <p>对应数据库表：t_ai_message</p>
 */
@Data
@TableName("t_ai_message")
public class AiMessage {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long conversationId;

    /** 角色: user / assistant */
    private String role;

    /** 消息内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;
}
