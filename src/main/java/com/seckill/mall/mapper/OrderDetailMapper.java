package com.seckill.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.mall.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细表数据访问层接口
 * <p>继承MyBatis-Plus的BaseMapper，自动获得CRUD通用方法</p>
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
