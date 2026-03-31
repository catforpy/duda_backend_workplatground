package com.duda.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.user.mapper.UserProfileMapper;
import com.duda.user.po.UserProfilePO;
import com.duda.user.service.IUserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户扩展资料Service实现
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfilePO>
        implements IUserProfileService {

    @Override
    public UserProfilePO getByUserId(Long userId) {
        QueryWrapper<UserProfilePO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(Long userId, UserProfilePO profile) {
        UserProfilePO existing = getByUserId(userId);
        if (existing != null) {
            profile.setId(existing.getId());
            return updateById(profile);
        } else {
            profile.setUserId(userId);
            return save(profile);
        }
    }
}
