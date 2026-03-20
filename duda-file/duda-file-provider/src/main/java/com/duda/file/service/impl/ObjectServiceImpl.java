package com.duda.file.service.impl;

import com.duda.file.adapter.StorageService;
import com.duda.file.provider.helper.SimpleAdapterFactory;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import com.duda.file.dto.object.*;
import com.duda.file.dto.upload.MultipartUploadInfoDTO;
import com.duda.file.enums.StorageType;
import com.duda.file.manager.ObjectManager;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.provider.mapper.ObjectMetadataMapper;
import com.duda.file.provider.entity.BucketConfig;
import com.duda.file.provider.entity.ObjectMetadata;
import com.duda.file.service.ObjectService;
import com.duda.file.common.exception.StorageException;
import com.duda.file.provider.util.AesEncryptUtil;
import com.duda.user.dto.userapikey.UserApiKeyDTO;
import com.duda.user.rpc.IUserApiKeyRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Object服务实现
 * 业务逻辑实现类
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@Service
public class ObjectServiceImpl implements ObjectService {

    @Autowired
    private ObjectManager objectManager;

    @Autowired
    private SimpleAdapterFactory storageAdapterFactory;

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Autowired
    private ObjectMetadataMapper objectMetadataMapper;

    @Autowired
    private AesEncryptUtil aesEncryptUtil;

    /**
     * API密钥加密密钥(从Nacos配置中心读取)
     */
    @Value("${duda.file.encryption.key:duda-file-encryption-key}")
    private String encryptionKey;

    @DubboReference(version = "1.0.0", group = "USER_GROUP", check = false, url = "dubbo://duda-user-provider:20880")
    private IUserApiKeyRpc userApiKeyRpc;

    // ==================== 基础操作 ====================

    @Override
    public void createDirectory(String bucketName, String directoryPath, Long userId) {
        log.info("【Service】创建目录: bucketName={}, directoryPath={}, userId={}",
                 bucketName, directoryPath, userId);

        try {
            // ==================== 步骤1: 验证参数 ====================
            if (bucketName == null || bucketName.trim().isEmpty()) {
                throw new StorageException("INVALID_PARAM", "Bucket名称不能为空");
            }
            if (directoryPath == null || directoryPath.trim().isEmpty()) {
                throw new StorageException("INVALID_PARAM", "目录路径不能为空");
            }
            if (userId == null) {
                throw new StorageException("INVALID_PARAM", "用户ID不能为空");
            }

            // 确保目录路径以 "/" 结尾
            if (!directoryPath.endsWith("/")) {
                directoryPath += "/";
                log.info("自动修正目录路径，添加末尾的 /: {}", directoryPath);
            }

            // ==================== 步骤2: 验证 Bucket 配置 ====================
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND",
                    "Bucket不存在: " + bucketName);
            }

            // 验证用户权限
            if (!bucketConfig.getUserId().equals(userId)) {
                throw new StorageException("PERMISSION_DENIED",
                    "无权限操作此Bucket");
            }

            // ==================== 步骤3: 获取存储适配器 ====================
            StorageService adapter = getStorageAdapter(userId, bucketName);

            // ==================== 步骤4: 调用 OSS SDK 创建目录 ====================
            log.info("调用OSS SDK创建目录: bucketName={}, directoryPath={}",
                     bucketName, directoryPath);

            // 创建空内容的输入流
            byte[] emptyContent = new byte[0];
            java.io.InputStream emptyInputStream = new java.io.ByteArrayInputStream(emptyContent);

            // 构建元数据
            ObjectMetadataDTO metadata = ObjectMetadataDTO.builder()
                .contentLength(0L)
                .contentType("application/x-directory")
                .build();

            // 上传空对象（目录）
            adapter.uploadObject(bucketName, directoryPath, emptyInputStream, metadata);

            log.info("OSS目录创建成功: bucketName={}, directoryPath={}",
                     bucketName, directoryPath);

