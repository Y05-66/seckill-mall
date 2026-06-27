package com.seckill.mall.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品信息响应DTO
 * <p>
 * 用于返回给前端的普通商品信息。
 * </p>
 */
@Data
public class GoodsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 商品ID */
    private Long id;
    /** 商品名称 */
    private String goodsName;
    /** 商品标题 */
    private String goodsTitle;
    /** 商品图片URL */
    private String goodsImg;
    /** 商品价格（使用BigDecimal避免精度丢失） */
    private BigDecimal goodsPrice;
    /** 商品库存 */
    private Integer goodsStock;
    /** 商品描述 */
    private String goodsDesc;
    /** 商品状态（0-下架，1-上架） */
    private Integer status;
}
