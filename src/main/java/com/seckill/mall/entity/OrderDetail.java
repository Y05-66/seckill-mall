package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体类
 * <p>对应数据库表：t_order_detail，记录订单中的商品快照信息</p>
 */
@Data
@TableName("t_order_detail")
public class OrderDetail {

    /** 订单明细ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单编号（关联t_seckill_order.orderNo） */
    private String orderNo;
    /** 商品ID（关联t_goods.id） */
    private Long goodsId;
    /** 商品名称（下单时快照，避免商品改名影响历史订单） */
    private String goodsName;
    /** 商品价格（下单时秒杀价快照） */
    private BigDecimal goodsPrice;
    /** 购买数量 */
    private Integer goodsCount;
    /** 创建时间 */
    private LocalDateTime createTime;
}
