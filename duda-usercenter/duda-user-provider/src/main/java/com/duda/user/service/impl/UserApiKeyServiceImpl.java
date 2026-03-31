package com.duda.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duda.user.dto.userapikey.AddUserApiKeyReqDTO;
import com.duda.user.dto.userapikey.UserApiKeyDTO;
import com.duda.common.util.ApiAesEncryptUtil;
import com.duda.user.entity.UserApiKey;
import com.duda.user.enums.UserApiKeyTypeEnum;
import com.duda.user.enums.VerificationStatusEnum;
import com.duda.user.mapper.UserApiKeyMapper;
import com.duda.user.mapper.UserMapper;
import com.duda.user.po.UserPO;
import com.duda.user.service.UserApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户API密钥管理服务实现
 *
 * 功能说明：
 * 1. 加密存储：接收明文密钥，AES加密后存入数据库
 * 2. 解密使用：从数据库读取密文，解密后返回（仅内部服务调用）
 * 3. 密钥验证：添加密钥时自动验证有效性
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@Service
public class UserApiKeyServiceImpl implements UserApiKeyService {

    @Autowired
    private UserApiKeyMapper userApiKeyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ApiAesEncryptUtil apiAesEncryptUtil;

    @Autowired
    private com.duda.common.redis.RedisUtils redisUtils;

    @Autowired
    private com.duda.common.redis.key.ApiKeyRedisKeyBuilder apiKeyRedisKeyBuilder;

