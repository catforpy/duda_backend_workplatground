package com.duda.file.provider.impl;

import com.duda.file.adapter.StorageService;
import com.duda.file.provider.helper.SimpleAdapterFactory;
import com.duda.file.dto.bucket.*;
import com.duda.file.enums.StorageType;
import com.duda.file.manager.BucketManager;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.provider.entity.BucketConfig;
import com.duda.file.service.BucketService;
import com.duda.file.common.exception.StorageException;
import com.duda.file.common.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Bucket服务实现
 * Dubbo服务实现类,对外提供Bucket管理服务
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
@DubboService(version = "1.0.0", timeout = 30000)
public class BucketServiceImpl implements BucketService {

    @Autowired
    private BucketManager bucketManager;

    @Autowired
    private SimpleAdapterFactory storageAdapterFactory;

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    /**
     * API密钥加密密钥(从Nacos配置中心读取)
     */
    @Value("${duda.file.encryption.key:duda-file-encryption-key}")
    private String encryptionKey;

    @Override
    public BucketDTO createBucket(CreateBucketReqDTO request) throws StorageException {
        log.info("Dubbo: Creating bucket: {}", request.getBucketName());

        try {
            // 1. 验证Bucket名称
            if (!bucketManager.validateBucketName(request.getBucketName())) {
                throw new StorageException("INVALID_BUCKET_NAME", "Invalid bucket name format");
            }

            // 2. 检查Bucket是否已存在
            BucketConfig existingBucket = bucketConfigMapper.selectByBucketName(request.getBucketName());
            if (existingBucket != null && !existingBucket.getIsDeleted()) {
                throw new StorageException("BUCKET_ALREADY_EXISTS", "Bucket already exists: " + request.getBucketName());
            }

            // 3. 获取存储适配器
            StorageService adapter = getStorageAdapter(request.getUserId(), request.getBucketName());

            // 4. 调用Manager创建Bucket（在云存储中创建）
            BucketDTO result = bucketManager.createBucket(request, adapter);

            // 5. 保存Bucket配置到数据库
            BucketConfig bucketConfig = buildBucketConfig(request);
            bucketConfigMapper.insert(bucketConfig);

            log.info("Dubbo: Bucket created successfully: {}", request.getBucketName());
            return result;

        } catch (Exception e) {
            log.error("Dubbo: Failed to create bucket: {}", request.getBucketName(), e);
            throw e;
        }
    }

