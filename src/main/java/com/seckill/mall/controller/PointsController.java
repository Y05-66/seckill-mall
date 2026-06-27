package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.entity.PointsLog;
import com.seckill.mall.entity.UserPoints;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.PointsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分控制器
 * <p>
 * 处理用户积分相关的HTTP请求，包括查看积分余额和积分变动记录。
 * 基础路径：/points
 * </p>
 */
@Api(tags = "积分模块")
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    /**
     * 获取当前用户的积分信息
     * <p>返回当前积分余额、累计获取、累计使用等信息</p>
     *
     * @param loginUser 当前登录用户
     * @return 用户积分信息
     */
    @ApiOperation("获取我的积分")
    @GetMapping("/my")
    public Result<UserPoints> getMyPoints(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(pointsService.getUserPoints(loginUser.getUserId()));
    }

    /**
     * 获取用户的积分变动日志
     * <p>按时间倒序返回积分的获取和使用记录</p>
     *
     * @param loginUser 当前登录用户
     * @return 积分日志列表
     */
    @ApiOperation("获取积分日志")
    @GetMapping("/log")
    public Result<List<PointsLog>> getPointsLog(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(pointsService.getPointsLog(loginUser.getUserId()));
    }
}
