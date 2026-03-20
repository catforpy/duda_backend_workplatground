package com.duda.user.api.service;

import com.duda.user.dto.userapikey.AddUserApiKeyReqDTO;
import com.duda.user.dto.userapikey.UserApiKeyDTO;

import java.util.List;

/**
 * 用户API密钥服务接口
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
public interface UserApiKeyService {

    /**
     * 添加用户API密钥
     */
    UserApiKeyDTO addUserApiKey(AddUserApiKeyReqDTO request);

    /**
     * 获取用户的所有API密钥
     */
    List<UserApiKeyDTO> listUserApiKeys(Long userId, Boolean includeInactive);

    /**
     * 获取用户的默认API密钥
     */
    UserApiKeyDTO getDefaultUserApiKey(Long userId);

    /**
     * 删除用户API密钥
     */
    Boolean deleteUserApiKey(Long keyId, Long userId);

    /**
     * 设置默认密钥
     */
    Boolean setDefaultApiKey(Long keyId, Long userId);

    /**
     * 更新密钥状态
     */
    Boolean updateApiKeyStatus(Long keyId, Long userId, Boolean active);

    /**
     * 根据用户名添加API密钥（测试方法）
     */
    UserApiKeyDTO addUserApiKeyByUsername(String username, String keyName, String keyType,
                                         String accessKeyId, String accessKeySecret);

    /**
     * 根据用户名获取API密钥（测试方法）
     */
    UserApiKeyDTO getUserApiKeyByUsername(String username);
}
