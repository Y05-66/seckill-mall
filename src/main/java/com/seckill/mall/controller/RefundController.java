package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.entity.Refund;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.RefundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "退款模块")
@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @ApiOperation("申请退款")
    @PostMapping("/apply")
    public Result<?> applyRefund(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Refund refund) {
        refundService.applyRefund(loginUser.getUserId(), refund);
        return Result.success();
    }

    @ApiOperation("我的退款列表")
    @GetMapping("/my")
    public Result<List<Refund>> getMyRefunds(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(refundService.getUserRefunds(loginUser.getUserId()));
    }

    @ApiOperation("所有退款列表（管理端）")
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Refund>> getAllRefunds() {
        return Result.success(refundService.getAllRefunds());
    }

    @ApiOperation("同意退款（管理端）")
    @PutMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> approveRefund(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        refundService.approveRefund(id, body.get("adminNote"));
        return Result.success();
    }

    @ApiOperation("拒绝退款（管理端）")
    @PutMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> rejectRefund(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        refundService.rejectRefund(id, body.get("adminNote"));
        return Result.success();
    }
}
