package com.seckill.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.mall.entity.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 秒杀商品表数据访问层接口
 * <p>继承MyBatis-Plus的BaseMapper，额外提供乐观锁扣减库存方法</p>
 */
@Mapper
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    /**
     * 乐观锁扣减库存
     * <p>
     * 通过SQL层面的乐观锁实现：WHERE stock_count > 0 确保不会超卖。
     * 当库存为0时，WHERE条件不匹配，更新行数为0，表示扣减失败。
     * </p>
     *
     * @param id 秒杀商品ID
     * @return 影响行数，1=扣减成功，0=库存不足
     */
    @Update("UPDATE t_seckill_goods SET stock_count = stock_count - 1 WHERE id = #{id} AND stock_count > 0")
    int reduceStock(@Param("id") Long id);

    // TODO [功能缺失] 需要新增原子库存回滚方法，供 cancelOrder() 使用，解决并发竞态问题
    /**
     * 原子回滚库存（并发安全）
     *
     * 使用SQL原子自增，避免 read-modify-write 的竞态条件。
     * @param id 秒杀商品ID
     * @return 影响行数（通常为1）
     */

    @Update("UPDATE t_seckill_goods SET stock_count = stock_count + 1 WHERE id = #{id}")
    int increaseStock(@Param("id") Long id);

    /**
    * 原子性批量增加库存（管理员补充库存使用）
    *
    * @param id   秒杀商品ID
    * @param count  增加的库存数量
    * @return   影响行数
    */
    @Update("UPDATE t_seckill_goods SET stock_count = stock_count + #{count} WHERE id = #{id}")
    int addStockAtomic(@Param("id") Long id, @Param("count") Integer count);
}