            // ==================== 步骤5: 保存目录元数据到数据库 ====================
            ObjectMetadata dirMetadata = ObjectMetadata.builder()
                .bucketName(bucketName)
                .objectKey(directoryPath)
                .objectType("DIRECTORY")
                .fileName(directoryPath.substring(directoryPath.lastIndexOf('/') > 0
                    ? directoryPath.lastIndexOf('/', directoryPath.length() - 2) + 1
                    : 0, directoryPath.length() - 1))
                .contentType("application/x-directory")
                .fileSize(0L)
                .storageClass(com.duda.file.enums.StorageClass.STANDARD.name())
                .acl("PRIVATE")
                .status("active")
                .createdBy(userId)
                .uploadTime(java.time.LocalDateTime.now())
                .build();

            objectMetadataMapper.insert(dirMetadata);
            log.info("数据库记录已保存: bucketName={}, directoryPath={}",
                     bucketName, directoryPath);

        } catch (Exception e) {
            log.error("创建目录失败: bucketName={}, directoryPath={}",
                      bucketName, directoryPath, e);
            throw new StorageException("CREATE_DIRECTORY_FAILED",
                "创建目录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ObjectDTO getObjectInfo(String bucketName, String objectKey) {
        log.debug("Service: Getting object info: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // 转换为DTO
            return convertToObjectDTO(metadata);

        } catch (Exception e) {
            log.error("Service: Failed to get object info: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey) {
        log.debug("Service: Getting object metadata: {}/{}", bucketName, objectKey);

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
            log.error("Service: Failed to get object metadata: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata) {
        log.info("Service: Setting object metadata: {}/{}", bucketName, objectKey);

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

            log.info("Service: Object metadata set successfully");

        } catch (Exception e) {
            log.error("Service: Failed to set object metadata: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public Boolean doesObjectExist(String bucketName, String objectKey) {
        log.debug("Service: Checking object existence: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);

            return metadata != null && "active".equals(metadata.getStatus());

        } catch (Exception e) {
            log.error("Service: Failed to check object existence: {}/{}", bucketName, objectKey, e);
            return false;
        }
    }

    // ==================== 删除操作 ====================

    @Override
    public void deleteObject(String bucketName, String objectKey, Long userId) throws StorageException {
        log.info("Service: Deleting object: {}/{}, userId: {}", bucketName, objectKey, userId);

        try {
            // 1. ✅ 权限验证（使用和 UploadServiceImpl 相同的逻辑）
            BucketConfig bucketConfig = validateAndGetBucket(userId, bucketName);

            // 2. 获取存储适配器
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 3. 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);

            // 4. 调用Manager删除对象(从云存储中删除)
            // ⚠️ 即使数据库中没有记录，也允许删除 OSS 中的文件
            if (metadata == null) {
                log.warn("Object metadata not found in database, but will delete from OSS: {}/{}", bucketName, objectKey);
            } else {
                log.info("Object metadata found in database: {}/{}", bucketName, objectKey);
            }

            // 5. 从 OSS 删除文件
            adapter.deleteObject(bucketName, objectKey);

            // 6. 如果数据库中有记录，软删除
            if (metadata != null) {
                objectMetadataMapper.deleteByBucketAndKey(bucketName, objectKey);
                log.info("Database record deleted: {}/{}", bucketName, objectKey);
            }

            log.info("Service: Object deleted successfully: {}/{}", bucketName, objectKey);

        } catch (Exception e) {
            log.error("Service: Failed to delete object: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public BatchDeleteResultDTO deleteObjects(String bucketName, List<String> objectKeys, Long userId) throws StorageException {
        log.info("Service: Batch deleting objects: {}, count: {}", bucketName, objectKeys.size());

        try {
            // 1. 获取存储适配器
            StorageService adapter = getStorageAdapter(userId, bucketName);

            // 2. 调用Manager批量删除对象
            BatchDeleteResultDTO result = objectManager.deleteObjects(bucketName, objectKeys, userId, adapter);

            // 3. 批量软删除数据库记录
            objectMetadataMapper.batchDelete(bucketName, objectKeys);

            log.info("Service: Batch delete completed: success={}, failure={}",
                    result.getSuccessCount(), result.getFailureCount());

            return result;

        } catch (Exception e) {
            log.error("Service: Failed to batch delete objects: {}", bucketName, e);
            throw e;
        }
    }

    // ==================== 复制和重命名 ====================

    @Override
    public void copyObject(String sourceBucketName, String sourceObjectKey,
                          String destinationBucketName, String destinationObjectKey,
                          Long userId) throws StorageException {
        log.info("Service: Copying object: {}/{} -> {}/{}, userId: {}",
                sourceBucketName, sourceObjectKey, destinationBucketName, destinationObjectKey, userId);

        try {
            // ✅ 1. 验证源 Bucket 权限
            BucketConfig sourceBucketConfig = validateAndGetBucket(userId, sourceBucketName);
            StorageService sourceAdapter = getStorageAdapterFromConfig(sourceBucketConfig);

            // ✅ 2. 验证目标 Bucket 权限（如果是同一个 Bucket，使用同一个适配器）
            StorageService destAdapter = sourceAdapter;
            if (!sourceBucketName.equals(destinationBucketName)) {
                BucketConfig destBucketConfig = validateAndGetBucket(userId, destinationBucketName);
                destAdapter = getStorageAdapterFromConfig(destBucketConfig);
            }

            // 3. 检查源对象是否存在（从 OSS）
            if (!sourceAdapter.doesObjectExist(sourceBucketName, sourceObjectKey)) {
                throw new StorageException("SOURCE_OBJECT_NOT_FOUND",
                    "Source object not found: " + sourceBucketName + "/" + sourceObjectKey);
            }

            // 4. 从数据库查询源对象元数据（允许不存在）
            ObjectMetadata sourceMetadata = objectMetadataMapper.selectByBucketAndKey(sourceBucketName, sourceObjectKey);

            // 5. 直接调用存储适配器复制（不使用 Manager，避免 PermissionChecker）
            sourceAdapter.copyObject(sourceBucketName, sourceObjectKey,
                                   destinationBucketName, destinationObjectKey);

            // 6. 如果源文件有元数据记录，创建目标对象的元数据记录
            if (sourceMetadata != null) {
                ObjectMetadata destMetadata = cloneObjectMetadata(sourceMetadata, destinationBucketName, destinationObjectKey, userId);
                objectMetadataMapper.insert(destMetadata);
                log.info("Database metadata copied for destination object");
            } else {
                log.warn("Source object has no metadata in database, skipping metadata copy");
            }

            log.info("Service: Object copied successfully: {}/{} -> {}/{}",
                    sourceBucketName, sourceObjectKey, destinationBucketName, destinationObjectKey);

        } catch (Exception e) {
            log.error("Service: Failed to copy object: {}/{} -> {}/{}",
                    sourceBucketName, sourceObjectKey, destinationBucketName, destinationObjectKey, e);
            throw e;
        }
    }

    @Override
    public void renameObject(String bucketName, String sourceObjectKey,
                             String destinationObjectKey, Long userId) throws StorageException {
        log.info("Service: Renaming object: {} -> {} in bucket: {}", sourceObjectKey, destinationObjectKey, bucketName);

        try {
            // 1. 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, sourceObjectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // 2. 获取存储适配器
            StorageService adapter = getStorageAdapter(userId, bucketName);

            // 3. 调用Manager重命名对象
            objectManager.renameObject(bucketName, sourceObjectKey, destinationObjectKey, userId, adapter);

            // 4. 更新数据库中的对象键
            metadata.setObjectKey(destinationObjectKey);
            objectMetadataMapper.update(metadata);

            log.info("Service: Object renamed successfully");

        } catch (Exception e) {
            log.error("Service: Failed to rename object: {} -> {} in bucket: {}", sourceObjectKey, destinationObjectKey, bucketName, e);
            throw e;
        }
    }

    // ==================== 列表操作 ====================

    @Override
    public ListObjectsResultDTO listObjects(ListObjectsReqDTO request) {
        log.info("Service: Listing objects in bucket: {}, prefix: {}", request.getBucketName(), request.getPrefix());

        try {
            String bucketName = request.getBucketName();

            // ✅ 从 OSS 读取文件列表（能看到所有文件，包括手工上传的）
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
            }

            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 从 OSS 列出对象（使用正确的方法签名）
            List<ObjectDTO> ossObjects = adapter.listObjects(
                bucketName,
                request.getPrefix(),
                request.getMaxKeys(),
                request.getMarker(),      // 分页标记
                request.getDelimiter()    // 分隔符
            );

            log.info("从 OSS 读取到 {} 个对象", ossObjects.size());

            return ListObjectsResultDTO.builder()
                .bucketName(bucketName)
                .prefix(request.getPrefix())
                .delimiter(request.getDelimiter())
                .marker(request.getMarker())
                .objects(ossObjects)
                .truncated(ossObjects.size() >= request.getMaxKeys())
                .objectCount(ossObjects.size())
                .build();

        } catch (Exception e) {
            log.error("Service: Failed to list objects in bucket: {}", request.getBucketName(), e);
            throw e;
        }
    }

    @Override
    public List<ObjectDTO> listObjectsRecursive(String bucketName, String prefix, Integer maxKeys) {
        log.debug("Service: Listing objects recursively: {}/{}", bucketName, prefix);

        try {
            // TODO: 实现递归列出对象
            return List.of();

        } catch (Exception e) {
            log.error("Service: Failed to list objects recursively: {}/{}", bucketName, prefix, e);
            throw e;
        }
    }

    // ==================== 权限管理 ====================

    @Override
    public void setObjectAcl(String bucketName, String objectKey, com.duda.file.enums.AclType aclType) {
        log.info("Service: Setting object ACL: {}/{} -> {}", bucketName, objectKey, aclType);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // 更新数据库中的ACL
            metadata.setAcl(aclType.name());
            objectMetadataMapper.update(metadata);

            log.info("Service: Object ACL set successfully");

        } catch (Exception e) {
            log.error("Service: Failed to set object ACL: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public com.duda.file.enums.AclType getObjectAcl(String bucketName, String objectKey) {
        log.debug("Service: Getting object ACL: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            return com.duda.file.enums.AclType.valueOf(metadata.getAcl());

        } catch (Exception e) {
            log.error("Service: Failed to get object ACL: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    // ==================== 标签管理 ====================

    @Override
    public void setObjectTags(String bucketName, String objectKey, Map<String, String> tags) {
        log.info("Service: Setting object tags: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // TODO: 将Map转换为JSON字符串
            metadata.setTags(tags.toString());
            objectMetadataMapper.update(metadata);

            log.info("Service: Object tags set successfully");

        } catch (Exception e) {
            log.error("Service: Failed to set object tags: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public Map<String, String> getObjectTags(String bucketName, String objectKey) {
        log.debug("Service: Getting object tags: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // TODO: 解析JSON格式的tags
            return Map.of();

        } catch (Exception e) {
            log.error("Service: Failed to get object tags: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public void deleteObjectTags(String bucketName, String objectKey, List<String> tagKeys) {
        log.info("Service: Deleting object tags: {}/{}", bucketName, objectKey);

        try {
            // TODO: 实现删除对象标签
            log.warn("deleteObjectTags not implemented yet");

        } catch (Exception e) {
            log.error("Service: Failed to delete object tags: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    // ==================== 恢复操作 ====================

    @Override
    public void restoreObject(String bucketName, String objectKey, RestoreObjectReqDTO restoreRequest) {
        log.info("Service: Restoring object: {}/{}", bucketName, objectKey);

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

            log.info("Service: Object restore started successfully");

        } catch (Exception e) {
            log.error("Service: Failed to restore object: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public RestoreStatusDTO getRestoreStatus(String bucketName, String objectKey) {
        log.debug("Service: Getting restore status: {}/{}", bucketName, objectKey);

        try {
            // TODO: 实现获取恢复状态
            return RestoreStatusDTO.builder()
                .restoreStatus("UNKNOWN")
                .build();

        } catch (Exception e) {
            log.error("Service: Failed to get restore status: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    // ==================== 软链接操作 ====================

    @Override
    public void createSymlink(String bucketName, String symlinkKey, String targetKey, Long userId) {
        log.info("Service: Creating symlink: {} -> {} in bucket: {}", symlinkKey, targetKey, bucketName);

        try {
            StorageService adapter = getStorageAdapter(userId, bucketName);

            objectManager.createSymlink(bucketName, symlinkKey, targetKey, userId, adapter);

            // 保存软链接元数据到数据库
            ObjectMetadata metadata = ObjectMetadata.builder()
                .bucketName(bucketName)
                .objectKey(symlinkKey)
                .objectType("SYMLINK")
                .isSymlink(true)
                .symlinkTarget(targetKey)
                .status("active")
                .createdBy(userId)
                .build();

            objectMetadataMapper.insert(metadata);

            log.info("Service: Symlink created successfully");

        } catch (Exception e) {
            log.error("Service: Failed to create symlink: {} -> {} in bucket: {}", symlinkKey, targetKey, bucketName, e);
            throw e;
        }
    }

    @Override
    public String getSymlink(String bucketName, String symlinkKey) {
        log.debug("Service: Getting symlink: {}/{}", bucketName, symlinkKey);

        try {
            // 从数据库查询软链接元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, symlinkKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Symlink not found");
            }

            return metadata.getSymlinkTarget();

        } catch (Exception e) {
            log.error("Service: Failed to get symlink: {}/{}", bucketName, symlinkKey, e);
            throw e;
        }
    }

    @Override
    public void deleteSymlink(String bucketName, String symlinkKey, Long userId) throws StorageException {
        log.info("Service: Deleting symlink: {}/{}", bucketName, symlinkKey);

        try {
            // TODO: 实现删除软链接
            log.warn("deleteSymlink not implemented yet");

        } catch (Exception e) {
            log.error("Service: Failed to delete symlink: {}/{}", bucketName, symlinkKey, e);
            throw e;
        }
    }

    // ==================== 分片清理 ====================

    @Override
    public Integer cleanupMultipartUploads(String bucketName, Integer daysBefore) {
        log.info("Service: Cleaning up multipart uploads: {} (older than {} days)", bucketName, daysBefore);

        try {
            // TODO: 实现清理过期分片上传
            // 返回清理的upload数量
            return 0;

        } catch (Exception e) {
            log.error("Service: Failed to cleanup multipart uploads: {}", bucketName, e);
            throw e;
        }
    }

    @Override
    public List<MultipartUploadInfoDTO> listMultipartUploads(String bucketName, String prefix, Integer maxUploads) {
        log.debug("Service: Listing multipart uploads: {}", bucketName);

        try {
            // TODO: 实现列举分片上传
            return List.of();

        } catch (Exception e) {
            log.error("Service: Failed to list multipart uploads: {}", bucketName, e);
            throw e;
        }
    }

    // ==================== 工具方法 ====================

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

    // ==================== 元数据操作 ====================

    @Override
    public void updateObjectMetadata(String bucketName, String objectKey, Map<String, String> metadata) {
        log.info("Service: Updating object metadata: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata objectMetadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (objectMetadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // TODO: 将Map转换为JSON字符串并更新
            if (metadata != null && !metadata.isEmpty()) {
                objectMetadata.setUserMetadata(metadata.toString());
                objectMetadataMapper.update(objectMetadata);
            }

            log.info("Service: Object metadata updated successfully");

        } catch (Exception e) {
            log.error("Service: Failed to update object metadata: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    @Override
    public Map<String, String> getObjectMetadataMap(String bucketName, String objectKey) {
        log.debug("Service: Getting object metadata map: {}/{}", bucketName, objectKey);

        try {
            // 从数据库查询对象元数据
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);
            if (metadata == null) {
                throw new StorageException("OBJECT_NOT_FOUND", "Object not found");
            }

            // TODO: 解析JSON格式的userMetadata
            return Map.of();

        } catch (Exception e) {
            log.error("Service: Failed to get object metadata map: {}/{}", bucketName, objectKey, e);
            throw e;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证并获取Bucket配置（与UploadServiceImpl相同的权限验证逻辑）
     */
    private BucketConfig validateAndGetBucket(Long userId, String bucketName) {
        log.info("【权限验证】开始验证 bucket 访问权限");
        log.info("【权限验证】请求 userId: {}, bucketName: {}", userId, bucketName);

        BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
        if (bucketConfig == null) {
            throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
        }

        log.info("【权限验证】Bucket配置 - userId: {}, apiKeyId: {}, isDeleted: {}",
                bucketConfig.getUserId(), bucketConfig.getApiKeyId(), bucketConfig.getIsDeleted());

        if (bucketConfig.getIsDeleted()) {
            throw new StorageException("BUCKET_DELETED", "Bucket has been deleted");
        }

        // ✅ 通过 api_key_id 验证权限
        // 验证逻辑：该 apiKey 是否属于当前用户
        if (bucketConfig.getApiKeyId() != null) {
            com.duda.user.dto.userapikey.UserApiKeyDTO apiKeyDTO = userApiKeyRpc.getUserApiKeyById(bucketConfig.getApiKeyId());

            if (apiKeyDTO == null) {
                log.error("【权限验证】API Key不存在！apiKeyId: {}", bucketConfig.getApiKeyId());
                throw new StorageException("API_KEY_NOT_FOUND", "API Key not found: " + bucketConfig.getApiKeyId());
            }

            log.info("【权限验证】API Key信息 - apiKeyId: {}, apiKeyUserId: {}, 请求userId: {}",
                    bucketConfig.getApiKeyId(), apiKeyDTO.getUserId(), userId);

            // 验证 API Key 是否属于该用户
            if (!apiKeyDTO.getUserId().equals(userId)) {
                log.error("【权限验证】权限拒绝！API Key (id={}) 属于用户 {}，但请求来自用户 {}",
                        bucketConfig.getApiKeyId(), apiKeyDTO.getUserId(), userId);
                throw new StorageException("PERMISSION_DENIED", "No permission to access bucket");
            }
        } else {
            // 如果没有 api_key_id，使用旧的验证方式（兼容性）
            log.warn("【权限验证】Bucket没有配置apiKeyId，使用旧版权限验证");
            if (!bucketConfig.getUserId().equals(userId)) {
                log.error("【权限验证】权限拒绝！请求userId: {}, Bucket的userId: {}", userId, bucketConfig.getUserId());
                throw new StorageException("PERMISSION_DENIED", "No permission to access bucket");
            }
        }

        log.info("【权限验证】权限验证通过");
        return bucketConfig;
    }

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
        StorageType storageType = StorageType.fromCode(bucketConfig.getStorageType());

        log.info("查询API密钥: bucketName={}, userId={}, apiKeyId={}, accessKeyId={}",
            bucketConfig.getBucketName(),
            bucketConfig.getUserId(),
            bucketConfig.getApiKeyId(),
            bucketConfig.getAccessKeyId());

        // 如果数据库中有 access_key_id 和 access_key_secret,直接使用
        // 否则通过 api_key_id 查询 user_api_keys 表获取
        String accessKeyId;
        String accessKeySecret;

        if (bucketConfig.getAccessKeyId() != null && !bucketConfig.getAccessKeyId().isEmpty()) {
            log.info("使用数据库中的 access_key_id");
            accessKeyId = decryptApiKey(bucketConfig.getAccessKeyId());
            accessKeySecret = decryptApiKey(bucketConfig.getAccessKeySecret());
        } else {
            // 通过 user_id 查询用户的 API 密钥
            log.info("api_key_id 为空,通过 userId={} 查询用户的 API 密钥", bucketConfig.getUserId());
            List<UserApiKeyDTO> userApiKeys = userApiKeyRpc.listUserApiKeys(bucketConfig.getUserId(), false);

            if (userApiKeys == null || userApiKeys.isEmpty()) {
                throw new StorageException("API_KEY_NOT_FOUND",
                    "用户没有配置API密钥: userId=" + bucketConfig.getUserId());
            }

            // 如果有 api_key_id,使用它过滤;否则使用第一个可用的密钥
            UserApiKeyDTO matchedApiKey = null;
            if (bucketConfig.getApiKeyId() != null) {
                matchedApiKey = userApiKeys.stream()
                    .filter(key -> key.getId().equals(bucketConfig.getApiKeyId()))
                    .findFirst()
                    .orElse(null);
            }

            if (matchedApiKey == null) {
                log.warn("未找到匹配的 api_key_id,使用用户的第一个 API �钥");
                matchedApiKey = userApiKeys.get(0);
            }

            log.info("使用 API 密钥: keyName={}, keyId={}", matchedApiKey.getKeyName(), matchedApiKey.getId());
            accessKeyId = aesEncryptUtil.decrypt(matchedApiKey.getAccessKeyId());
            accessKeySecret = aesEncryptUtil.decrypt(matchedApiKey.getAccessKeySecret());

            // 自动更新 bucket_config.api_key_id (如果与当前值不同)
            if (!matchedApiKey.getId().equals(bucketConfig.getApiKeyId())) {
                log.info("自动更新 bucket_config.api_key_id: bucketName={}, oldApiKeyId={}, newApiKeyId={}",
                         bucketConfig.getBucketName(), bucketConfig.getApiKeyId(), matchedApiKey.getId());
                bucketConfig.setApiKeyId(matchedApiKey.getId());
                bucketConfig.setUpdatedTime(LocalDateTime.now());
                bucketConfigMapper.update(bucketConfig);
            }
        }

        ApiKeyConfigDTO apiKeyConfig = ApiKeyConfigDTO.builder()
            .storageType(storageType)
            .accessKeyId(accessKeyId)
            .accessKeySecret(accessKeySecret)
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
            return aesEncryptUtil.decrypt(encryptedKey);
        } catch (Exception e) {
            log.error("解密API密钥失败", e);
            throw new StorageException("DECRYPTION_FAILED", "Failed to decrypt API key: " + e.getMessage());
        }
    }

    /**
     * 转换ObjectMetadata为ObjectDTO
     */
    private ObjectDTO convertToObjectDTO(ObjectMetadata metadata) {
        // 安全地转换存储类型枚举，避免null值导致异常
        com.duda.file.enums.StorageClass storageClass = null;
        if (metadata.getStorageClass() != null && !metadata.getStorageClass().isEmpty()) {
            try {
                storageClass = com.duda.file.enums.StorageClass.valueOf(metadata.getStorageClass());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid storage class value: {}, using default STANDARD", metadata.getStorageClass());
                storageClass = com.duda.file.enums.StorageClass.STANDARD;
            }
        } else {
            storageClass = com.duda.file.enums.StorageClass.STANDARD;
        }

        return ObjectDTO.builder()
            .bucketName(metadata.getBucketName())
            .objectKey(metadata.getObjectKey())
            .eTag(metadata.getEtag())
            .size(metadata.getFileSize())
            .storageClass(storageClass)
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
