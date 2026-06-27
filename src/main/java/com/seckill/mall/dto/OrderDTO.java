package com.seckill.mall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单信息响应DTO
 * <p>
 * 用于返回给前端的订单信息，包含订单状态的中文描述。
 * </p>
 */
@Data
public class OrderDTO {
    /** 订单ID */
    private Long id;
    /** 订单编号（系统生成的唯一号） */
    private String orderNo;
    /** 下单用户ID */
    private Long userId;
    /** 秒杀商品ID */
    private Long seckillGoodsId;
    /** 商品名称（冗余存储，避免关联查询） */
    private String goodsName;
    /** 秒杀成交价格 */
    private BigDecimal seckillPrice;
    /** 购买数量 */
    private Integer goodsCount;
    /** 订单状态码（0-未支付，1-已支付，2-已取消） */
    private Integer status;
    /** 订单状态中文描述（如"未支付"、"已支付"） */
    private String statusDesc;
    /** 订单创建时间 */
    private LocalDateTime createTime;
    /** 支付时间（未支付时为null） */
    private LocalDateTime payTime;
}
