package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀订单实体类
 * <p>对应数据库表：t_seckill_order，记录秒杀成功后的订单信息</p>
 */
@Data
@TableName("t_seckill_order")
public class SeckillOrder {

    /** 订单ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 下单用户ID（外键→t_user.id） */
    private Long userId;
    /** 秒杀商品ID（外键→t_seckill_goods.id） */
    private Long seckillGoodsId;
    /** 订单编号（系统生成的唯一号） */
    private String orderNo;
    /** 订单状态（0-未支付，1-已支付，2-已取消） */
    private Integer status;
    /** 创建时间（下单时间） */
    private LocalDateTime createTime;
    /** 支付时间（未支付时为null） */
    private LocalDateTime payTime;
}
