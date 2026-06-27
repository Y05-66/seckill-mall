package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.mall.entity.Notification;
import com.seckill.mall.mapper.NotificationMapper;
import com.seckill.mall.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知服务实现类
 * <p>处理系统消息的发送、查询、已读标记等操作</p>
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    /**
     * 发送系统通知
     * <p>创建一条新的通知消息，默认状态为未读</p>
     *
     * @param userId  接收用户ID
     * @param title   通知标题
     * @param content 通知内容
     * @param type    通知类型（如 order、coupon、system）
     * @param refId   关联业务ID（如订单号、优惠券ID）
     */
    @Override
    public void sendNotification(Long userId, String title, String content, String type, String refId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notification.setRefId(refId);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    /**
     * 获取用户的通知列表
     * <p>按创建时间倒序返回，最新的通知在前</p>
     *
     * @param userId 用户ID
     * @return 通知列表
     */
    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime));
    }

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Override
    public int getUnreadCount(Long userId) {
        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
        return count.intValue();
    }

    /**
     * 标记单条通知为已读
     * <p>同时校验通知归属，防止标记其他用户的通知</p>
     *
     * @param userId         用户ID
     * @param notificationId 通知ID
     */
    @Override
    public void markAsRead(Long userId, Long notificationId) {
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getId, notificationId)
                        .eq(Notification::getUserId, userId)
                        .set(Notification::getIsRead, 1));
    }

    /**
     * 标记用户所有未读通知为已读
     *
     * @param userId 用户ID
     */
    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
                        .set(Notification::getIsRead, 1));
    }
}
