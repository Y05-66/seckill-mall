package com.seckill.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.mall.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 秒杀订单表数据访问层接口
 * <p>继承MyBatis-Plus的BaseMapper，自动获得CRUD通用方法</p>
 */
@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {

    /**
     * 订单统计聚合查询（单次SQL替代多次COUNT）
     * 返回Map包含：total_orders, unpaid_count, paid_count, cancelled_count, timeout_count, today_orders
     */
    @Select("SELECT " +
            "COUNT(*) AS total_orders, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS unpaid_count, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS paid_count, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS cancelled_count, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS timeout_count, " +
            "SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) AS today_orders " +
            "FROM t_seckill_order")
    Map<String, Object> getOrderStatistics();

    /**
     * 查询已支付订单总营收（SQL聚合替代Java内存计算）
     */
    @Select("SELECT COALESCE(SUM(d.goods_price * d.goods_count), 0) AS total_revenue " +
            "FROM t_order_detail d " +
            "INNER JOIN t_seckill_order o ON d.order_no = o.order_no " +
            "WHERE o.status = 1")
    BigDecimal getTotalRevenue();
}
