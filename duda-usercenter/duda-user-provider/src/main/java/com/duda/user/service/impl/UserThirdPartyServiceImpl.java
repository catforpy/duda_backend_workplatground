package com.duda.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.user.mapper.UserThirdPartyMapper;
import com.duda.user.po.UserThirdPartyPO;
import com.duda.user.service.IUserThirdPartyService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户第三方账号绑定Service实现
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Service
public class UserThirdPartyServiceImpl extends ServiceImpl<UserThirdPartyMapper, UserThirdPartyPO>
        implements IUserThirdPartyService {

    @Override
    public UserThirdPartyPO getByPlatformAndOpenId(String platform, String openid) {
        QueryWrapper<UserThirdPartyPO> wrapper = new QueryWrapper<>();
        wrapper.eq("platform", platform);
        wrapper.eq("openid", openid);
        wrapper.eq("status", 1); // 只查询已绑定的
        return getOne(wrapper);
    }

    @Override
    public List<UserThirdPartyPO> listByUserId(Long userId) {
        QueryWrapper<UserThirdPartyPO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("status", 1); // 只查询已绑定的
        return list(wrapper);
    }

    @Override
    public boolean bindThirdParty(Long userId, String platform, String openid, String unionid) {
        // 检查是否已经绑定
        UserThirdPartyPO existing = getByPlatformAndOpenId(platform, openid);
        if (existing != null) {
            // 已绑定，更新用户ID（可能是换绑）
            existing.setUserId(userId);
            existing.setStatus(1);
            existing.setUnbindTime(null);
            return updateById(existing);
        }

        // 新增绑定
        UserThirdPartyPO thirdParty = new UserThirdPartyPO();
        thirdParty.setUserId(userId);
        thirdParty.setPlatform(platform);
        thirdParty.setOpenid(openid);
        thirdParty.setUnionid(unionid);
        thirdParty.setBindTime(LocalDateTime.now());
        thirdParty.setStatus(1);
        return save(thirdParty);
    }

    @Override
    public boolean unbindThirdParty(Long userId, String platform) {
        QueryWrapper<UserThirdPartyPO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("platform", platform);
        wrapper.eq("status", 1);

        UserThirdPartyPO thirdParty = getOne(wrapper);
        if (thirdParty != null) {
            thirdParty.setStatus(0); // 标记为已解绑
            thirdParty.setUnbindTime(LocalDateTime.now());
            return updateById(thirdParty);
        }

        return false;
    }
}
