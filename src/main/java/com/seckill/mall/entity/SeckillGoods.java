package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品实体类
 * <p>对应数据库表：t_seckill_goods，通过goodsId关联普通商品表</p>
 */
@Data
@TableName("t_seckill_goods")
public class SeckillGoods {

    /** 秒杀商品ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联的普通商品ID（外键→t_goods.id） */
    private Long goodsId;
    /** 秒杀价格 */
    private BigDecimal seckillPrice;
    /** 秒杀库存（独立于普通商品库存） */
    private Integer stockCount;
    /** 秒杀开始时间 */
    private LocalDateTime startTime;
    /** 秒杀结束时间 */
    private LocalDateTime endTime;
    /** 秒杀状态（0-未开始，1-进行中，2-已结束） */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
}
