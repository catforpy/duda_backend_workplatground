package com.duda.file.service.impl;

import com.duda.file.dto.bucket.*;
import com.duda.file.service.BucketService;
import com.duda.file.manager.BucketManager;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.provider.entity.BucketConfig;
import com.duda.file.provider.util.AesEncryptUtil;
import com.duda.user.dto.userapikey.UserApiKeyDTO;
import com.duda.user.rpc.IUserApiKeyRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bucket Service 实现
 * 业务逻辑实现类
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@Service("bucketServiceImpl")
public class BucketServiceImpl implements BucketService {

    @Autowired
    private BucketManager bucketManager;

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Autowired
    private AesEncryptUtil aesEncryptUtil;

    @DubboReference(version = "1.0.0", group = "USER_GROUP", check = false, url = "dubbo://duda-user-provider:20880")
    private IUserApiKeyRpc userApiKeyRpc;

    @Override
    public BucketDTO createBucket(CreateBucketReqDTO request) {
        log.info("【Service】Creating bucket: {}", request.getBucketName());

        try {
            // ==================== 步骤1: 验证参数 ====================
            if (request.getBucketName() == null || request.getBucketName().trim().isEmpty()) {
                throw new IllegalArgumentException("Bucket名称不能为空");
            }
            if (request.getUserId() == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            if (request.getKeyName() == null || request.getKeyName().trim().isEmpty()) {
                throw new IllegalArgumentException("API密钥名称不能为空");
            }

            // ==================== 步骤2: 查询 API 密钥 ====================
            List<UserApiKeyDTO> userApiKeys = userApiKeyRpc.listUserApiKeys(request.getUserId(), false);
            if (userApiKeys == null || userApiKeys.isEmpty()) {
                throw new RuntimeException("用户没有配置API密钥: userId=" + request.getUserId());
            }

            UserApiKeyDTO matchedApiKey = userApiKeys.stream()
                .filter(key -> request.getKeyName().equals(key.getKeyName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到指定的API密钥: keyName=" + request.getKeyName()));

            Long apiKeyId = matchedApiKey.getId();
            log.info("找到API密钥: keyName={}, apiKeyId={}", request.getKeyName(), apiKeyId);

            // ==================== 步骤3: 解密获取密钥信息 ====================
            String accessKeyId = aesEncryptUtil.decrypt(matchedApiKey.getAccessKeyId());
            String accessKeySecret = aesEncryptUtil.decrypt(matchedApiKey.getAccessKeySecret());
            // String endpoint = matchedApiKey.getEndpoint();  // 如果没有 getEndpoint 方法，使用 region 构建
            String region = matchedApiKey.getRegion();

            if (accessKeyId == null || accessKeySecret == null) {
                throw new RuntimeException("API密钥信息不完整");
            }

            log.info("密钥解密成功: region={}", region);

            // ==================== 步骤4: 构建 ApiKeyConfigDTO ====================
            com.duda.file.dto.bucket.ApiKeyConfigDTO apiKeyConfigDTO =
                new com.duda.file.dto.bucket.ApiKeyConfigDTO();
            apiKeyConfigDTO.setAccessKeyId(accessKeyId);
            apiKeyConfigDTO.setAccessKeySecret(accessKeySecret);
            // TODO: endpoint 可能需要从其他地方获取，用于其他阿里云业务功能
            // apiKeyConfigDTO.setEndpoint(endpoint);
            apiKeyConfigDTO.setRegion(region);

            // ==================== 步骤5: 创建 OSS 适配器 ====================
            com.duda.file.adapter.AliyunOSSAdapter ossAdapter =
                new com.duda.file.adapter.AliyunOSSAdapter(apiKeyConfigDTO);

            // ==================== 步骤6: 检查 Bucket 是否已存在 ====================
            boolean exists = ossAdapter.doesBucketExist(request.getBucketName());
            if (exists) {
                throw new RuntimeException("Bucket已存在: " + request.getBucketName());
            }

            // ==================== 步骤7: 构建 OSS 配置参数 ====================
            Map<String, Object> config = new HashMap<>();

            // 存储类型
            if (request.getStorageClass() != null) {
                config.put("storageClass", request.getStorageClass().name());
            }

            // ACL
            if (request.getAclType() != null) {
                config.put("acl", request.getAclType().name());
            }

            // ==================== 步骤8: 调用 OSS SDK 创建 Bucket ====================
            log.info("开始创建OSS Bucket: bucketName={}, region={}", request.getBucketName(), request.getRegion());

            BucketDTO bucketDTO = ossAdapter.createBucket(
                request.getBucketName(),
                request.getRegion(),
                config
            );

            log.info("OSS Bucket创建成功: bucketName={}", request.getBucketName());

            // ==================== 步骤9: 保存到数据库 ====================
            BucketConfig bucketConfig = BucketConfig.builder()
                .bucketName(request.getBucketName())
                .bucketDisplayName(request.getDisplayName())
                .storageType(request.getStorageType())
                .region(request.getRegion())
                .userId(request.getUserId())
                .apiKeyId(apiKeyId)  // 关联到 user_api_keys.id
                .userType(request.getUserType())
                .storageClass(request.getStorageClass() != null ? request.getStorageClass().name() : "STANDARD")
                .dataRedundancyType(request.getDataRedundancyType() != null ? request.getDataRedundancyType().name() : "LRS")
                .aclType(request.getAclType() != null ? request.getAclType().name() : "PRIVATE")
                .maxFileSize(request.getMaxFileSize() != null ? request.getMaxFileSize() : 10737418240L)
                .maxFileCount(request.getMaxFileCount() != null ? request.getMaxFileCount() : 100000)
                .cdnEnabled(false)
                .cdnDomain(null)
                .versioningEnabled(request.getVersioningEnabled() != null ? request.getVersioningEnabled() : false)
                .corsEnabled(false)
                .watermarkEnabled(false)
                .encryptionEnabled(false)
                .lifecycleEnabled(false)
                .currentFileCount(0)
                .currentStorageSize(0L)
                .status("ACTIVE")
                .autoRenewEnabled(false)
                .tags(request.getTags() != null ? new com.google.gson.Gson().toJson(request.getTags()) : null)
                .category(request.getCategory())
                .description(request.getDescription())
                .createdBy(request.getUserId())
                .isDeleted(false)
                .build();

            bucketConfigMapper.insert(bucketConfig);
            log.info("Bucket配置已保存到数据库: bucketName={}, id={}", bucketConfig.getBucketName(), bucketConfig.getId());

            // ==================== 步骤10: 返回结果 ====================
            // bucketDTO.setId(bucketConfig.getId());
            // bucketDTO.setDisplayName(bucketConfig.getBucketDisplayName());

            return bucketDTO;

        } catch (Exception e) {
            log.error("创建Bucket失败: bucketName={}", request.getBucketName(), e);
            throw new RuntimeException("创建Bucket失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteBucket(String bucketName, Long userId) {
        log.info("【Service】Deleting bucket: {}", bucketName);

        try {
            // ==================== 步骤1: 查询 Bucket 配置 ====================
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new RuntimeException("Bucket不存在: " + bucketName);
            }

            // 验证用户权限
            if (!bucketConfig.getUserId().equals(userId)) {
                throw new RuntimeException("无权限删除此Bucket");
            }

            // ==================== 步骤2: 查询用户的 API 密钥 ====================
            Long apiKeyId = bucketConfig.getApiKeyId();
            UserApiKeyDTO matchedApiKey = null;

            // 如果 Bucket 关联了 API 密钥，使用关联的密钥；否则使用用户的默认密钥
            if (apiKeyId != null) {
                List<UserApiKeyDTO> userApiKeys = userApiKeyRpc.listUserApiKeys(userId, false);
                if (userApiKeys == null || userApiKeys.isEmpty()) {
                    throw new RuntimeException("用户没有配置API密钥: userId=" + userId);
                }
                matchedApiKey = userApiKeys.stream()
                    .filter(key -> key.getId().equals(apiKeyId))
                    .findFirst()
                    .orElse(null);
            }

            // 如果没有找到关联的密钥，使用用户的第一个密钥
            if (matchedApiKey == null) {
                List<UserApiKeyDTO> userApiKeys = userApiKeyRpc.listUserApiKeys(userId, false);
                if (userApiKeys == null || userApiKeys.isEmpty()) {
                    throw new RuntimeException("用户没有配置API密钥: userId=" + userId);
                }
                matchedApiKey = userApiKeys.get(0);
                log.warn("Bucket未关联API密钥，使用用户的默认密钥: keyName={}", matchedApiKey.getKeyName());
            }

            // ==================== 步骤3: 解密获取密钥信息 ====================
            String accessKeyId = aesEncryptUtil.decrypt(matchedApiKey.getAccessKeyId());
            String accessKeySecret = aesEncryptUtil.decrypt(matchedApiKey.getAccessKeySecret());
            String region = matchedApiKey.getRegion();

            if (accessKeyId == null || accessKeySecret == null) {
                throw new RuntimeException("API密钥信息不完整");
            }

            log.info("密钥解密成功: region={}", region);

            // ==================== 步骤4: 构建 ApiKeyConfigDTO ====================
            com.duda.file.dto.bucket.ApiKeyConfigDTO apiKeyConfigDTO =
                new com.duda.file.dto.bucket.ApiKeyConfigDTO();
            apiKeyConfigDTO.setAccessKeyId(accessKeyId);
            apiKeyConfigDTO.setAccessKeySecret(accessKeySecret);
            apiKeyConfigDTO.setRegion(region);

            // ==================== 步骤5: 创建 OSS 适配器 ====================
            com.duda.file.adapter.AliyunOSSAdapter ossAdapter =
                new com.duda.file.adapter.AliyunOSSAdapter(apiKeyConfigDTO);

            // ==================== 步骤6: 调用 OSS SDK 删除 Bucket ====================
            log.info("开始删除OSS Bucket: bucketName={}", bucketName);
            ossAdapter.deleteBucket(bucketName);
            log.info("OSS Bucket删除成功: bucketName={}", bucketName);

            // ==================== 步骤7: 更新数据库（软删除） ====================
            bucketConfigMapper.deleteByBucketName(bucketName);
            log.info("数据库记录已更新（软删除）: bucketName={}", bucketName);

        } catch (Exception e) {
            log.error("删除Bucket失败: bucketName={}", bucketName, e);
            throw new RuntimeException("删除Bucket失败: " + e.getMessage(), e);
        }
    }

    @Override
    public BucketDTO getBucketInfo(String bucketName) {
        log.info("【Service】Getting bucket info: {}", bucketName);

        try {
            // ==================== 步骤1: 从数据库查询 Bucket 配置 ====================
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new RuntimeException("Bucket不存在: " + bucketName);
            }

            // ==================== 步骤2: 获取用户的 API 密钥 ====================
            Long userId = bucketConfig.getUserId();
            Long apiKeyId = bucketConfig.getApiKeyId();

            List<UserApiKeyDTO> userApiKeys = userApiKeyRpc.listUserApiKeys(userId, false);
            if (userApiKeys == null || userApiKeys.isEmpty()) {
                throw new RuntimeException("用户没有配置API密钥: userId=" + userId);
            }

            UserApiKeyDTO matchedApiKey = null;
            if (apiKeyId != null) {
                matchedApiKey = userApiKeys.stream()
                    .filter(key -> key.getId().equals(apiKeyId))
                    .findFirst()
                    .orElse(null);
            }

            if (matchedApiKey == null) {
                matchedApiKey = userApiKeys.get(0);
                log.warn("Bucket未关联API密钥，使用默认密钥: bucketName={}", bucketName);
            }

            // ==================== 步骤3: 解密密钥信息 ====================
            String accessKeyId = aesEncryptUtil.decrypt(matchedApiKey.getAccessKeyId());
            String accessKeySecret = aesEncryptUtil.decrypt(matchedApiKey.getAccessKeySecret());
            String region = matchedApiKey.getRegion();

            if (accessKeyId == null || accessKeySecret == null) {
                throw new RuntimeException("API密钥信息不完整");
            }

            // ==================== 步骤4: 创建 OSS 适配器并获取信息 ====================
            com.duda.file.dto.bucket.ApiKeyConfigDTO apiKeyConfigDTO =
                new com.duda.file.dto.bucket.ApiKeyConfigDTO();
            apiKeyConfigDTO.setAccessKeyId(accessKeyId);
            apiKeyConfigDTO.setAccessKeySecret(accessKeySecret);
            apiKeyConfigDTO.setRegion(region);

            com.duda.file.adapter.AliyunOSSAdapter ossAdapter =
                new com.duda.file.adapter.AliyunOSSAdapter(apiKeyConfigDTO);

            // 从 OSS 获取 Bucket 信息
            log.info("从OSS获取Bucket信息: bucketName={}", bucketName);
            BucketDTO bucketDTO = ossAdapter.getBucketInfo(bucketName);
            log.info("OSS Bucket信息获取成功: bucketName={}, location={}", bucketName, bucketDTO.getRegion());

            // ==================== 步骤5: 同步到数据库（可选）====================
            // 更新数据库中的 Bucket 信息
            if (bucketDTO.getCreationTime() != null) {
                bucketConfig.setCreatedTime(bucketDTO.getCreationTime().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            if (bucketDTO.getRegion() != null) {
                bucketConfig.setRegion(bucketDTO.getRegion());
            }
            if (bucketDTO.getStorageClass() != null) {
                bucketConfig.setStorageClass(bucketDTO.getStorageClass().name());
            }
            bucketConfigMapper.update(bucketConfig);
            log.info("数据库信息已同步: bucketName={}", bucketName);

            return bucketDTO;

        } catch (Exception e) {
            log.error("获取Bucket信息失败: bucketName={}", bucketName, e);
            throw new RuntimeException("获取Bucket信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        log.info("【Service】Checking bucket existence: {}", bucketName);
        // 具体实现...
        return false;
    }

    @Override
    public List<BucketDTO> listBuckets(Long userId, String keyName) {
        log.info("【Service】Listing buckets for user: {}, keyName: {}", userId, keyName);

        try {
            // 步骤1: 查询 user_api_keys 获取 API 密钥信息
            List<UserApiKeyDTO> userApiKeys = userApiKeyRpc.listUserApiKeys(userId, false);
            if (userApiKeys == null || userApiKeys.isEmpty()) {
                log.warn("用户没有配置API密钥: userId={}", userId);
                return List.of();
            }

            // 步骤2: 筛选出匹配 keyName 的 API 密钥
            UserApiKeyDTO matchedApiKey = userApiKeys.stream()
                .filter(key -> keyName.equals(key.getKeyName()))
                .findFirst()
                .orElse(null);

            if (matchedApiKey == null) {
                log.warn("未找到指定的API密钥: userId={}, keyName={}", userId, keyName);
                return List.of();
            }

            // 步骤3: 获取 API 密钥的 ID (这就是 api_key_id)
            Long apiKeyId = matchedApiKey.getId();
            log.info("找到API密钥: keyName={}, apiKeyId={}", keyName, apiKeyId);

            // 步骤4: 查询该 API 密钥下的所有 Bucket
            List<BucketConfig> bucketConfigs = bucketConfigMapper.selectByApiKeyId(apiKeyId);

            if (bucketConfigs == null || bucketConfigs.isEmpty()) {
                log.info("该API密钥下没有Bucket: apiKeyId={}", apiKeyId);
                return List.of();
            }

            // 步骤5: 转换为 DTO 并返回
            List<BucketDTO> buckets = bucketConfigs.stream()
                .map(this::convertToBucketDTO)
                .toList();

            log.info("查询到Bucket数量: {}", buckets.size());
            return buckets;

        } catch (Exception e) {
            log.error("查询Bucket列表失败: userId={}, keyName={}", userId, keyName, e);
            throw new RuntimeException("查询Bucket列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转换 BucketConfig 为 BucketDTO
     */
    private BucketDTO convertToBucketDTO(BucketConfig config) {
        return BucketDTO.builder()
            .bucketName(config.getBucketName())
            .displayName(config.getBucketDisplayName())
            .region(config.getRegion())
            .storageClass(com.duda.file.enums.StorageClass.valueOf(config.getStorageClass()))
            .acl(com.duda.file.enums.AclType.valueOf(config.getAclType()))
            .build();
    }

    // TODO: 需要创建 UpdateBucketConfigReqDTO 和 BucketConfigDTO
    // @Override
    // public void updateBucketConfig(String bucketName, UpdateBucketConfigReqDTO request) {
    //     log.info("【Service】Updating bucket config: {}", bucketName);
    //     // 具体实现...
    // }
    //
    // @Override
    // public BucketConfigDTO getBucketConfig(String bucketName) {
    //     log.info("【Service】Getting bucket config: {}", bucketName);
    //     // 具体实现...
    //     return null;
    // }

    @Override
    public void setDefaultBucket(String bucketName, Long userId) {
        log.info("【Service】Setting default bucket: {}", bucketName);
        // 具体实现...
    }

    @Override
    public BucketDTO getDefaultBucket(Long userId) {
        log.info("【Service】Getting default bucket for user: {}", userId);
        // 具体实现...
        return null;
    }

    @Override
    public BucketStatisticsDTO getBucketStatistics(String bucketName) {
        log.info("【Service】Getting bucket statistics: {}", bucketName);

        try {
            // ==================== 步骤1: 从数据库查询 Bucket 配置 ====================
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new RuntimeException("Bucket不存在: " + bucketName);
            }

            // ==================== 步骤2: 获取用户的 API 密钥 ====================
            Long userId = bucketConfig.getUserId();
            Long apiKeyId = bucketConfig.getApiKeyId();

            List<UserApiKeyDTO> userApiKeys = userApiKeyRpc.listUserApiKeys(userId, false);
            if (userApiKeys == null || userApiKeys.isEmpty()) {
                throw new RuntimeException("用户没有配置API密钥: userId=" + userId);
            }

            UserApiKeyDTO matchedApiKey = null;
            if (apiKeyId != null) {
                matchedApiKey = userApiKeys.stream()
                    .filter(key -> key.getId().equals(apiKeyId))
                    .findFirst()
                    .orElse(null);
            }

            if (matchedApiKey == null) {
                matchedApiKey = userApiKeys.get(0);
                log.warn("Bucket未关联API密钥，使用默认密钥: bucketName={}", bucketName);
            }

            // ==================== 步骤3: 解密密钥信息 ====================
            String accessKeyId = aesEncryptUtil.decrypt(matchedApiKey.getAccessKeyId());
            String accessKeySecret = aesEncryptUtil.decrypt(matchedApiKey.getAccessKeySecret());
            String region = matchedApiKey.getRegion();

            if (accessKeyId == null || accessKeySecret == null) {
                throw new RuntimeException("API密钥信息不完整");
            }

            // ==================== 步骤4: 创建 OSS 适配器并获取统计信息 ====================
            com.duda.file.dto.bucket.ApiKeyConfigDTO apiKeyConfigDTO =
                new com.duda.file.dto.bucket.ApiKeyConfigDTO();
            apiKeyConfigDTO.setAccessKeyId(accessKeyId);
            apiKeyConfigDTO.setAccessKeySecret(accessKeySecret);
            apiKeyConfigDTO.setRegion(region);

            com.duda.file.adapter.AliyunOSSAdapter ossAdapter =
                new com.duda.file.adapter.AliyunOSSAdapter(apiKeyConfigDTO);

            // 从 OSS 获取 Bucket 统计信息
            log.info("从OSS获取Bucket统计信息: bucketName={}", bucketName);
            com.aliyun.oss.model.BucketStat stat = ossAdapter.getBucketStat(bucketName);

            // ==================== 步骤5: 构建 BucketStatisticsDTO ====================
            BucketStatisticsDTO statistics = BucketStatisticsDTO.builder()
                .bucketName(bucketName)
                .fileCount(stat.getObjectCount())           // 总文件数量
                .storageUsed(stat.getStorageSize())          // 总存储量（字节）
                .storageQuota(bucketConfig.getMaxStorageSize())  // 存储配额
                .lastUpdateTime(System.currentTimeMillis())   // 最后更新时间
                .build();

            // 计算使用率百分比
            Long maxStorageSize = bucketConfig.getMaxStorageSize();
            if (maxStorageSize != null && maxStorageSize > 0) {
                double usagePercent = (double) stat.getStorageSize() / maxStorageSize * 100;
                statistics.setUsagePercentage(Math.round(usagePercent * 100.0) / 100.0);  // 保留两位小数
            }

            log.info("OSS Bucket统计信息获取成功: bucketName={}, storageSize={}, objectCount={}",
                bucketName, stat.getStorageSize(), stat.getObjectCount());

            // ==================== 步骤6: 同步到数据库 ====================
            bucketConfig.setCurrentStorageSize(stat.getStorageSize());
            bucketConfig.setCurrentFileCount((int) stat.getObjectCount().longValue());
            bucketConfigMapper.update(bucketConfig);
            log.info("数据库统计信息已同步: bucketName={}", bucketName);

            return statistics;

        } catch (Exception e) {
            log.error("获取Bucket统计信息失败: bucketName={}", bucketName, e);
            throw new RuntimeException("获取Bucket统计信息失败: " + e.getMessage(), e);
        }
    }
}
