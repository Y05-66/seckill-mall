package com.seckill.mall.service;

import com.seckill.mall.entity.Goods;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    /**
     * 添加收藏
     */
    void addFavorite(Long userId, Long goodsId);

    /**
     * 取消收藏
     */
    void removeFavorite(Long userId, Long goodsId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorite(Long userId, Long goodsId);

    /**
     * 获取收藏列表
     */
    List<Goods> getFavoriteList(Long userId);

    /**
     * 获取收藏数量
     */
    int getFavoriteCount(Long userId);
}
