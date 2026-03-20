package com.duda.file.api.service.impl;

import com.duda.file.dto.object.*;
import com.duda.file.dto.upload.MultipartUploadInfoDTO;
import com.duda.file.rpc.IObjectRpc;
import com.duda.file.api.service.ObjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Object 本地服务实现
 * 通过 Dubbo RPC 调用 Provider 服务
 *
 * @author DudaNexus
 * @since 2026-03-19
 */
@Slf4j
@Service("objectApiServiceImpl")
public class ObjectServiceImpl implements ObjectService {

    @DubboReference(
        version = "1.0.0",
        group = "DUDA_FILE_GROUP",
        check = false,
        timeout = 30000
    )
    private IObjectRpc objectRpc;

    // ==================== 基础操作 ====================

    @Override
    public void createDirectory(String bucketName, String directoryPath, Long userId) {
        log.info("【API Service】Creating directory: bucket={}, path={}, userId={}", bucketName, directoryPath, userId);
        objectRpc.createDirectory(bucketName, directoryPath, userId);
    }

    @Override
    public ObjectDTO getObjectInfo(String bucketName, String objectKey) {
        log.info("【API Service】Getting object info: bucket={}, object={}", bucketName, objectKey);
        return objectRpc.getObjectInfo(bucketName, objectKey);
    }

