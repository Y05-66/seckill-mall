package com.seckill.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.mall.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品表数据访问层接口
 * <p>继承MyBatis-Plus的BaseMapper，自动获得CRUD通用方法</p>
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}
