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
    public List<UserApiKeyDTO> listUserApiKeys(Long tenantId, Long userId, Boolean includeInactive) {
        log.info("【RPC】查询用户API密钥列表: tenantId={}, userId={}", tenantId, userId);
        return userApiKeyService.listUserApiKeys(tenantId, userId, includeInactive);
    }

    @Override
    public UserApiKeyDTO getDefaultUserApiKey(Long tenantId, Long userId) {
        log.info("【RPC】查询用户默认API密钥: tenantId={}, userId={}", tenantId, userId);
        return userApiKeyService.getDefaultUserApiKey(tenantId, userId);
    }

    @Override
    public UserApiKeyDTO getUserApiKeyById(Long tenantId, Long keyId) {
        log.info("【RPC】查询API密钥详情（内部调用）: tenantId={}, keyId={}", tenantId, keyId);
        return userApiKeyService.getUserApiKeyById(tenantId, keyId);
    }

    @Override
    public Boolean deleteUserApiKey(Long tenantId, Long keyId, Long userId) {
        log.info("【RPC】删除用户API密钥: tenantId={}, keyId={}, userId={}", tenantId, keyId, userId);
        return userApiKeyService.deleteUserApiKey(tenantId, keyId, userId);
    }

    @Override
    public Boolean setDefaultApiKey(Long tenantId, Long keyId, Long userId) {
        log.info("【RPC】设置默认API密钥: tenantId={}, keyId={}, userId={}", tenantId, keyId, userId);
        return userApiKeyService.setDefaultApiKey(tenantId, keyId, userId);
    }

    @Override
    public Boolean updateApiKeyStatus(Long tenantId, Long keyId, Long userId, Boolean active) {
        log.info("【RPC】更新API密钥状态: tenantId={}, keyId={}, userId={}, active={}", tenantId, keyId, userId, active);
        return userApiKeyService.updateApiKeyStatus(tenantId, keyId, userId, active);
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
