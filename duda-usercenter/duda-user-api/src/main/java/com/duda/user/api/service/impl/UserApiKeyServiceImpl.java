package com.duda.user.api.service.impl;

import com.duda.user.api.service.UserApiKeyService;
import com.duda.user.dto.userapikey.AddUserApiKeyReqDTO;
import com.duda.user.dto.userapikey.UserApiKeyDTO;
import com.duda.user.rpc.IUserApiKeyRpc;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户API密钥服务实现
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@Service
public class UserApiKeyServiceImpl implements UserApiKeyService {

    @DubboReference(version = "1.0.0", group = "USER_GROUP", check = false)
    private IUserApiKeyRpc userApiKeyRpc;

    @Override
    public UserApiKeyDTO addUserApiKey(AddUserApiKeyReqDTO request) {
        log.info("【API服务】添加用户API密钥: username={}", request.getUsername());
        return userApiKeyRpc.addUserApiKey(request);
    }

    @Override
    public List<UserApiKeyDTO> listUserApiKeys(Long userId, Boolean includeInactive) {
        log.info("【API服务】查询用户API密钥列表: userId={}", userId);
        return userApiKeyRpc.listUserApiKeys(userId, includeInactive);
    }

    @Override
    public UserApiKeyDTO getDefaultUserApiKey(Long userId) {
        log.info("【API服务】查询用户默认API密钥: userId={}", userId);
        return userApiKeyRpc.getDefaultUserApiKey(userId);
    }

    @Override
    public Boolean deleteUserApiKey(Long keyId, Long userId) {
        log.info("【API服务】删除用户API密钥: keyId={}, userId={}", keyId, userId);
        return userApiKeyRpc.deleteUserApiKey(keyId, userId);
    }

    @Override
    public Boolean setDefaultApiKey(Long keyId, Long userId) {
        log.info("【API服务】设置默认API密钥: keyId={}, userId={}", keyId, userId);
        return userApiKeyRpc.setDefaultApiKey(keyId, userId);
    }

    @Override
    public Boolean updateApiKeyStatus(Long keyId, Long userId, Boolean active) {
        log.info("【API服务】更新API密钥状态: keyId={}, userId={}, active={}", keyId, userId, active);
        return userApiKeyRpc.updateApiKeyStatus(keyId, userId, active);
    }

    @Override
    public UserApiKeyDTO addUserApiKeyByUsername(String username, String keyName, String keyType,
                                                 String accessKeyId, String accessKeySecret) {
        log.info("【API服务】根据用户名添加API密钥（测试）: username={}", username);
        return userApiKeyRpc.addUserApiKeyByUsername(username, keyName, keyType, accessKeyId, accessKeySecret);
    }

    @Override
    public UserApiKeyDTO getUserApiKeyByUsername(String username) {
        log.info("【API服务】根据用户名获取API密钥（测试）: username={}", username);
        return userApiKeyRpc.getUserApiKeyByUsername(username);
    }
}
