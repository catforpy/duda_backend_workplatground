package com.duda.file.provider.impl;

import com.duda.file.adapter.StorageService;
import com.duda.file.provider.helper.SimpleAdapterFactory;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import com.duda.file.dto.object.*;
import com.duda.file.enums.StorageType;
import com.duda.file.manager.ObjectManager;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.provider.mapper.ObjectMetadataMapper;
import com.duda.file.provider.entity.BucketConfig;
import com.duda.file.provider.entity.ObjectMetadata;
import com.duda.file.service.ObjectService;
import com.duda.file.common.exception.StorageException;
import com.duda.file.common.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Object服务实现
 * Dubbo服务实现类,对外提供Object管理服务
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
@DubboService(version = "1.0.0", timeout = 30000)
public class ObjectServiceImpl implements ObjectService {

    @Autowired
    private ObjectManager objectManager;

    @Autowired
    private SimpleAdapterFactory storageAdapterFactory;

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Autowired
    private ObjectMetadataMapper objectMetadataMapper;

    /**
     * API密钥加密密钥(从Nacos配置中心读取)
     */
    @Value("${duda.file.encryption.key:duda-file-encryption-key}")
    private String encryptionKey;

    @Override
    public ObjectDTO getObjectInfo(String bucketName, String objectKey) {
        log.debug("Dubbo: Getting object info: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // 转换为DTO
            return convertToObjectDTO(metadata);

        } catch (Exception e) {
            log.error("Dubbo: Failed to get object info: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey) {
        log.debug("Dubbo: Getting object metadata: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // 更新访问统计
            objectMetadataMapper.updateAccessStats(bucketName, objectKey);

            // 转换为DTO
            return convertToObjectMetadataDTO(metadata);

        } catch (Exception e) {
            log.error("Dubbo: Failed to get object metadata: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata) {
        log.info("Dubbo: Setting object metadata: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata objectMetadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (objectMetadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // 更新元数据
            if (metadata.getContentType() != null) {
                objectMetadata.setContentType(metadata.getContentType());
            }
            if (metadata.getUserMetadata() != null) {
                objectMetadata.setUserMetadata(metadata.getUserMetadata().toString());
            }

            objectMetadataMapper.update(objectMetadata);

            log.info("Dubbo: Object metadata set successfully");

        } catch (Exception e) {
            log.error("Dubbo: Failed to set object metadata: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public Boolean doesObjectExist(String bucketName, String objectKey) {
        log.debug("Dubbo: Checking object existence: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);

            return metadata != null && "active".equals(metadata.getStatus());

        } catch (Exception e) {
            log.error("Dubbo: Failed to check object existence: {}/{}", bucketName, objectKey, e);
            return false;
        }
    }

    @Override
    public void deleteObject(String bucketName, String objectKey, Long userId) throws StorageException {
        log.info("Dubbo: Deleting object: {}/{}", bucketName, objectKey);

        try {
            // 1. 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // 2. 获取存储适配器
            StorageService adapter = getStorageAdapter(userId, bucketName);

            // 3. 调用Manager删除对象（从云存储中删除）
            objectManager.deleteObject(bucketName, objectKey, userId, adapter);

            // 4. 软删除数据库记录
            objectMetadataMapper.deleteByBucketAndKey(bucketName, objectKey);

            log.info("Dubbo: Object deleted successfully: {}/{}", bucketName, objectKey);

        } catch (Exception e) {
            log.error("Dubbo: Failed to delete object: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public BatchDeleteResultDTO deleteObjects(String bucketName, List<String> objectKeys, Long userId) throws StorageException {
        log.info("Dubbo: Batch deleting objects: {}, count: {}", bucketName, objectKeys.size());

        try {
            // 1. 获取存储适配器
            StorageService adapter = getStorageAdapter(userId, bucketName);

            // 2. 调用Manager批量删除对象
            BatchDeleteResultDTO result = objectManager.deleteObjects(bucketName, objectKeys, userId, adapter);

            // 3. 批量软删除数据库记录
            objectMetadataMapper.batchDelete(bucketName, objectKeys);

            log.info("Dubbo: Batch delete completed: success={}, failure={}",
                    result.getSuccessCount(), result.getFailureCount());

            return result;

        } catch (Exception e) {
            log.error("Dubbo: Failed to batch delete objects: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public void copyObject(String sourceBucketName, String sourceObjectKey,
                          String destinationBucketName, String destinationObjectKey,
                          Long userId) throws StorageException {
        log.info("Dubbo: Copying object: {}/{} -> {}/{}",
                sourceBucketName, sourceObjectKey, destinationBucketName, destinationObjectKey);

        try {
            // 1. 从数据库查询源对象元数据
            ObjectMetadata sourceMetadata = objectMetadataMapper.selectByBucketAndKey(sourceBucketName, sourceObjectKey);
            if (sourceMetadata == null) {
                throw new StorageException("SOURCE_OBJECT_NOT_FOUND", "Source object not found");
            }

            // 2. 获取存储适配器
            StorageService adapter = getStorageAdapter(userId, sourceBucketName);

            // 3. 调用Manager复制对象
            objectManager.copyObject(sourceBucketName, sourceObjectKey,
                                   destinationBucketName, destinationObjectKey,
                                   userId, adapter);

            // 4. 创建目标对象的元数据记录
            ObjectMetadata destMetadata = cloneObjectMetadata(sourceMetadata, destinationBucketName, destinationObjectKey, userId);
            objectMetadataMapper.insert(destMetadata);

            log.info("Dubbo: Object copied successfully");

        } catch (Exception e) {
            log.error("Dubbo: Failed to copy object: {}/{} -> {}/{}",
                    sourceBucketName, sourceObjectKey, destinationBucketName, destinationObjectKey, e);
            throw e;
        }
    }

    @Override
    public void renameObject(String bucketName, String objectKey, String newObjectKey, Long userId) throws StorageException {
        log.info("Dubbo: Renaming object: {} -> {} in bucket: {}", objectKey, newObjectKey, bucketName);

        try {
            // 1. 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // 2. 获取存储适配器
            StorageService adapter = getStorageAdapter(userId, bucketName);

            // 3. 调用Manager重命名对象
            objectManager.renameObject(bucketName, objectKey, newObjectKey, userId, adapter);

            // 4. 更新数据库中的对象键
            metadata.setObjectKey(newObjectKey);
            objectMetadataMapper.update(metadata);

            log.info("Dubbo: Object renamed successfully");

        } catch (Exception e) {
            log.error("Dubbo: Failed to rename object: {} -> {} in bucket: {}", objectKey, newObjectKey, bucketName, e);
            throw e;
        }
    }

    @Override
    public ListObjectsResultDTO listObjects(ListObjectsReqDTO request) {
        log.debug("Dubbo: Listing objects in bucket: {}", request.getBucketName());

        try {
            String bucketName = request.getBucketName();
            // 从数据库查询对象列表
            List<ObjectMetadata> metadataList;

            if (StringUtils.hasText(request.getPrefix())) {
                // 按前缀查询
                metadataList = objectMetadataMapper.selectByBucketAndPrefix(
                    bucketName, request.getPrefix(), request.getMaxKeys());
            } else {
                // 查询所有对象
                metadataList = objectMetadataMapper.selectByBucketName(bucketName);
            }

            // 转换为DTO列表
            List<ObjectDTO> objects = metadataList.stream()
                .filter(m -> "active".equals(m.getStatus()))
                .map(this::convertToObjectDTO)
                .toList();

            return ListObjectsResultDTO.builder()
                .bucketName(bucketName)
                .objects(objects)
                .truncated(false)
                .build();

        } catch (Exception e) {
            log.error("Dubbo: Failed to list objects in bucket: {}", request.getBucketName(), e);
            throw e;
        }
    }

    @Override
    public List<ObjectDTO> listObjectsRecursive(String bucketName, String prefix, Integer maxKeys) {
        log.debug("Dubbo: Listing objects recursively: {}/{}", bucketName, prefix);

        try {
            // TODO: 实现递归列出对象
            return List.of();

        } catch (Exception e) {
            log.error("Dubbo: Failed to list objects recursively: {}/{}", bucketName, prefix, e);
            throw e;
        }
    }

    public List<String> listObjectVersions(String bucketName, String objectKey) {
        log.debug("Dubbo: Listing object versions: {}/{}", bucketName, objectKey);

        try {
            // TODO: 实现版本列表查询
            return List.of();

        } catch (Exception e) {
            log.error("Dubbo: Failed to list object versions: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    public void setObjectAcl(String bucketName, String objectKey, com.duda.file.enums.AclType aclType) {
        log.info("Dubbo: Setting object ACL: {}/{} -> {}", bucketName, objectKey, aclType);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // 更新数据库中的ACL
            metadata.setAcl(aclType.name());
            objectMetadataMapper.update(metadata);

            log.info("Dubbo: Object ACL set successfully");

        } catch (Exception e) {
            log.error("Dubbo: Failed to set object ACL: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    public com.duda.file.enums.AclType getObjectAcl(String bucketName, String objectKey) {
        log.debug("Dubbo: Getting object ACL: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            return com.duda.file.enums.AclType.valueOf(metadata.getAcl());

        } catch (Exception e) {
            log.error("Dubbo: Failed to get object ACL: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public void setObjectTags(String bucketName, String objectKey, Map<String, String> tags) {
        log.info("Dubbo: Setting object tags: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // TODO: 将Map转换为JSON字符串
            metadata.setTags(tags.toString());
            objectMetadataMapper.update(metadata);

            log.info("Dubbo: Object tags set successfully");

        } catch (Exception e) {
            log.error("Dubbo: Failed to set object tags: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public Map<String, String> getObjectTags(String bucketName, String objectKey) {
        log.debug("Dubbo: Getting object tags: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // TODO: 解析JSON格式的tags
            return Map.of();

        } catch (Exception e) {
            log.error("Dubbo: Failed to get object tags: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public void deleteObjectTags(String bucketName, String objectKey, List<String> tagKeys) {
        log.info("Dubbo: Deleting object tags: {}/{}", bucketName, objectKey);

        try {
            // TODO: 实现删除对象标签
            log.warn("deleteObjectTags not implemented yet");

        } catch (Exception e) {
            log.error("Dubbo: Failed to delete object tags: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public void restoreObject(String bucketName, String objectKey, RestoreObjectReqDTO restoreRequest) {
        log.info("Dubbo: Restoring object: {}/{}", bucketName, objectKey);

        try {
            Long userId = 1L; // TODO: 从上下文获取
            StorageService adapter = getStorageAdapter(userId, bucketName);

            objectManager.restoreObject(bucketName, objectKey, restoreRequest, adapter);

            // 更新数据库中的恢复状态
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata != null) {
                metadata.setRestoreStatus("IN_PROGRESS");
                objectMetadataMapper.update(metadata);
            }

            log.info("Dubbo: Object restore started successfully");

        } catch (Exception e) {
            log.error("Dubbo: Failed to restore object: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public RestoreStatusDTO getRestoreStatus(String bucketName, String objectKey) {
        log.debug("Dubbo: Getting restore status: {}/{}", bucketName, objectKey);

        try {
            // TODO: 实现获取恢复状态
            return RestoreStatusDTO.builder()
                .restoreStatus("UNKNOWN")
                .build();

        } catch (Exception e) {
            log.error("Dubbo: Failed to get restore status: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public void createSymlink(String bucketName, String symlinkName, String targetName, Long userId) {
        log.info("Dubbo: Creating symlink: {} -> {} in bucket: {}", symlinkName, targetName, bucketName);

        try {
            StorageService adapter = getStorageAdapter(userId, bucketName);

            objectManager.createSymlink(bucketName, symlinkName, targetName, userId, adapter);

            // 保存软链接元数据到数据库
            ObjectMetadata metadata = ObjectMetadata.builder()
                .bucketName(bucketName)
                .objectKey(symlinkName)
                .objectType("SYMLINK")
                .isSymlink(true)
                .symlinkTarget(targetName)
                .status("active")
                .createdBy(userId)
                .build();

            objectMetadataMapper.insert(metadata);

            log.info("Dubbo: Symlink created successfully");

        } catch (Exception e) {
            log.error("Dubbo: Failed to create symlink: {} -> {} in bucket: {}", symlinkName, targetName, bucketName, e);
            throw e;
        }
    }

    @Override
    public void deleteSymlink(String bucketName, String symlinkKey, Long userId) throws StorageException {
        log.info("Dubbo: Deleting symlink: {}/{}", bucketName, symlinkKey);

        try {
            // TODO: 实现删除软链接
            log.warn("deleteSymlink not implemented yet");

        } catch (Exception e) {
            log.error("Dubbo: Failed to delete symlink: {}/{}", bucketName, symlinkKey, e);
            throw e;
        }
    }

    @Override
    public String getSymlink(String bucketName, String symlinkKey) {
        log.debug("Dubbo: Getting symlink: {}/{}", bucketName, symlinkKey);

        try {
            // 从数据库查询软链接元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, symlinkKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Symlink not found");
            }

            return metadata.getSymlinkTarget();

        } catch (Exception e) {
            log.error("Dubbo: Failed to get symlink: {}/{}", bucketName, symlinkKey, e);
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
    public String getFullPath(String bucketName, String objectKey) {
        return objectManager.getFullPath(bucketName, objectKey);
    }

    @Override
    public java.util.List<com.duda.file.dto.upload.MultipartUploadInfoDTO> listMultipartUploads(
            String bucketName, String prefix, Integer maxUploads) {
        log.debug("Dubbo: Listing multipart uploads: {}", bucketName);

        try {
            // TODO: 实现列举分片上传
            return java.util.List.of();

        } catch (Exception e) {
            log.error("Dubbo: Failed to list multipart uploads: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public Integer cleanupMultipartUploads(String bucketName, Integer daysBefore) {
        log.info("Dubbo: Cleaning up multipart uploads: {} (older than {} days)", bucketName, daysBefore);

        try {
            // TODO: 实现清理过期分片上传
            // 返回清理的upload数量
            return 0;

        } catch (Exception e) {
            log.error("Dubbo: Failed to cleanup multipart uploads: {}", bucketName, e);
            throw e;
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
     * 转换ObjectMetadata为ObjectDTO
     */
    private ObjectDTO convertToObjectDTO(ObjectMetadata metadata) {
        return ObjectDTO.builder()
            .bucketName(metadata.getBucketName())
            .objectKey(metadata.getObjectKey())
            .eTag(metadata.getEtag())
            .size(metadata.getFileSize())
            .storageClass(com.duda.file.enums.StorageClass.valueOf(metadata.getStorageClass()))
            .lastModified(metadata.getUpdatedTime())
            .build();
    }

    /**
     * 转换ObjectMetadata为ObjectMetadataDTO
     */
    private ObjectMetadataDTO convertToObjectMetadataDTO(ObjectMetadata metadata) {
        return ObjectMetadataDTO.builder()
            .contentType(metadata.getContentType())
            .contentLength(metadata.getFileSize())
            .eTag(metadata.getEtag())
            .lastModified(metadata.getUpdatedTime())
            .userMetadata(Map.of())  // TODO: 解析JSON
            .build();
    }

    /**
     * 克隆对象元数据
     */
    private ObjectMetadata cloneObjectMetadata(ObjectMetadata source, String bucketName, String objectKey, Long userId) {
        return ObjectMetadata.builder()
            .bucketName(bucketName)
            .objectKey(objectKey)
            .fileSize(source.getFileSize())
            .fileName(source.getFileName())
            .contentType(source.getContentType())
            .contentMd5(source.getContentMd5())
            .crc64(source.getCrc64())
            .storageClass(source.getStorageClass())
            .objectType(source.getObjectType())
            .acl(source.getAcl())
            .etag(source.getEtag())
            .userMetadata(source.getUserMetadata())
            .tags(source.getTags())
            .status("active")
            .createdBy(userId)
            .uploadTime(LocalDateTime.now())
            .build();
    }
}
