package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("t_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人ID */
    private Long userId;

    /** 操作人用户名 */
    private String username;

    /** 操作类型 */
    private String operation;

    /** 请求方法 */
    private String method;

    /** 请求URL */
    private String url;

    /** 请求参数 */
    private String params;

    /** IP地址 */
    private String ip;

    /** 操作结果（0-失败，1-成功） */
    private Integer result;

    /** 耗时（毫秒） */
    private Long costTime;

    private LocalDateTime createTime;
}
