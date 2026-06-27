package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.entity.PointsLog;
import com.seckill.mall.entity.UserPoints;
import com.seckill.mall.mapper.PointsLogMapper;
import com.seckill.mall.mapper.UserPointsMapper;
import com.seckill.mall.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分服务实现类
 * <p>处理用户积分的查询、增加、使用和积分日志记录</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final UserPointsMapper userPointsMapper;
    private final PointsLogMapper pointsLogMapper;

    /**
     * 获取用户积分信息
     * <p>如果用户没有积分记录，自动创建一条初始记录（积分=0）</p>
     *
     * @param userId 用户ID
     * @return 用户积分信息
     */
    @Override
    public UserPoints getUserPoints(Long userId) {
        UserPoints points = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId));
        if (points == null) {
            points = new UserPoints();
            points.setUserId(userId);
            points.setPoints(0);
            points.setTotalEarned(0);
            points.setTotalUsed(0);
            points.setCreateTime(LocalDateTime.now());
            points.setUpdateTime(LocalDateTime.now());
            try {
                userPointsMapper.insert(points);
            } catch (Exception e) {
                // 并发插入时唯一约束冲突，重新查询
                points = userPointsMapper.selectOne(
                        new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId));
            }
        }
        return points;
    }

    /**
     * 增加用户积分
     * <p>使用原子SQL增加积分，同时记录积分日志</p>
     *
     * @param userId      用户ID
     * @param points      增加的积分数量（必须为正数）
     * @param description 积分变动描述
     * @param orderNo     关联订单号（可选）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, int points, String description, String orderNo) {
        // 确保用户积分记录存在
        getUserPoints(userId);

        // 原子更新积分
        userPointsMapper.update(null,
                new LambdaUpdateWrapper<UserPoints>()
                        .eq(UserPoints::getUserId, userId)
                        .setSql(String.format("points = points + %d", points))
                        .setSql(String.format("total_earned = total_earned + %d", points))
                        .set(UserPoints::getUpdateTime, LocalDateTime.now()));

        // 记录积分日志
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPoints(points);
        log.setType("earn");
        log.setDescription(description);
        log.setOrderNo(orderNo);
        log.setCreateTime(LocalDateTime.now());
        pointsLogMapper.insert(log);
    }

    /**
     * 使用（扣减）用户积分
     * <p>使用原子SQL扣减积分，只有积分足够时才能成功扣减</p>
     *
     * @param userId      用户ID
     * @param points      使用的积分数量（必须为正数）
     * @param description 积分变动描述
     * @param orderNo     关联订单号（可选）
     * @throws BusinessException 积分不足时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void usePoints(Long userId, int points, String description, String orderNo) {
        // 原子扣减积分（只有积分足够时才能成功）
        int affected = userPointsMapper.update(null,
                new LambdaUpdateWrapper<UserPoints>()
                        .eq(UserPoints::getUserId, userId)
                        .ge(UserPoints::getPoints, points)
                        .setSql(String.format("points = points - %d", points))
                        .setSql(String.format("total_used = total_used + %d", points))
                        .set(UserPoints::getUpdateTime, LocalDateTime.now()));
        if (affected == 0) {
            throw new BusinessException("积分不足");
        }

        // 记录积分日志
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPoints(-points);
        log.setType("use");
        log.setDescription(description);
        log.setOrderNo(orderNo);
        log.setCreateTime(LocalDateTime.now());
        pointsLogMapper.insert(log);
    }

    /**
     * 查询用户积分变动日志
     * <p>按创建时间倒序返回</p>
     *
     * @param userId 用户ID
     * @return 积分日志列表
     */
    @Override
    public List<PointsLog> getPointsLog(Long userId) {
        return pointsLogMapper.selectList(
                new LambdaQueryWrapper<PointsLog>()
                        .eq(PointsLog::getUserId, userId)
                        .orderByDesc(PointsLog::getCreateTime));
    }
}
