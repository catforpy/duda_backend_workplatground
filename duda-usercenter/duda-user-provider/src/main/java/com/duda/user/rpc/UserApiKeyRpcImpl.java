package com.duda.user.rpc;

import com.duda.user.dto.userapikey.AddUserApiKeyReqDTO;
import com.duda.user.dto.userapikey.UserApiKeyDTO;
import com.duda.user.service.UserApiKeyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 用户API密钥管理RPC实现
 * 暴露Dubbo服务供其他服务调用
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@DubboService(version = "1.0.0", group = "USER_GROUP", timeout = 30000)
public class UserApiKeyRpcImpl implements IUserApiKeyRpc {

    @Resource
    private UserApiKeyService userApiKeyService;

    @Override
    public UserApiKeyDTO addUserApiKey(AddUserApiKeyReqDTO request) {
        log.info("【RPC】添加用户API密钥: username={}", request.getUsername());
        return userApiKeyService.addUserApiKey(request);
    }

    @Override
    public List<UserApiKeyDTO> listUserApiKeys(Long userId, Boolean includeInactive) {
        log.info("【RPC】查询用户API密钥列表: userId={}", userId);
        return userApiKeyService.listUserApiKeys(userId, includeInactive);
    }

    @Override
    public UserApiKeyDTO getDefaultUserApiKey(Long userId) {
        log.info("【RPC】查询用户默认API密钥: userId={}", userId);
        return userApiKeyService.getDefaultUserApiKey(userId);
    }

    @Override
    public UserApiKeyDTO getUserApiKeyById(Long keyId) {
        log.info("【RPC】查询API密钥详情（内部调用）: keyId={}", keyId);
        return userApiKeyService.getUserApiKeyById(keyId);
    }

    @Override
    public Boolean deleteUserApiKey(Long keyId, Long userId) {
        log.info("【RPC】删除用户API密钥: keyId={}, userId={}", keyId, userId);
        return userApiKeyService.deleteUserApiKey(keyId, userId);
    }

    @Override
    public Boolean setDefaultApiKey(Long keyId, Long userId) {
        log.info("【RPC】设置默认API密钥: keyId={}, userId={}", keyId, userId);
        return userApiKeyService.setDefaultApiKey(keyId, userId);
    }

    @Override
    public Boolean updateApiKeyStatus(Long keyId, Long userId, Boolean active) {
        log.info("【RPC】更新API密钥状态: keyId={}, userId={}, active={}", keyId, userId, active);
        return userApiKeyService.updateApiKeyStatus(keyId, userId, active);
    }

    @Override
    public UserApiKeyDTO addUserApiKeyByUsername(String username, String keyName, String keyType,
                                                 String accessKeyId, String accessKeySecret) {
        log.info("【RPC】根据用户名添加API密钥（测试）: username={}", username);
        return userApiKeyService.addUserApiKeyByUsername(username, keyName, keyType, accessKeyId, accessKeySecret);
    }

    @Override
    public UserApiKeyDTO getUserApiKeyByUsername(String username) {
        log.info("【RPC】根据用户名获取API密钥（测试）: username={}", username);
        return userApiKeyService.getUserApiKeyByUsername(username);
    }
}
