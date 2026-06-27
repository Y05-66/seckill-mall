package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.entity.Notification;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息通知控制器
 * <p>
 * 处理用户消息通知相关的HTTP请求，包括查看通知、获取未读数量、标记已读。
 * 基础路径：/notification
 * </p>
 */
@Api(tags = "消息通知模块")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取当前用户的通知列表
     * <p>按创建时间倒序返回，最新的通知在前</p>
     */
    @ApiOperation("获取通知列表")
    @GetMapping("/list")
    public Result<List<Notification>> getNotifications(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(notificationService.getUserNotifications(loginUser.getUserId()));
    }

    /**
     * 获取当前用户的未读通知数量
     * <p>用于前端显示未读红点</p>
     */
    @ApiOperation("获取未读数量")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(notificationService.getUnreadCount(loginUser.getUserId()));
    }

    /**
     * 标记单条通知为已读
     *
     * @param id 通知ID
     */
    @ApiOperation("标记单条已读")
    @PutMapping("/{id}/read")
    public Result<?> markAsRead(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        notificationService.markAsRead(loginUser.getUserId(), id);
        return Result.success();
    }

    /**
     * 标记当前用户所有未读通知为已读
     */
    @ApiOperation("标记全部已读")
    @PutMapping("/read-all")
    public Result<?> markAllAsRead(@AuthenticationPrincipal LoginUser loginUser) {
        notificationService.markAllAsRead(loginUser.getUserId());
        return Result.success();
    }
}
