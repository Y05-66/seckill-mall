package com.seckill.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.mall.entity.SeckillSuccess;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀成功记录表数据访问层接口
 * <p>继承MyBatis-Plus的BaseMapper，用于记录和查询秒杀成功信息</p>
 */
@Mapper
public interface SeckillSuccessMapper extends BaseMapper<SeckillSuccess> {
}
