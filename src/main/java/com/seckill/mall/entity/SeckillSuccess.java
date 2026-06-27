package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀成功记录实体类
 * <p>对应数据库表：t_seckill_success，记录用户秒杀成功信息，用于重复购买校验</p>
 */
@Data
@TableName("t_seckill_success")
public class SeckillSuccess {

    /** 记录ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 秒杀商品ID */
    private Long seckillGoodsId;
    /** 订单号 */
    private String orderNo;
    /** 创建时间 */
    private LocalDateTime createTime;
}
