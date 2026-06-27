package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 */
@Data
@TableName("t_cart")
public class Cart {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long goodsId;

    /** 商品名称 */
    private String goodsName;

    /** 商品图片 */
    private String goodsImg;

    /** 商品价格 */
    private BigDecimal goodsPrice;

    /** 购买数量 */
    private Integer quantity;

    /** 是否选中（0-未选中，1-选中） */
    private Integer checked;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
