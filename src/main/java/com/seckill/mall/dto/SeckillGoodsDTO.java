package com.seckill.mall.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品信息响应DTO
 * <p>
 * 用于返回给前端的秒杀商品信息，在普通商品基础上增加了秒杀专属字段。
 * 包含活动状态和倒计时秒数，方便前端展示秒杀状态和倒计时。
 * </p>
 */
@Data
public class SeckillGoodsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 秒杀商品记录ID */
    private Long id;
    /** 关联的普通商品ID */
    private Long goodsId;
    /** 商品名称 */
    private String goodsName;
    /** 商品标题 */
    private String goodsTitle;
    /** 商品图片URL */
    private String goodsImg;
    /** 商品原价 */
    private BigDecimal goodsPrice;
    /** 秒杀价格 */
    private BigDecimal seckillPrice;
    /** 秒杀库存（独立于普通商品库存） */
    private Integer stockCount;
    /** 商品总库存 */
    private Integer goodsStock;
    /** 秒杀开始时间 */
    private LocalDateTime startTime;
    /** 秒杀结束时间 */
    private LocalDateTime endTime;
    /** 秒杀状态（0-未开始，1-进行中，2-已结束） */
    private Integer status;
    /** 距离秒杀开始/结束的剩余秒数（用于前端倒计时） */
    private Long remainSeconds;
}
