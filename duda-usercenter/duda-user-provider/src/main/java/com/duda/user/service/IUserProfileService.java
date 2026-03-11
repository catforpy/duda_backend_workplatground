package com.duda.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.user.po.UserProfilePO;

/**
 * 用户扩展资料Service
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
public interface IUserProfileService extends IService<UserProfilePO> {

    /**
     * 根据用户ID获取扩展资料
     */
    UserProfilePO getByUserId(Long userId);

    /**
     * 保存或更新用户扩展资料
     */
    boolean saveOrUpdate(Long userId, UserProfilePO profile);
}
