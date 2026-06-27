package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.entity.Address;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器
 */
@Api(tags = "收货地址模块")
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @ApiOperation("添加收货地址")
    @PostMapping
    public Result<?> addAddress(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Address address) {
        addressService.addAddress(loginUser.getUserId(), address);
        return Result.success();
    }

    @ApiOperation("获取收货地址列表")
    @GetMapping("/list")
    public Result<List<Address>> getAddressList(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(addressService.getAddressList(loginUser.getUserId()));
    }

    @ApiOperation("获取收货地址详情")
    @GetMapping("/{addressId}")
    public Result<Address> getAddress(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long addressId) {
        return Result.success(addressService.getAddress(loginUser.getUserId(), addressId));
    }

    @ApiOperation("更新收货地址")
    @PutMapping("/{addressId}")
    public Result<?> updateAddress(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long addressId,
            @RequestBody Address address) {
        addressService.updateAddress(loginUser.getUserId(), addressId, address);
        return Result.success();
    }

    @ApiOperation("删除收货地址")
    @DeleteMapping("/{addressId}")
    public Result<?> deleteAddress(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long addressId) {
        addressService.deleteAddress(loginUser.getUserId(), addressId);
        return Result.success();
    }

    @ApiOperation("设置默认地址")
    @PutMapping("/{addressId}/default")
    public Result<?> setDefault(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long addressId) {
        addressService.setDefault(loginUser.getUserId(), addressId);
        return Result.success();
    }

    @ApiOperation("获取默认地址")
    @GetMapping("/default")
    public Result<Address> getDefaultAddress(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(addressService.getDefaultAddress(loginUser.getUserId()));
    }
}
