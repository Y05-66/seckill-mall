package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * <p>对应数据库表：t_goods，存储普通商品的基础信息</p>
 */
@Data
@TableName("t_goods")
public class Goods {

    /** 商品ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 商品名称 */
    private String goodsName;
    /** 商品标题（简短描述） */
    private String goodsTitle;
    /** 商品图片URL */
    private String goodsImg;
    /** 商品价格（BigDecimal避免精度丢失） */
    private BigDecimal goodsPrice;
    /** 商品库存 */
    private Integer goodsStock;
    /** 商品详情描述 */
    private String goodsDesc;
    /** 商品状态（0-下架，1-上架） */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
