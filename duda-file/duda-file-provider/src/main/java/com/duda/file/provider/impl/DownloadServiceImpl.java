package com.duda.file.provider.impl;

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
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

/**
 * 下载服务实现
 * Dubbo服务实现类,对外提供文件下载服务
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
@DubboService(version = "1.0.0", timeout = 60000)
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    private SimpleAdapterFactory storageAdapterFactory;

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Autowired
    private ObjectMetadataMapper objectMetadataMapper;

    @Autowired
    private FileAccessLogMapper fileAccessLogMapper;

    /**
     * API密钥加密密钥(从Nacos配置中心读取)
     */
    @Value("${duda.file.encryption.key:duda-file-encryption-key}")
    private String encryptionKey;

    @Override
    public DownloadResultDTO download(DownloadReqDTO request) throws StorageException {
        log.info("Dubbo: Download: {}/{}", request.getBucketName(), request.getObjectKey());
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

            log.info("Dubbo: Download completed: {}/{}", request.getBucketName(), request.getObjectKey());
            return result;

        } catch (Exception e) {
            log.error("Dubbo: Failed to download: {}/{}", request.getBucketName(), request.getObjectKey(), e);

            // 记录失败的访问日志
            saveAccessLog(request.getBucketName(), request.getObjectKey(), "DOWNLOAD",
                        request.getUserId(), null,
                        "FAILED", e.getMessage(), startTime);

            throw e;
        }
    }

    @Override
    public String getDownloadUrl(String bucketName, String objectKey, Integer expiration) {
        log.info("Dubbo: Getting download URL: {}/{}", bucketName, objectKey);

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
            log.error("Dubbo: Failed to get download URL: {}/{}", bucketName, objectKey, e);
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

    // ==================== 私有辅助方法 ====================

    /**
     * 获取存储适配器
     */
    private StorageService getStorageAdapter(Long userId, String bucketName) {
        BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);

        if (bucketConfig != null) {
            return getStorageAdapterFromConfig(bucketConfig);
        }

        // 如果数据库中没有配置，使用默认配置
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
     */
    private StorageService getStorageAdapterFromConfig(BucketConfig bucketConfig) {
        StorageType storageType = StorageType.valueOf(bucketConfig.getStorageType());

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
