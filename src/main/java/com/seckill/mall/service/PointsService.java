package com.seckill.mall.service;

import com.seckill.mall.entity.PointsLog;
import com.seckill.mall.entity.UserPoints;

import java.util.List;

public interface PointsService {
    UserPoints getUserPoints(Long userId);
    void addPoints(Long userId, int points, String description, String orderNo);
    void usePoints(Long userId, int points, String description, String orderNo);
    List<PointsLog> getPointsLog(Long userId);
}
