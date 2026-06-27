package com.seckill.mall.service;

import com.seckill.mall.entity.Address;

import java.util.List;

/**
 * 收货地址服务接口
 */
public interface AddressService {

    /**
     * 添加收货地址
     */
    void addAddress(Long userId, Address address);

    /**
     * 获取用户收货地址列表
     */
    List<Address> getAddressList(Long userId);

    /**
     * 获取收货地址详情
     */
    Address getAddress(Long userId, Long addressId);

    /**
     * 更新收货地址
     */
    void updateAddress(Long userId, Long addressId, Address address);

    /**
     * 删除收货地址
     */
    void deleteAddress(Long userId, Long addressId);

    /**
     * 设置默认地址
     */
    void setDefault(Long userId, Long addressId);

    /**
     * 获取默认地址
     */
    Address getDefaultAddress(Long userId);
}
