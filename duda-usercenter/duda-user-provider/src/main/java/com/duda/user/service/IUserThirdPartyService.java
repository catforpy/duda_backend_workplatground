package com.duda.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.user.po.UserThirdPartyPO;

/**
 * 用户第三方账号绑定Service
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
public interface IUserThirdPartyService extends IService<UserThirdPartyPO> {

    /**
     * 根据平台和OpenID查询绑定关系
     */
    UserThirdPartyPO getByPlatformAndOpenId(String platform, String openid);

    /**
     * 根据用户ID获取所有绑定的第三方账号
     */
    java.util.List<UserThirdPartyPO> listByUserId(Long userId);

    /**
     * 绑定第三方账号
     */
    boolean bindThirdParty(Long userId, String platform, String openid, String unionid);

    /**
     * 解绑第三方账号
     */
    boolean unbindThirdParty(Long userId, String platform);
}
