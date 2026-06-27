package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.entity.Favorite;
import com.seckill.mall.entity.Goods;
import com.seckill.mall.mapper.FavoriteMapper;
import com.seckill.mall.mapper.GoodsMapper;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.FavoriteKey;
import com.seckill.mall.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务实现类
 * <p>
 * 处理商品收藏的添加、删除、查询等业务逻辑。
 * 使用Redis缓存减少数据库访问，收藏列表缓存5分钟，单品收藏状态缓存5分钟。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final GoodsMapper goodsMapper;
    private final RedisService redisService;

    /**
     * 添加商品收藏
     * <p>如果已收藏则直接返回，不会重复添加</p>
     *
     * @param userId  用户ID
     * @param goodsId 商品ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long userId, Long goodsId) {
        // 检查是否已收藏
        if (isFavorite(userId, goodsId)) {
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favorite.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(favorite);

        // 清除收藏列表缓存和单品收藏状态缓存
        redisService.delete(FavoriteKey.FAVORITE_LIST, String.valueOf(userId));
        redisService.delete(FavoriteKey.FAVORITE_CHECK, userId + ":" + goodsId);
    }

    /**
     * 取消商品收藏
     *
     * @param userId  用户ID
     * @param goodsId 商品ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long userId, Long goodsId) {
        favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getGoodsId, goodsId));

        // 清除收藏列表缓存和单品收藏状态缓存
        redisService.delete(FavoriteKey.FAVORITE_LIST, String.valueOf(userId));
        redisService.delete(FavoriteKey.FAVORITE_CHECK, userId + ":" + goodsId);
    }

    /**
     * 查询用户是否收藏了某商品
     * <p>优先从Redis缓存获取，减少数据库查询（商品详情页频繁调用）</p>
     *
     * @param userId  用户ID
     * @param goodsId 商品ID
     * @return true=已收藏, false=未收藏
     */
    @Override
    public boolean isFavorite(Long userId, Long goodsId) {
        String cacheKey = userId + ":" + goodsId;
        // 优先从Redis缓存获取
        Boolean cached = redisService.get(FavoriteKey.FAVORITE_CHECK, cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getGoodsId, goodsId));
        boolean result = count > 0;

        // 写入Redis缓存
        redisService.set(FavoriteKey.FAVORITE_CHECK, cacheKey, result);
        return result;
    }

    /**
     * 获取用户收藏的商品列表
     * <p>使用Redis缓存（5分钟TTL），按收藏时间倒序返回</p>
     *
     * @param userId 用户ID
     * @return 收藏的商品列表
     */
    @Override
    public List<Goods> getFavoriteList(Long userId) {
        // 优先从Redis缓存获取
        List<Goods> cached = redisService.get(FavoriteKey.FAVORITE_LIST, String.valueOf(userId));
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<Favorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreateTime));

        if (favorites.isEmpty()) {
            return List.of();
        }

        List<Long> goodsIds = favorites.stream()
                .map(Favorite::getGoodsId)
                .collect(Collectors.toList());

        List<Goods> result = goodsMapper.selectBatchIds(goodsIds);

        // 写入Redis缓存
        redisService.set(FavoriteKey.FAVORITE_LIST, String.valueOf(userId), result);
        return result;
    }

    /**
     * 获取用户收藏数量
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    @Override
    public int getFavoriteCount(Long userId) {
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId));
        return count.intValue();
    }
}
