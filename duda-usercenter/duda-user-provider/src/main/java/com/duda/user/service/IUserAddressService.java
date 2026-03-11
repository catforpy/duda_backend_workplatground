package com.duda.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.user.po.UserAddressPO;

import java.util.List;

/**
 * 用户地址Service
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
public interface IUserAddressService extends IService<UserAddressPO> {

    /**
     * 根据用户ID获取地址列表
     */
    List<UserAddressPO> listByUserId(Long userId);

    /**
     * 获取用户的默认地址
     */
    UserAddressPO getDefaultAddress(Long userId);

    /**
     * 设置默认地址
     */
    boolean setDefaultAddress(Long userId, Long addressId);
}
