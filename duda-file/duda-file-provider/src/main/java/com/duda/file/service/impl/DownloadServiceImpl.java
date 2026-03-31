package com.duda.file.service.impl;

import com.duda.file.adapter.StorageService;
import com.duda.file.provider.helper.SimpleAdapterFactory;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import com.duda.file.dto.download.DownloadReqDTO;
import com.duda.file.dto.download.DownloadResultDTO;
import com.duda.file.enums.StorageType;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.provider.mapper.ObjectMetadataMapper;
import com.duda.file.provider.mapper.FileAccessLogMapper;
import com.duda.file.provider.entity.BucketConfig;
import com.duda.file.provider.entity.ObjectMetadata;
import com.duda.file.provider.entity.FileAccessLog;
import com.duda.file.service.DownloadService;
import com.duda.file.common.exception.StorageException;
import com.duda.file.common.util.AesUtil;
import com.duda.user.rpc.IUserApiKeyRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * 下载服务实现
 * 业务逻辑实现类
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@Service
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    private SimpleAdapterFactory storageAdapterFactory;

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Autowired
    private ObjectMetadataMapper objectMetadataMapper;

    @Autowired
    private FileAccessLogMapper fileAccessLogMapper;

    @DubboReference(
        version = "1.0.0",
        group = "USER_GROUP",
        registry = "userRegistry",
        check = false
    )
    private IUserApiKeyRpc userApiKeyRpc;
    /**
     * API密钥加密密钥(从Nacos配置中心读取)
     */
    @Value("${duda.file.encryption.key:duda-file-encryption-key}")
    private String encryptionKey;

    /**
     * OSS配置（从bootstrap.yml读取，作为RPC失败时的fallback）
     */
    @Value("${oss.access-key-id:}")
    private String ossAccessKeyId;

    @Value("${oss.access-key-secret:}")
    private String ossAccessKeySecret;

    @Value("${oss.region:cn-hangzhou}")
    private String ossRegion;

    @Override
    public DownloadResultDTO download(DownloadReqDTO request) throws StorageException {
        log.info("Service: Download: {}/{}", request.getBucketName(), request.getObjectKey());
        LocalDateTime startTime = LocalDateTime.now();

        try {
            // 1. 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(
                request.getBucketName(),
                request.getObjectKey()
            );

            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // 2. 获取存储适配器
            StorageService adapter = getStorageAdapter(request.getUserId(), request.getBucketName());

            // 3. 调用适配器下载
            DownloadResultDTO result = adapter.downloadObject(
                    request.getBucketName(),
                    request.getObjectKey()
            );

            // 4. 更新下载统计
            objectMetadataMapper.updateDownloadStats(request.getBucketName(), request.getObjectKey());

            // 5. 记录访问日志
            saveAccessLog(request.getBucketName(), request.getObjectKey(), "DOWNLOAD",
                        request.getUserId(), metadata.getFileSize(),
                        "SUCCESS", null, startTime);

            log.info("Service: Download completed: {}/{}", request.getBucketName(), request.getObjectKey());
            return result;

        } catch (Exception e) {
            log.error("Service: Failed to download: {}/{}", request.getBucketName(), request.getObjectKey(), e);

            // 记录失败的访问日志
            saveAccessLog(request.getBucketName(), request.getObjectKey(), "DOWNLOAD",
                        request.getUserId(), null,
                        "FAILED", e.getMessage(), startTime);

            throw e;
        }
    }

    @Override
    public String getDownloadUrl(String bucketName, String objectKey, Integer expiration) {
        log.info("Service: Getting download URL: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询Bucket配置
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found");
            }

            // 获取存储适配器
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 生成预签名URL
            int exp = expiration != null ? expiration : 3600;
            return adapter.generatePresignedUrl(bucketName, objectKey, exp, "GET");

        } catch (Exception e) {
            log.error("Service: Failed to get download URL: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public Boolean checkPermission(String bucketName, String objectKey, Long userId) {
        // 从数据库查询Bucket配置
        BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);

        if (bucketConfig == null || bucketConfig.getIsDeleted()) {
            return false;
        }

        return bucketConfig.getUserId().equals(userId);
    }

    @Override
    public InputStream getFileStream(String bucketName, String objectKey) {
        log.info("Service: Getting file stream: {}/{}", bucketName, objectKey);

        try {
            // 获取存储适配器
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found");
            }

            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 获取文件流 - 通过downloadObject获取
            DownloadResultDTO result = adapter.downloadObject(bucketName, objectKey);
            return result.getInputStream();

        } catch (Exception e) {
            log.error("Service: Failed to get file stream: {}/{}", bucketName, objectKey, e);
            throw new StorageException("GET_STREAM_FAILED", "Failed to get file stream: " + e.getMessage());
        }
    }

    @Override
    public byte[] getFileBytes(String bucketName, String objectKey) {
        log.info("Service: Getting file bytes: {}/{}", bucketName, objectKey);

        try {
            // 获取存储适配器
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found");
            }

            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 获取文件字节数组 - 通过downloadObject获取流,然后转换为字节数组
            DownloadResultDTO result = adapter.downloadObject(bucketName, objectKey);
            return result.getInputStream().readAllBytes();

        } catch (Exception e) {
            log.error("Service: Failed to get file bytes: {}/{}", bucketName, objectKey, e);
            throw new StorageException("GET_BYTES_FAILED", "Failed to get file bytes: " + e.getMessage());
        }
    }

    @Override
    public Long getFileSize(String bucketName, String objectKey) {
        log.debug("Service: Getting file size: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);

            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            return metadata.getFileSize();

        } catch (Exception e) {
            log.error("Service: Failed to get file size: {}/{}", bucketName, objectKey, e);
            throw new StorageException("GET_SIZE_FAILED", "Failed to get file size: " + e.getMessage());
        }
    }

    @Override
    public String getContentType(String bucketName, String objectKey) {
        log.debug("Service: Getting content type: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);

            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            return metadata.getContentType();

        } catch (Exception e) {
            log.error("Service: Failed to get content type: {}/{}", bucketName, objectKey, e);
            throw new StorageException("GET_CONTENT_TYPE_FAILED", "Failed to get content type: " + e.getMessage());
        }
    }

    @Override
    public Boolean validateDownloadPermission(String bucketName, String objectKey, Long userId) {
        log.debug("Service: Validating download permission: {}/{}", bucketName, objectKey);

        try {
            // 1. 检查Bucket权限
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null || bucketConfig.getIsDeleted()) {
                return false;
            }

            // 2. 检查用户是否有权限访问Bucket
            if (!bucketConfig.getUserId().equals(userId)) {
                return false;
            }

            // 3. 检查对象是否存在
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null || !"active".equals(metadata.getStatus())) {
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Service: Failed to validate download permission: {}/{}", bucketName, objectKey, e);
            return false;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取存储适配器
     */
    private StorageService getStorageAdapter(Long userId, String bucketName) {
        BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);

        if (bucketConfig != null) {
            return getStorageAdapterFromConfig(bucketConfig);
        }

        // 如果数据库中没有配置,使用默认配置
        log.warn("Bucket config not found in database, using default config: {}", bucketName);

        ApiKeyConfigDTO apiKeyConfig = ApiKeyConfigDTO.builder()
            .storageType(StorageType.ALIYUN_OSS)
            .accessKeyId("")
            .accessKeySecret("")
            .endpoint("oss-cn-hangzhou.aliyuncs.com")
            .region("cn-hangzhou")
            .build();

        return storageAdapterFactory.createAdapter(StorageType.ALIYUN_OSS, apiKeyConfig);
    }

    /**
     * 根据Bucket配置创建存储适配器
     * 当RPC调用失败时，使用bootstrap.yml中的OSS配置作为fallback
     */
    private StorageService getStorageAdapterFromConfig(BucketConfig bucketConfig) {
        StorageType storageType = StorageType.fromCode(bucketConfig.getStorageType());

        try {
            // 尝试通过RPC调用获取API密钥信息
            com.duda.user.dto.userapikey.UserApiKeyDTO apiKeyDTO = userApiKeyRpc.getUserApiKeyById(bucketConfig.getApiKeyId());

            if (apiKeyDTO != null) {
                log.info("✓ 通过RPC获取API密钥成功: keyId={}, keyName={}",
                    bucketConfig.getApiKeyId(), apiKeyDTO.getKeyName());

                ApiKeyConfigDTO apiKeyConfig = ApiKeyConfigDTO.builder()
                    .storageType(storageType)
                    .accessKeyId(apiKeyDTO.getPlainAccessKeyId())
                    .accessKeySecret(apiKeyDTO.getPlainAccessKeySecret())
                    .endpoint(bucketConfig.getEndpoint())
                    .region(bucketConfig.getRegion())
                    .build();

                return storageAdapterFactory.createAdapter(storageType, apiKeyConfig);
            }
        } catch (Exception e) {
            log.warn("⚠️ RPC调用获取API密钥失败，使用fallback配置: keyId={}, error={}",
                bucketConfig.getApiKeyId(), e.getMessage());
        }

        // Fallback: 使用bootstrap.yml中的OSS配置
        log.info("✓ 使用fallback OSS配置: region={}", ossRegion);

        // 检查OSS配置是否可用
        if (ossAccessKeyId == null || ossAccessKeyId.isEmpty() ||
            ossAccessKeySecret == null || ossAccessKeySecret.isEmpty()) {
            throw new StorageException("OSS_CONFIG_NOT_FOUND",
                "OSS配置未找到，请检查bootstrap.yml中的oss.access-key-id和oss.access-key-secret配置");
        }

        ApiKeyConfigDTO apiKeyConfig = ApiKeyConfigDTO.builder()
            .storageType(storageType)
            .accessKeyId(ossAccessKeyId)
            .accessKeySecret(ossAccessKeySecret)
            .endpoint(bucketConfig.getEndpoint())
            .region(bucketConfig.getRegion() != null ? bucketConfig.getRegion() : ossRegion)
            .build();

        return storageAdapterFactory.createAdapter(storageType, apiKeyConfig);
    }

    /**
     * 解密API密钥
     * 使用AES解密从数据库读取的加密API密钥
     */
    private String decryptApiKey(String encryptedKey) {
        if (!org.springframework.util.StringUtils.hasText(encryptedKey)) {
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
     * 保存访问日志
     */
    private void saveAccessLog(String bucketName, String objectKey, String operation,
                              Long userId, Long fileSize, String resultStatus,
                              String errorMessage, LocalDateTime startTime) {
        try {
            FileAccessLog log = FileAccessLog.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .operation(operation)
                .userId(userId)
                .fileSize(fileSize)
                .resultStatus(resultStatus)
                .errorMessage(errorMessage)
                .startTime(startTime)
                .endTime(LocalDateTime.now())
                .durationMs(java.time.Duration.between(startTime, LocalDateTime.now()).toMillis())
                .build();

            fileAccessLogMapper.insert(log);
        } catch (Exception e) {
            log.error("Failed to save access log", e);
        }
    }
}
