package com.duda.user.rpc;

import com.duda.user.dto.userapikey.AddUserApiKeyReqDTO;
import com.duda.user.dto.userapikey.UserApiKeyDTO;

import java.util.List;

/**
 * 用户API密钥管理RPC接口
 * 提供用户API密钥的增删改查服务
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
public interface IUserApiKeyRpc {

    /**
     * 添加用户API密钥
     *
     * 功能说明：
     * - 接收明文密钥
     * - 使用AES加密后存储到数据库
     * - 自动验证密钥有效性
     *
     * @param request 添加请求（包含明文密钥）
     * @return API密钥信息
     */
    UserApiKeyDTO addUserApiKey(AddUserApiKeyReqDTO request);

    /**
     * 获取用户的所有API密钥
     *
     * @param userId 用户ID
     * @param includeInactive 是否包含禁用的密钥
     * @return API密钥列表
     */
    List<UserApiKeyDTO> listUserApiKeys(Long userId, Boolean includeInactive);

    /**
     * 获取用户的默认API密钥
     *
     * @param userId 用户ID
     * @return 默认API密钥（如果不存在返回null）
     */
    UserApiKeyDTO getDefaultUserApiKey(Long userId);

    /**
     * 根据密钥ID获取API密钥（用于其他服务调用）
     *
     * 注意：此方法返回的密钥已解密，仅供内部服务调用
     *
     * @param keyId 密钥ID
     * @return API密钥信息（包含解密后的明文密钥）
     */
    UserApiKeyDTO getUserApiKeyById(Long keyId);

    /**
     * 删除用户API密钥
     *
     * @param keyId 密钥ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    Boolean deleteUserApiKey(Long keyId, Long userId);

    /**
     * 设置默认密钥
     *
     * @param keyId 密钥ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean setDefaultApiKey(Long keyId, Long userId);

    /**
     * 禁用/启用密钥
     *
     * @param keyId 密钥ID
     * @param userId 用户ID
     * @param active 是否启用
     * @return 是否成功
     */
    Boolean updateApiKeyStatus(Long keyId, Long userId, Boolean active);

    // ==================== 测试方法 ====================

    /**
     * 根据用户名添加API密钥（测试方法）
     *
     * @param username 用户名
     * @param keyName 密钥名称
     * @param keyType 密钥类型
     * @param accessKeyId AccessKey ID（明文）
     * @param accessKeySecret AccessKey Secret（明文）
     * @return API密钥信息
     */
    UserApiKeyDTO addUserApiKeyByUsername(String username, String keyName, String keyType,
                                         String accessKeyId, String accessKeySecret);

    /**
     * 根据用户名获取API密钥（测试方法）
     *
     * 返回解密后的明文密钥，用于验证加密/解密逻辑
     *
     * @param username 用户名
     * @return API密钥信息（包含明文密钥）
     */
    UserApiKeyDTO getUserApiKeyByUsername(String username);
}