    @Override
    public ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey) {
        log.info("【API Service】Getting object metadata: bucket={}, object={}", bucketName, objectKey);
        return objectRpc.getObjectMetadata(bucketName, objectKey);
    }

    @Override
    public void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata) {
        log.info("【API Service】Setting object metadata: bucket={}, object={}", bucketName, objectKey);
        objectRpc.setObjectMetadata(bucketName, objectKey, metadata);
    }

    @Override
    public Boolean doesObjectExist(String bucketName, String objectKey) {
        log.info("【API Service】Checking object exists: bucket={}, object={}", bucketName, objectKey);
        return objectRpc.doesObjectExist(bucketName, objectKey);
    }

    // ==================== 删除操作 ====================

    @Override
    public void deleteObject(String bucketName, String objectKey, Long userId) {
        log.info("【API Service】Deleting object: bucket={}, object={}, userId={}", bucketName, objectKey, userId);
        objectRpc.deleteObject(bucketName, objectKey, userId);
    }

    @Override
    public BatchDeleteResultDTO deleteObjects(String bucketName, List<String> objectKeys, Long userId) {
        log.info("【API Service】Batch deleting objects: bucket={}, count={}, userId={}", bucketName, objectKeys.size(), userId);
        return objectRpc.deleteObjects(bucketName, objectKeys, userId);
    }

    // ==================== 复制和重命名 ====================

    @Override
    public void copyObject(String sourceBucketName, String sourceObjectKey,
                           String destinationBucketName, String destinationObjectKey,
                           Long userId) {
        log.info("【API Service】Copying object: {}:{} -> {}:{}, userId={}",
            sourceBucketName, sourceObjectKey, destinationBucketName, destinationObjectKey, userId);
        objectRpc.copyObject(sourceBucketName, sourceObjectKey,
            destinationBucketName, destinationObjectKey, userId);
    }

    @Override
    public void renameObject(String bucketName, String sourceObjectKey,
                             String destinationObjectKey, Long userId) {
        log.info("【API Service】Renaming object: bucket={} {} -> {}, userId={}",
            bucketName, sourceObjectKey, destinationObjectKey, userId);
        objectRpc.renameObject(bucketName, sourceObjectKey, destinationObjectKey, userId);
    }

    // ==================== 列表操作 ====================

    @Override
    public ListObjectsResultDTO listObjects(ListObjectsReqDTO request) {
        log.info("【API Service】Listing objects: bucket={}, prefix={}", request.getBucketName(), request.getPrefix());
        return objectRpc.listObjects(request);
    }

    @Override
    public List<ObjectDTO> listObjectsRecursive(String bucketName, String prefix, Integer maxKeys) {
        log.info("【API Service】Listing objects recursive: bucket={}, prefix={}, maxKeys={}", bucketName, prefix, maxKeys);
        return objectRpc.listObjectsRecursive(bucketName, prefix, maxKeys);
    }

    // ==================== 权限管理 ====================

    @Override
    public void setObjectAcl(String bucketName, String objectKey, com.duda.file.enums.AclType aclType) {
        log.info("【API Service】Setting object ACL: bucket={}, object={}, acl={}", bucketName, objectKey, aclType);
        objectRpc.setObjectAcl(bucketName, objectKey, aclType);
    }

    @Override
    public com.duda.file.enums.AclType getObjectAcl(String bucketName, String objectKey) {
        log.info("【API Service】Getting object ACL: bucket={}, object={}", bucketName, objectKey);
        return objectRpc.getObjectAcl(bucketName, objectKey);
    }

    // ==================== 标签管理 ====================

    @Override
    public void setObjectTags(String bucketName, String objectKey, Map<String, String> tags) {
        log.info("【API Service】Setting object tags: bucket={}, object={}, tagsCount={}", bucketName, objectKey, tags.size());
        objectRpc.setObjectTags(bucketName, objectKey, tags);
    }

    @Override
    public Map<String, String> getObjectTags(String bucketName, String objectKey) {
        log.info("【API Service】Getting object tags: bucket={}, object={}", bucketName, objectKey);
        return objectRpc.getObjectTags(bucketName, objectKey);
    }

    @Override
    public void deleteObjectTags(String bucketName, String objectKey, List<String> tagKeys) {
        log.info("【API Service】Deleting object tags: bucket={}, object={}, tagsCount={}", bucketName, objectKey, tagKeys.size());
        objectRpc.deleteObjectTags(bucketName, objectKey, tagKeys);
    }

    // ==================== 恢复操作 ====================

    @Override
    public void restoreObject(String bucketName, String objectKey, RestoreObjectReqDTO restoreRequest) {
        log.info("【API Service】Restoring object: bucket={}, object={}", bucketName, objectKey);
        objectRpc.restoreObject(bucketName, objectKey, restoreRequest);
    }

    @Override
    public RestoreStatusDTO getRestoreStatus(String bucketName, String objectKey) {
        log.info("【API Service】Getting restore status: bucket={}, object={}", bucketName, objectKey);
        return objectRpc.getRestoreStatus(bucketName, objectKey);
    }

    // ==================== 软链接操作 ====================

    @Override
    public void createSymlink(String bucketName, String symlinkKey, String targetKey, Long userId) {
        log.info("【API Service】Creating symlink: bucket={} {} -> {}, userId={}",
            bucketName, symlinkKey, targetKey, userId);
        objectRpc.createSymlink(bucketName, symlinkKey, targetKey, userId);
    }

    @Override
    public String getSymlink(String bucketName, String symlinkKey) {
        log.info("【API Service】Getting symlink: bucket={}, symlink={}", bucketName, symlinkKey);
        return objectRpc.getSymlink(bucketName, symlinkKey);
    }

    @Override
    public void deleteSymlink(String bucketName, String symlinkKey, Long userId) {
        log.info("【API Service】Deleting symlink: bucket={}, symlink={}, userId={}", bucketName, symlinkKey, userId);
        objectRpc.deleteSymlink(bucketName, symlinkKey, userId);
    }

    // ==================== 分片清理 ====================

    @Override
    public Integer cleanupMultipartUploads(String bucketName, Integer daysBefore) {
        log.info("【API Service】Cleaning up multipart uploads: bucket={}, daysBefore={}", bucketName, daysBefore);
        return objectRpc.cleanupMultipartUploads(bucketName, daysBefore);
    }

    @Override
    public List<MultipartUploadInfoDTO> listMultipartUploads(String bucketName, String prefix, Integer maxUploads) {
        log.info("【API Service】Listing multipart uploads: bucket={}, prefix={}, maxUploads={}", bucketName, prefix, maxUploads);
        return objectRpc.listMultipartUploads(bucketName, prefix, maxUploads);
    }

    // ==================== 工具方法 ====================

    @Override
    public Boolean checkPermission(String bucketName, String objectKey, Long userId) {
        log.info("【API Service】Checking permission: bucket={}, object={}, userId={}", bucketName, objectKey, userId);
        return objectRpc.checkPermission(bucketName, objectKey, userId);
    }

    @Override
    public String getFullPath(String bucketName, String objectKey) {
        log.info("【API Service】Getting full path: bucket={}, object={}", bucketName, objectKey);
        return objectRpc.getFullPath(bucketName, objectKey);
    }

    // ==================== 元数据操作 ====================

    @Override
    public void updateObjectMetadata(String bucketName, String objectKey, Map<String, String> metadata) {
        log.info("【API Service】Updating object metadata: bucket={}, object={}", bucketName, objectKey);
        objectRpc.updateObjectMetadata(bucketName, objectKey, metadata);
    }

    @Override
    public Map<String, String> getObjectMetadataMap(String bucketName, String objectKey) {
        log.info("【API Service】Getting object metadata map: bucket={}, object={}", bucketName, objectKey);
        return objectRpc.getObjectMetadataMap(bucketName, objectKey);
    }
}
