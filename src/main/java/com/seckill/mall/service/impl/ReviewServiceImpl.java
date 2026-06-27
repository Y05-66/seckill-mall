package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.entity.Review;
import com.seckill.mall.mapper.ReviewMapper;
import com.seckill.mall.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价服务实现类
 * <p>处理商品评价的添加和查询逻辑</p>
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;

    /**
     * 添加商品评价
     * <p>自动设置评价用户ID和创建时间</p>
     *
     * @param userId 评价用户ID
     * @param review 评价内容（包含goodsId、rating、content等）
     */
    @Override
    public void addReview(Long userId, Review review) {
        review.setUserId(userId);
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review);
    }

    /**
     * 查询商品的评价列表
     * <p>按创建时间倒序返回，最新的评价在前</p>
     *
     * @param goodsId 商品ID
     * @return 评价列表
     */
    @Override
    public List<Review> getGoodsReviews(Long goodsId) {
        return reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getGoodsId, goodsId)
                        .orderByDesc(Review::getCreateTime));
    }

    /**
     * 查询用户的评价列表
     * <p>按创建时间倒序返回</p>
     *
     * @param userId 用户ID
     * @return 评价列表
     */
    @Override
    public List<Review> getUserReviews(Long userId) {
        return reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getUserId, userId)
                        .orderByDesc(Review::getCreateTime));
    }
}
