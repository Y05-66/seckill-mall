package com.seckill.mall.service;

import com.seckill.mall.common.BusinessException;
import com.seckill.mall.dto.GoodsDTO;
import com.seckill.mall.dto.SeckillGoodsDTO;
import com.seckill.mall.entity.SeckillGoods;

import java.util.List;

/**
 * 商品业务服务接口
 * <p>定义普通商品和秒杀商品的查询方法</p>
 */
public interface GoodsService {

    /**
     * 获取所有上架的普通商品列表
     *
     * @return 商品DTO列表
     */
    List<GoodsDTO> listGoods();

    /**
     * 获取普通商品详情
     *
     * @param goodsId 商品ID
     * @return 商品详情DTO
     * @throws BusinessException 商品不存在时抛出异常
     */
    GoodsDTO getGoodsDetail(Long goodsId);

    /**
     * 获取所有秒杀商品列表（含活动状态和倒计时）
     *
     * @return 秒杀商品DTO列表
     */
    List<SeckillGoodsDTO> listSeckillGoods();

    /**
     * 获取秒杀商品详情
     *
     * @param seckillGoodsId 秒杀商品ID
     * @return 秒杀商品详情DTO
     * @throws BusinessException 秒杀商品不存在时抛出异常
     */
    SeckillGoodsDTO getSeckillGoodsDetail(Long seckillGoodsId);

    /**
     * 新增秒杀商品
     *
     * @param goods 秒杀商品实体
     * @return 秒杀商品ID
     */
    Long addSeckillGoods(SeckillGoods goods);

    /**
     * 修改秒杀商品
     *
     * @param id 秒杀商品ID
     * @param goods 秒杀商品实体
     */
    void updateSeckillGoods(Long id, SeckillGoods goods);

    /**
     * 删除秒杀商品
     *
     * @param id 秒杀商品ID
     */
    void deleteSeckillGoods(Long id);

    /**
     * 秒杀商品补充库存
     *
     * @param id 秒杀商品ID
     * @param count 补充数量
     */
    void addStock(Long id, Integer count);
}
