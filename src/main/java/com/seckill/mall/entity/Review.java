package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单评价实体类
 */
@Data
@TableName("t_review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 订单号 */
    private String orderNo;

    /** 商品ID */
    private Long goodsId;

    /** 评分（1-5星） */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;
}
