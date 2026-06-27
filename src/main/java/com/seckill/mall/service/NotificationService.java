package com.seckill.mall.service;

import com.seckill.mall.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Long userId, String title, String content, String type, String refId);
    List<Notification> getUserNotifications(Long userId);
    int getUnreadCount(Long userId);
    void markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
}
