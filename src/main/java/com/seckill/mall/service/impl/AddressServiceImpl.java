package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.entity.Address;
import com.seckill.mall.mapper.AddressMapper;
import com.seckill.mall.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收货地址服务实现类
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(Long userId, Address address) {
        address.setUserId(userId);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        // 如果是第一个地址，自动设为默认
        Long count = addressMapper.selectCount(
                new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        if (count == 0) {
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }

        addressMapper.insert(address);
    }

    @Override
    public List<Address> getAddressList(Long userId) {
        return addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getUpdateTime));
    }

    @Override
    public Address getAddress(Long userId, Long addressId) {
        return addressMapper.selectOne(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getId, addressId)
                        .eq(Address::getUserId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, Long addressId, Address address) {
        Address existing = getAddress(userId, addressId);
        if (existing == null) {
            throw new BusinessException("地址不存在");
        }

        address.setId(addressId);
        address.setUserId(userId);
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long userId, Long addressId) {
        Address existing = getAddress(userId, addressId);
        if (existing == null) {
            throw new BusinessException("地址不存在");
        }

        addressMapper.deleteById(addressId);

        // 如果删除的是默认地址，将最新的地址设为默认
        if (existing.getIsDefault() == 1) {
            Address latest = addressMapper.selectOne(
                    new LambdaQueryWrapper<Address>()
                            .eq(Address::getUserId, userId)
                            .orderByDesc(Address::getUpdateTime)
                            .last("LIMIT 1"));
            if (latest != null) {
                latest.setIsDefault(1);
                addressMapper.updateById(latest);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long addressId) {
        // 先取消所有默认
        addressMapper.update(null,
                new LambdaUpdateWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .set(Address::getIsDefault, 0));

        // 再设置指定地址为默认
        Address address = getAddress(userId, addressId);
        if (address != null) {
            address.setIsDefault(1);
            address.setUpdateTime(LocalDateTime.now());
            addressMapper.updateById(address);
        }
    }

    @Override
    public Address getDefaultAddress(Long userId) {
        return addressMapper.selectOne(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .eq(Address::getIsDefault, 1));
    }
}
