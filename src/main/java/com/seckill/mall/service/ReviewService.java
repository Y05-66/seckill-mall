package com.seckill.mall.service;

import com.seckill.mall.entity.Review;

import java.util.List;

public interface ReviewService {
    void addReview(Long userId, Review review);
    List<Review> getGoodsReviews(Long goodsId);
    List<Review> getUserReviews(Long userId);
}
