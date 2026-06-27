package com.seckill.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.Result;
import com.seckill.mall.entity.OperationLog;
import com.seckill.mall.mapper.OperationLogMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "操作日志")
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;

    @ApiOperation("获取操作日志列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<OperationLog>> getLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (operation != null && !operation.isEmpty()) {
            wrapper.like(OperationLog::getOperation, operation);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        wrapper.last("LIMIT 200");
        return Result.success(operationLogMapper.selectList(wrapper));
    }
}