    @Override
    public void deleteBucket(String bucketName, Long userId) throws StorageException {
        log.info("Dubbo: Deleting bucket: {}", bucketName);

        try {
            // 1. 从数据库查询Bucket配置
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
            }

            // 2. 权限检查
            if (!bucketConfig.getUserId().equals(userId)) {
                throw new StorageException("PERMISSION_DENIED", "No permission to delete bucket");
            }

            // 3. 获取存储适配器
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 4. 调用Manager删除Bucket（从云存储中删除）
            bucketManager.deleteBucket(bucketName, userId, adapter);

            // 5. 软删除数据库记录
            bucketConfigMapper.deleteByBucketName(bucketName);

            log.info("Dubbo: Bucket deleted successfully: {}", bucketName);

        } catch (Exception e) {
            log.error("Dubbo: Failed to delete bucket: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public BucketDTO getBucketInfo(String bucketName) {
        log.debug("Dubbo: Getting bucket info: {}", bucketName);

        try {
            // 从数据库查询Bucket配置
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
            }

            // 获取存储适配器
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 调用Manager获取Bucket信息
            return bucketManager.getBucketInfo(bucketName, adapter);

        } catch (Exception e) {
            log.error("Dubbo: Failed to get bucket info: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public Boolean doesBucketExist(String bucketName) {
        log.debug("Dubbo: Checking bucket existence: {}", bucketName);

        try {
            // 从数据库查询Bucket
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);

            // 如果数据库中存在且未删除，返回true
            if (bucketConfig != null && !bucketConfig.getIsDeleted()) {
                return true;
            }

            // 数据库中不存在，检查云存储中是否存在
            if (bucketConfig != null) {
                StorageService adapter = getStorageAdapterFromConfig(bucketConfig);
                return bucketManager.doesBucketExist(bucketName, adapter);
            }

            return false;

        } catch (Exception e) {
            log.error("Dubbo: Failed to check bucket existence: {}", bucketName, e);
            return false;
        }
    }

    @Override
    public List<BucketDTO> listBuckets(Long userId) {
        log.debug("Dubbo: Listing buckets for user: {}", userId);

        try {
            // 从数据库查询用户的Bucket列表
            List<BucketConfig> bucketConfigs = bucketConfigMapper.selectByUserId(userId);

            // 转换为BucketDTO
            return bucketConfigs.stream()
                .map(this::convertToBucketDTO)
                .toList();

        } catch (Exception e) {
            log.error("Dubbo: Failed to list buckets for user: {}", userId, e);
            throw e;
        }
    }

    @Override
    public void setBucketAcl(String bucketName, com.duda.file.enums.AclType aclType) {
        log.info("Dubbo: Setting bucket ACL: {} -> {}", bucketName, aclType);

        try {
            // 从数据库查询Bucket配置
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
            }

            // 获取存储适配器
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 调用Manager设置ACL
            bucketManager.setBucketAcl(bucketName, aclType, adapter);

            // 更新数据库中的ACL配置
            bucketConfig.setAclType(aclType.name());
            bucketConfigMapper.update(bucketConfig);

            log.info("Dubbo: Bucket ACL set successfully");

        } catch (Exception e) {
            log.error("Dubbo: Failed to set bucket ACL: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public com.duda.file.enums.AclType getBucketAcl(String bucketName) {
        log.debug("Dubbo: Getting bucket ACL: {}", bucketName);

        try {
            // 从数据库查询Bucket配置
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
            }

            // 返回数据库中的ACL配置
            return com.duda.file.enums.AclType.valueOf(bucketConfig.getAclType());

        } catch (Exception e) {
            log.error("Dubbo: Failed to get bucket ACL: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public String getBucketLocation(String bucketName) {
        log.debug("Dubbo: Getting bucket location: {}", bucketName);

        try {
            // 从数据库查询Bucket配置
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
            }

            // 返回数据库中的region信息
            return bucketConfig.getRegion();

        } catch (Exception e) {
            log.error("Dubbo: Failed to get bucket location: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public void setBucketTags(String bucketName, Map<String, String> tags) {
        log.info("Dubbo: Setting bucket tags: {}", bucketName);

        try {
            // 从数据库查询Bucket配置
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
            }

            // 获取存储适配器
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 调用Manager设置标签（在云存储中设置）
            bucketManager.setBucketTags(bucketName, tags, adapter);

            // 更新数据库中的标签（JSON格式）
            // TODO: 将Map转换为JSON字符串
            bucketConfig.setTags(tags.toString());
            bucketConfigMapper.update(bucketConfig);

            log.info("Dubbo: Bucket tags set successfully");

        } catch (Exception e) {
            log.error("Dubbo: Failed to set bucket tags: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public Map<String, String> getBucketTags(String bucketName) {
        log.debug("Dubbo: Getting bucket tags: {}", bucketName);

        try {
            // 从数据库查询Bucket配置
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
            }

            // TODO: 解析JSON格式的tags
            // 临时返回空Map
            return Map.of();

        } catch (Exception e) {
            log.error("Dubbo: Failed to get bucket tags: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public BucketStatisticsDTO getBucketStatistics(String bucketName) {
        log.debug("Dubbo: Getting bucket statistics: {}", bucketName);

        try {
            // 从数据库查询Bucket配置
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
            }

            // 构建统计信息
            return BucketStatisticsDTO.builder()
                .bucketName(bucketName)
                .fileCount(bucketConfig.getCurrentFileCount() != null ? bucketConfig.getCurrentFileCount().longValue() : 0L)
                .storageUsed(bucketConfig.getCurrentStorageSize())
                .build();

        } catch (Exception e) {
            log.error("Dubbo: Failed to get bucket statistics: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public void updateBucketQuota(String bucketName, Long maxSize, Integer maxCount) {
        log.info("Dubbo: Updating bucket quota: {} (maxSize: {}, maxCount: {})", bucketName, maxSize, maxCount);

        try {
            // 从数据库查询Bucket配置
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
            }

            // 更新配额
            if (maxSize != null) {
                bucketConfig.setMaxStorageSize(maxSize);
            }
            if (maxCount != null) {
                bucketConfig.setMaxFileCount(maxCount);
            }

            bucketConfigMapper.update(bucketConfig);

            log.info("Dubbo: Bucket quota updated successfully");

        } catch (Exception e) {
            log.error("Dubbo: Failed to update bucket quota: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public String generateBucketName(Long userId, String userType, String category) {
        return bucketManager.generateBucketName(userId, userType, category);
    }

    @Override
    public Boolean validateBucketName(String bucketName) {
        return bucketManager.validateBucketName(bucketName);
    }

    @Override
    public void deleteBucketPolicy(String bucketName, String policyType) {
        log.info("Dubbo: Deleting bucket policy: {} - {}", bucketName, policyType);

        try {
            // TODO: 实现删除Bucket策略
            log.warn("deleteBucketPolicy not implemented yet");

        } catch (Exception e) {
            log.error("Dubbo: Failed to delete bucket policy: {} - {}", bucketName, policyType, e);
            throw e;
        }
    }

    @Override
    public String getBucketPolicy(String bucketName, String policyType) {
        log.debug("Dubbo: Getting bucket policy: {} - {}", bucketName, policyType);

        try {
            // TODO: 实现获取Bucket策略
            return "";

        } catch (Exception e) {
            log.error("Dubbo: Failed to get bucket policy: {} - {}", bucketName, policyType, e);
            throw e;
        }
    }

    @Override
    public void setBucketPolicy(String bucketName, String policyType, String policy) {
        log.info("Dubbo: Setting bucket policy: {} - {}", bucketName, policyType);

        try {
            // TODO: 实现设置Bucket策略
            log.warn("setBucketPolicy not implemented yet");

        } catch (Exception e) {
            log.error("Dubbo: Failed to set bucket policy: {} - {}", bucketName, policyType, e);
            throw e;
        }
    }

    @Override
    public void deleteBucketTags(String bucketName, java.util.List<String> tagKeys) {
        log.info("Dubbo: Deleting bucket tags: {} - {}", bucketName, tagKeys);

        try {
            // TODO: 实现删除Bucket标签
            log.warn("deleteBucketTags not implemented yet");

        } catch (Exception e) {
            log.error("Dubbo: Failed to delete bucket tags: {} - {}", bucketName, tagKeys, e);
            throw e;
        }
    }

    @Override
    public Boolean checkPermission(String bucketName, Long userId) {
        // 从数据库查询Bucket配置
        BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);

        if (bucketConfig == null || bucketConfig.getIsDeleted()) {
            return false;
        }

        return bucketManager.checkPermission(bucketName, userId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 从数据库配置获取存储适配器
     */
    private StorageService getStorageAdapter(Long userId, String bucketName) {
        // 从数据库查询Bucket配置
        BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);

        if (bucketConfig != null) {
            return getStorageAdapterFromConfig(bucketConfig);
        }

        // 如果数据库中没有配置，使用默认配置
        log.warn("Bucket config not found in database, using default config: {}", bucketName);

        // TODO: 从Nacos配置中心读取默认的API密钥
        ApiKeyConfigDTO apiKeyConfig = ApiKeyConfigDTO.builder()
            .storageType(StorageType.ALIYUN_OSS)
            .accessKeyId("")  // 从配置中心读取
            .accessKeySecret("")  // 从配置中心读取
            .endpoint("oss-cn-hangzhou.aliyuncs.com")
            .region("cn-hangzhou")
            .build();

        return storageAdapterFactory.createAdapter(StorageType.ALIYUN_OSS, apiKeyConfig);
    }

    /**
     * 根据Bucket配置创建存储适配器
     */
    private StorageService getStorageAdapterFromConfig(BucketConfig bucketConfig) {
        // 解析存储类型
        StorageType storageType = StorageType.valueOf(bucketConfig.getStorageType());

        // 构建API密钥配置
        ApiKeyConfigDTO apiKeyConfig = ApiKeyConfigDTO.builder()
            .storageType(storageType)
            .accessKeyId(decryptApiKey(bucketConfig.getAccessKeyId()))
            .accessKeySecret(decryptApiKey(bucketConfig.getAccessKeySecret()))
            .endpoint(bucketConfig.getEndpoint())
            .region(bucketConfig.getRegion())
            .build();

        return storageAdapterFactory.createAdapter(storageType, apiKeyConfig);
    }

    /**
     * 解密API密钥
     * 使用AES解密从数据库读取的加密API密钥
     */
    private String decryptApiKey(String encryptedKey) {
        if (!StringUtils.hasText(encryptedKey)) {
            return "";
        }
        try {
            return AesUtil.decrypt(encryptedKey, encryptionKey);
        } catch (Exception e) {
            log.error("解密API密钥失败", e);
            throw new StorageException("DECRYPTION_FAILED", "Failed to decrypt API key: " + e.getMessage());
        }
    }

    /**
     * 构建BucketConfig实体
     */
    private BucketConfig buildBucketConfig(CreateBucketReqDTO request) {
        return BucketConfig.builder()
            .bucketName(request.getBucketName())
            .bucketDisplayName(request.getDisplayName())
            .storageType(request.getStorageType() != null ? request.getStorageType() : "ALIYUN_OSS")
            .region(request.getRegion())
            .userId(request.getUserId())
            .userType(request.getUserType())
            .storageClass(request.getStorageClass() != null ? request.getStorageClass().name() : "STANDARD")
            .aclType(request.getAclType() != null ? request.getAclType().name() : "PRIVATE")
            .maxFileSize(request.getMaxFileSize())
            .maxFileCount(request.getMaxFileCount())
            .category(request.getCategory())
            .description(request.getDescription())
            .status("ACTIVE")
            .createdBy(request.getUserId())
            .isDeleted(false)
            // TODO: 设置API密钥（需要加密）
            .accessKeyId("")
            .accessKeySecret("")
            .endpoint("")
            .build();
    }

    /**
     * 转换BucketConfig为BucketDTO
     */
    private BucketDTO convertToBucketDTO(BucketConfig bucketConfig) {
        return BucketDTO.builder()
            .bucketName(bucketConfig.getBucketName())
            .displayName(bucketConfig.getBucketDisplayName())
            .region(bucketConfig.getRegion())
            .storageClass(com.duda.file.enums.StorageClass.valueOf(bucketConfig.getStorageClass()))
            .acl(com.duda.file.enums.AclType.valueOf(bucketConfig.getAclType()))
            .creationTime(bucketConfig.getCreatedTime() != null ?
                java.sql.Timestamp.valueOf(bucketConfig.getCreatedTime()) : null)
            .build();
    }
}
