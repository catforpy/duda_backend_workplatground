package com.duda.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.user.mapper.UserAddressMapper;
import com.duda.user.po.UserAddressPO;
import com.duda.user.service.IUserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户地址Service实现
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddressPO>
        implements IUserAddressService {

    @Override
    public List<UserAddressPO> listByUserId(Long userId) {
        QueryWrapper<UserAddressPO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("is_default");
        wrapper.orderByDesc("create_time");
        return list(wrapper);
    }

    @Override
    public UserAddressPO getDefaultAddress(Long userId) {
        QueryWrapper<UserAddressPO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("is_default", 1);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long userId, Long addressId) {
        // 1. 取消该用户的所有默认地址
        QueryWrapper<UserAddressPO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("is_default", 1);
        List<UserAddressPO> defaultAddresses = list(wrapper);
        for (UserAddressPO address : defaultAddresses) {
            address.setIsDefault(0);
            updateById(address);
        }

        // 2. 设置新的默认地址
        UserAddressPO newDefault = getById(addressId);
        if (newDefault != null && newDefault.getUserId().equals(userId)) {
            newDefault.setIsDefault(1);
            return updateById(newDefault);
        }

        return false;
    }
}