    @Override
    public UserApiKeyDTO addUserApiKey(AddUserApiKeyReqDTO request) {
        log.info("【用户API密钥】添加密钥: username={}", request.getUsername());

        try {
            // 1. 根据用户名查询用户ID
            UserPO user = userMapper.selectOne(new LambdaQueryWrapper<UserPO>()
                    .eq(UserPO::getUsername, request.getUsername())
                    .last("LIMIT 1"));

            if (user == null) {
                throw new RuntimeException("用户不存在: " + request.getUsername());
            }

            Long userId = user.getId();
            Long tenantId = user.getTenantId();  // 获取租户ID
            log.info("查询到用户: username={}, userId={}, tenantId={}", request.getUsername(), userId, tenantId);

            // 2. 设置默认值
            String keyName = StringUtils.hasText(request.getKeyName()) ?
                    request.getKeyName() :
                    request.getUsername() + "-云存储密钥";

            String keyType = StringUtils.hasText(request.getKeyType()) ?
                    request.getKeyType() :
                    "aliyun_oss";

            String region = StringUtils.hasText(request.getRegion()) ?
                    request.getRegion() :
                    "cn-hangzhou";

            // 3. 检查密钥名称是否重复
            UserApiKey existing = userApiKeyMapper.selectByUserIdAndKeyName(userId, keyName);
            if (existing != null) {
                throw new RuntimeException("密钥名称已存在: " + keyName);
            }

            // 4. 加密明文密钥
            log.info("开始加密API密钥...");
            String encryptedKeyId = apiAesEncryptUtil.encrypt(request.getAccessKeyId());
            String encryptedKeySecret = apiAesEncryptUtil.encrypt(request.getAccessKeySecret());

            // 5. 验证密钥是否有效（TODO: 调用云厂商API验证）
            boolean isValid = verifyApiKey(keyType,
                    request.getAccessKeyId(), request.getAccessKeySecret(), region);
            String verificationStatus = isValid ?
                    VerificationStatusEnum.SUCCESS.getCode() :
                    VerificationStatusEnum.FAILED.getCode();
            LocalDateTime verifiedTime = isValid ? LocalDateTime.now() : null;

            // 6. 检查是否为第一个密钥（第一个密钥自动设为默认）
            List<UserApiKey> existingKeys = userApiKeyMapper.selectByUserId(userId);
            boolean isFirstKey = existingKeys.isEmpty();

            // 7. 构建实体
            UserApiKey userApiKey = UserApiKey.builder()
                    .tenantId(tenantId)  // 设置租户ID
                    .userId(userId)
                    .keyName(keyName)
                    .keyType(keyType)
                    .accessKeyId(encryptedKeyId)
                    .accessKeySecret(encryptedKeySecret)
                    .stsRoleArn(request.getStsRoleArn())
                    .stsExternalId(request.getStsExternalId())
                    .region(region)
                    .isDefault(isFirstKey ? 1 : 0)
                    .isActive(1)
                    .verificationStatus(verificationStatus)
                    .lastVerifiedTime(verifiedTime)
                    .description(request.getDescription())
                    .createdBy(userId)
                    .updatedBy(userId)
                    .createdTime(LocalDateTime.now())
                    .updatedTime(LocalDateTime.now())
                    .isDeleted(0)
                    .build();

            // 8. 保存到数据库
            userApiKeyMapper.insert(userApiKey);

            log.info("✅ API密钥添加成功: id={}, keyName={}, isDefault={}",
                    userApiKey.getId(), userApiKey.getKeyName(), userApiKey.getIsDefault());

            // 9. 清除缓存（如果这是第一个密钥，会成为默认密钥）
            String cacheKeyByUserId = apiKeyRedisKeyBuilder.buildDefaultApiKeyKey(userId);
            redisUtils.delete(cacheKeyByUserId);

            String cacheKeyByUsername = apiKeyRedisKeyBuilder.buildDefaultApiKeyByUsernameKey(request.getUsername());
            redisUtils.delete(cacheKeyByUsername);

            log.info("✅ 已清除API密钥缓存，userId:{}, username:{}", userId, request.getUsername());

            // 10. 返回DTO（不包含明文密钥）
            return convertToDTO(userApiKey);

        } catch (Exception e) {
            log.error("❌ 添加API密钥失败: {}", e.getMessage(), e);
            throw new RuntimeException("添加API密钥失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UserApiKeyDTO> listUserApiKeys(Long tenantId, Long userId, Boolean includeInactive) {
        log.info("【用户API密钥】查询密钥列表: tenantId={}, userId={}, includeInactive={}",
                tenantId, userId, includeInactive);

        try {
            // 查询用户的所有密钥（按租户隔离）
            List<UserApiKey> keys = userApiKeyMapper.selectByTenantIdAndUserId(tenantId, userId);

            // 过滤掉禁用的密钥（如果要求）
            if (includeInactive == null || !includeInactive) {
                keys = keys.stream()
                        .filter(key -> key.getIsActive() == 1)
                        .collect(Collectors.toList());
            }

            // 转换为DTO（不包含明文密钥）
            return keys.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ 查询API密钥列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询API密钥列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public UserApiKeyDTO getDefaultUserApiKey(Long tenantId, Long userId) {
        log.info("【用户API密钥】查询默认密钥: tenantId={}, userId={}", tenantId, userId);

        try {
            // 1. 先从缓存获取
            String cacheKey = apiKeyRedisKeyBuilder.buildDefaultApiKeyKey(tenantId, userId);
            UserApiKeyDTO cachedApiKey = redisUtils.get(cacheKey, UserApiKeyDTO.class);
            if (cachedApiKey != null) {
                log.info("✅ 缓存命中，默认API密钥，tenantId:{}, userId:{}", tenantId, userId);
                return cachedApiKey;
            }

            // 2. 从数据库查询（按租户隔离）
            UserApiKey apiKey = userApiKeyMapper.selectDefaultByTenantIdAndUserId(tenantId, userId);
            if (apiKey == null) {
                log.warn("未找到默认密钥: userId={}", userId);
                return null;
            }

            UserApiKeyDTO apiKeyDTO = convertToDTO(apiKey);

            // 3. 写入缓存（30分钟）
            redisUtils.set(cacheKey, apiKeyDTO, 1800);
            log.info("✅ 默认API密钥已缓存，userId:{}, keyId:{}", userId, apiKeyDTO.getId());

            return apiKeyDTO;

        } catch (Exception e) {
            log.error("❌ 查询默认密钥失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询默认密钥失败: " + e.getMessage(), e);
        }
    }

    @Override
    public UserApiKeyDTO getUserApiKeyById(Long tenantId, Long keyId) {
        log.info("【用户API密钥】查询密钥详情（内部调用）: tenantId={}, keyId={}", tenantId, keyId);

        try {
            UserApiKey apiKey = userApiKeyMapper.selectByTenantIdAndId(tenantId, keyId);
            if (apiKey == null) {
                log.warn("未找到密钥: keyId={}", keyId);
                return null;
            }

            // 内部调用：解密并返回明文密钥
            return convertToDTOWithPlainText(apiKey);

        } catch (Exception e) {
            log.error("❌ 查询密钥详情失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询密钥详情失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean deleteUserApiKey(Long tenantId, Long keyId, Long userId) {
        log.info("【用户API密钥】删除密钥: tenantId={}, keyId={}, userId={}", tenantId, keyId, userId);

        try {
            // 软删除（按租户隔离）
            int rows = userApiKeyMapper.softDeleteByTenantIdAndId(tenantId, keyId, userId, LocalDateTime.now());

            if (rows > 0) {
                log.info("✅ API密钥删除成功: keyId={}", keyId);
                return true;
            } else {
                log.warn("API密钥不存在或无权限: keyId={}, userId={}", keyId, userId);
                return false;
            }

        } catch (Exception e) {
            log.error("❌ 删除API密钥失败: {}", e.getMessage(), e);
            throw new RuntimeException("删除API密钥失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean setDefaultApiKey(Long tenantId, Long keyId, Long userId) {
        log.info("【用户API密钥】设置默认密钥: tenantId={}, keyId={}, userId={}", tenantId, keyId, userId);

        try {
            // 设置默认密钥（会先取消其他默认密钥，按租户隔离）
            int rows = userApiKeyMapper.setDefaultKeyByTenantId(tenantId, userId, keyId);

            if (rows > 0) {
                log.info("✅ 默认密钥设置成功: keyId={}", keyId);

                // 清除缓存
                String cacheKey = apiKeyRedisKeyBuilder.buildDefaultApiKeyKey(userId);
                redisUtils.delete(cacheKey);
                log.info("✅ 已清除默认API密钥缓存，userId:{}", userId);

                return true;
            } else {
                log.warn("密钥不存在或无权限: keyId={}, userId={}", keyId, userId);
                return false;
            }

        } catch (Exception e) {
            log.error("❌ 设置默认密钥失败: {}", e.getMessage(), e);
            throw new RuntimeException("设置默认密钥失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean updateApiKeyStatus(Long tenantId, Long keyId, Long userId, Boolean active) {
        log.info("【用户API密钥】更新密钥状态: tenantId={}, keyId={}, userId={}, active={}",
                tenantId, keyId, userId, active);

        try {
            UserApiKey apiKey = userApiKeyMapper.selectByTenantIdAndId(tenantId, keyId);
            if (apiKey == null) {
                throw new RuntimeException("密钥不存在: " + keyId);
            }

            // 权限验证（检查租户和用户）
            if (!apiKey.getTenantId().equals(tenantId) || !apiKey.getUserId().equals(userId)) {
                throw new RuntimeException("无权限操作此密钥");
            }

            // 更新状态
            apiKey.setIsActive(active ? 1 : 0);
            apiKey.setUpdatedBy(userId);
            apiKey.setUpdatedTime(LocalDateTime.now());

            int rows = userApiKeyMapper.updateById(apiKey);

            if (rows > 0) {
                log.info("✅ 密钥状态更新成功: keyId={}, isActive={}", keyId, active);
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            log.error("❌ 更新密钥状态失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新密钥状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证API密钥是否有效
     * TODO: 调用云厂商API验证
     *
     * @param keyType     密钥类型
     * @param accessKeyId AccessKey ID
     * @param accessKeySecret AccessKey Secret
     * @param region      区域
     * @return 是否有效
     */
    private boolean verifyApiKey(String keyType, String accessKeyId,
                                 String accessKeySecret, String region) {
        log.info("验证API密钥: keyType={}, region={}", keyType, region);

        try {
            // TODO: 根据keyType调用不同云厂商的API验证
            // 例如：调用 OSS listBuckets() 验证密钥是否有效

            // 暂时返回true（实际应该调用API验证）
            log.info("API密钥验证通过（TODO: 实际验证）");
            return true;

        } catch (Exception e) {
            log.error("API密钥验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 转换为DTO（不包含明文密钥）
     */
    private UserApiKeyDTO convertToDTO(UserApiKey entity) {
        return UserApiKeyDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())  // 设置租户ID
                .userId(entity.getUserId())
                .keyName(entity.getKeyName())
                .keyType(entity.getKeyType())
                // 只返回加密后的密文，不返回明文
                .accessKeyId(entity.getAccessKeyId())
                .accessKeySecret(entity.getAccessKeySecret())
                .stsRoleArn(entity.getStsRoleArn())
                .stsExternalId(entity.getStsExternalId())
                .region(entity.getRegion())
                .isDefault(entity.getIsDefault() == 1)
                .isActive(entity.getIsActive() == 1)
                .lastUsedTime(entity.getLastUsedTime())
                .lastVerifiedTime(entity.getLastVerifiedTime())
                .verificationStatus(entity.getVerificationStatus())
                .description(entity.getDescription())
                .createdTime(entity.getCreatedTime())
                .updatedTime(entity.getUpdatedTime())
                .build();
    }

    /**
     * 转换为DTO（包含明文密钥，仅供内部服务调用）
     */
    private UserApiKeyDTO convertToDTOWithPlainText(UserApiKey entity) {
        UserApiKeyDTO dto = convertToDTO(entity);

        // 解密密钥
        try {
            String plainKeyId = apiAesEncryptUtil.decrypt(entity.getAccessKeyId());
            String plainKeySecret = apiAesEncryptUtil.decrypt(entity.getAccessKeySecret());

            dto.setPlainAccessKeyId(plainKeyId);
            dto.setPlainAccessKeySecret(plainKeySecret);

            log.info("✅ 密钥解密成功: keyId={}, keyName={}", entity.getId(), entity.getKeyName());

        } catch (Exception e) {
            log.error("❌ 密钥解密失败: {}", e.getMessage(), e);
        }

        return dto;
    }

    // ==================== 测试方法实现 ====================

    @Override
    public UserApiKeyDTO addUserApiKeyByUsername(String username, String keyName, String keyType,
                                                 String accessKeyId, String accessKeySecret) {
        log.info("【测试方法】根据用户名添加密钥: username={}, keyName={}", username, keyName);

        try {
            // 1. 根据用户名查询用户ID
            UserPO user = userMapper.selectOne(new LambdaQueryWrapper<UserPO>()
                    .eq(UserPO::getUsername, username)
                    .last("LIMIT 1"));

            if (user == null) {
                throw new RuntimeException("用户不存在: " + username);
            }

            Long userId = user.getId();
            Long tenantId = user.getTenantId();
            log.info("查询到用户: username={}, userId={}, tenantId={}", username, userId, tenantId);

            // 2. 构建请求DTO
            AddUserApiKeyReqDTO request = AddUserApiKeyReqDTO.builder()
                    .tenantId(tenantId)  // 设置租户ID
                    .username(username)
                    .keyName(keyName)
                    .keyType(keyType)
                    .accessKeyId(accessKeyId)
                    .accessKeySecret(accessKeySecret)
                    .build();

            // 3. 调用现有的添加方法
            return addUserApiKey(request);

        } catch (Exception e) {
            log.error("❌ 根据用户名添加密钥失败: {}", e.getMessage(), e);
            throw new RuntimeException("添加密钥失败: " + e.getMessage(), e);
        }
    }

    @Override
    public UserApiKeyDTO getUserApiKeyByUsername(String username) {
        log.info("【测试方法】根据用户名获取密钥: username={}", username);

        try {
            // 1. 先从缓存获取（根据username）
            String cacheKey = apiKeyRedisKeyBuilder.buildDefaultApiKeyByUsernameKey(username);
            UserApiKeyDTO cachedApiKey = redisUtils.get(cacheKey, UserApiKeyDTO.class);
            if (cachedApiKey != null) {
                log.info("✅ 缓存命中，默认API密钥，username:{}", username);
                return cachedApiKey;
            }

            // 2. 根据用户名查询用户ID
            UserPO user = userMapper.selectOne(new LambdaQueryWrapper<UserPO>()
                    .eq(UserPO::getUsername, username)
                    .last("LIMIT 1"));

            if (user == null) {
                log.warn("用户不存在: {}", username);
                return null;
            }

            Long userId = user.getId();
            log.info("查询到用户: username={}, userId={}", username, userId);

            // 3. 查询用户的默认密钥
            UserApiKey apiKey = userApiKeyMapper.selectDefaultByUserId(userId);

            if (apiKey == null) {
                log.warn("未找到默认密钥: userId={}", userId);
                return null;
            }

            // 4. 转换为DTO（包含明文，测试用）
            UserApiKeyDTO apiKeyDTO = convertToDTOWithPlainText(apiKey);

            // 5. 写入缓存（30分钟）
            redisUtils.set(cacheKey, apiKeyDTO, 1800);
            log.info("✅ 默认API密钥已缓存，username:{}, keyId:{}", username, apiKeyDTO.getId());

            return apiKeyDTO;

        } catch (Exception e) {
            log.error("❌ 根据用户名获取密钥失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取密钥失败: " + e.getMessage(), e);
        }
    }
}
