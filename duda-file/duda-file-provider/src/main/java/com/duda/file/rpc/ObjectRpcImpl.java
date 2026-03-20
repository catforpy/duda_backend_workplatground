package com.duda.file.rpc;

import com.duda.file.dto.object.*;
import com.duda.file.dto.upload.MultipartUploadInfoDTO;
import com.duda.file.service.ObjectService;
import com.duda.file.service.impl.ObjectServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.Map;

/**
 * Object RPC 实现类
 * 对外提供 Dubbo RPC 服务
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@DubboService(version = "1.0.0", group = "DUDA_FILE_GROUP", timeout = 30000)
public class ObjectRpcImpl implements IObjectRpc {

    @org.springframework.beans.factory.annotation.Autowired
    private ObjectServiceImpl objectServiceImpl;

    // ==================== 基础操作 ====================

    @Override
    public void createDirectory(String bucketName, String directoryPath, Long userId) {
        log.info("【RPC】Create directory: bucket={}, path={}, userId={}", bucketName, directoryPath, userId);
        objectServiceImpl.createDirectory(bucketName, directoryPath, userId);
    }

    @Override
    public ObjectDTO getObjectInfo(String bucketName, String objectKey) {
        log.info("【RPC】Get object info: bucket={}, object={}", bucketName, objectKey);
        return objectServiceImpl.getObjectInfo(bucketName, objectKey);
    }

    @Override
    public ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey) {
        log.info("【RPC】Get object metadata: bucket={}, object={}", bucketName, objectKey);
        return objectServiceImpl.getObjectMetadata(bucketName, objectKey);
    }

    @Override
    public void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata) {
        log.info("【RPC】Set object metadata: bucket={}, object={}", bucketName, objectKey);
        objectServiceImpl.setObjectMetadata(bucketName, objectKey, metadata);
    }

    @Override
    public Boolean doesObjectExist(String bucketName, String objectKey) {
        log.info("【RPC】Check object exists: bucket={}, object={}", bucketName, objectKey);
        return objectServiceImpl.doesObjectExist(bucketName, objectKey);
    }

    // ==================== 删除操作 ====================

    @Override
    public void deleteObject(String bucketName, String objectKey, Long userId) {
        log.info("【RPC】Delete object: bucket={}, object={}, userId={}", bucketName, objectKey, userId);
        objectServiceImpl.deleteObject(bucketName, objectKey, userId);
    }

    @Override
    public BatchDeleteResultDTO deleteObjects(String bucketName, List<String> objectKeys, Long userId) {
        log.info("【RPC】Batch delete objects: bucket={}, count={}, userId={}", bucketName, objectKeys.size(), userId);
        return objectServiceImpl.deleteObjects(bucketName, objectKeys, userId);
    }

    // ==================== 复制和重命名 ====================

    @Override
    public void copyObject(String sourceBucketName, String sourceObjectKey,
                           String destinationBucketName, String destinationObjectKey,
                           Long userId) {
        log.info("【RPC】Copy object: {}:{} -> {}:{}, userId={}",
            sourceBucketName, sourceObjectKey, destinationBucketName, destinationObjectKey, userId);
        objectServiceImpl.copyObject(sourceBucketName, sourceObjectKey,
            destinationBucketName, destinationObjectKey, userId);
    }

    @Override
    public void renameObject(String bucketName, String sourceObjectKey,
                             String destinationObjectKey, Long userId) {
        log.info("【RPC】Rename object: bucket={} {} -> {}, userId={}",
            bucketName, sourceObjectKey, destinationObjectKey, userId);
        objectServiceImpl.renameObject(bucketName, sourceObjectKey, destinationObjectKey, userId);
    }

    // ==================== 列表操作 ====================

    @Override
    public ListObjectsResultDTO listObjects(ListObjectsReqDTO request) {
        log.info("【RPC】List objects: bucket={}, prefix={}", request.getBucketName(), request.getPrefix());
        return objectServiceImpl.listObjects(request);
    }

    @Override
    public List<ObjectDTO> listObjectsRecursive(String bucketName, String prefix, Integer maxKeys) {
        log.info("【RPC】List objects recursive: bucket={}, prefix={}, maxKeys={}", bucketName, prefix, maxKeys);
        return objectServiceImpl.listObjectsRecursive(bucketName, prefix, maxKeys);
    }

    // ==================== 权限管理 ====================

    @Override
    public void setObjectAcl(String bucketName, String objectKey, com.duda.file.enums.AclType aclType) {
        log.info("【RPC】Set object ACL: bucket={}, object={}, acl={}", bucketName, objectKey, aclType);
        objectServiceImpl.setObjectAcl(bucketName, objectKey, aclType);
    }

    @Override
    public com.duda.file.enums.AclType getObjectAcl(String bucketName, String objectKey) {
        log.info("【RPC】Get object ACL: bucket={}, object={}", bucketName, objectKey);
        return objectServiceImpl.getObjectAcl(bucketName, objectKey);
    }

    // ==================== 标签管理 ====================

    @Override
    public void setObjectTags(String bucketName, String objectKey, Map<String, String> tags) {
        log.info("【RPC】Set object tags: bucket={}, object={}, tagsCount={}", bucketName, objectKey, tags.size());
        objectServiceImpl.setObjectTags(bucketName, objectKey, tags);
    }

    @Override
    public Map<String, String> getObjectTags(String bucketName, String objectKey) {
        log.info("【RPC】Get object tags: bucket={}, object={}", bucketName, objectKey);
        return objectServiceImpl.getObjectTags(bucketName, objectKey);
    }

    @Override
    public void deleteObjectTags(String bucketName, String objectKey, List<String> tagKeys) {
        log.info("【RPC】Delete object tags: bucket={}, object={}, tagsCount={}", bucketName, objectKey, tagKeys.size());
        objectServiceImpl.deleteObjectTags(bucketName, objectKey, tagKeys);
    }

    // ==================== 恢复操作 ====================

    @Override
    public void restoreObject(String bucketName, String objectKey, RestoreObjectReqDTO restoreRequest) {
        log.info("【RPC】Restore object: bucket={}, object={}", bucketName, objectKey);
        objectServiceImpl.restoreObject(bucketName, objectKey, restoreRequest);
    }

    @Override
    public RestoreStatusDTO getRestoreStatus(String bucketName, String objectKey) {
        log.info("【RPC】Get restore status: bucket={}, object={}", bucketName, objectKey);
        return objectServiceImpl.getRestoreStatus(bucketName, objectKey);
    }

    // ==================== 软链接操作 ====================

    @Override
    public void createSymlink(String bucketName, String symlinkKey, String targetKey, Long userId) {
        log.info("【RPC】Create symlink: bucket={} {} -> {}, userId={}",
            bucketName, symlinkKey, targetKey, userId);
        objectServiceImpl.createSymlink(bucketName, symlinkKey, targetKey, userId);
    }

    @Override
    public String getSymlink(String bucketName, String symlinkKey) {
        log.info("【RPC】Get symlink: bucket={}, symlink={}", bucketName, symlinkKey);
        return objectServiceImpl.getSymlink(bucketName, symlinkKey);
    }

    @Override
    public void deleteSymlink(String bucketName, String symlinkKey, Long userId) {
        log.info("【RPC】Delete symlink: bucket={}, symlink={}, userId={}", bucketName, symlinkKey, userId);
        objectServiceImpl.deleteSymlink(bucketName, symlinkKey, userId);
    }

    // ==================== 分片清理 ====================

    @Override
    public Integer cleanupMultipartUploads(String bucketName, Integer daysBefore) {
        log.info("【RPC】Cleanup multipart uploads: bucket={}, daysBefore={}", bucketName, daysBefore);
        return objectServiceImpl.cleanupMultipartUploads(bucketName, daysBefore);
    }

    @Override
    public List<MultipartUploadInfoDTO> listMultipartUploads(String bucketName, String prefix, Integer maxUploads) {
        log.info("【RPC】List multipart uploads: bucket={}, prefix={}, maxUploads={}", bucketName, prefix, maxUploads);
        return objectServiceImpl.listMultipartUploads(bucketName, prefix, maxUploads);
    }

    // ==================== 工具方法 ====================

    @Override
    public Boolean checkPermission(String bucketName, String objectKey, Long userId) {
        log.info("【RPC】Check permission: bucket={}, object={}, userId={}", bucketName, objectKey, userId);
        return objectServiceImpl.checkPermission(bucketName, objectKey, userId);
    }

    @Override
    public String getFullPath(String bucketName, String objectKey) {
        log.info("【RPC】Get full path: bucket={}, object={}", bucketName, objectKey);
        return objectServiceImpl.getFullPath(bucketName, objectKey);
    }

    // ==================== 元数据操作 ====================

    @Override
    public void updateObjectMetadata(String bucketName, String objectKey, Map<String, String> metadata) {
        log.info("【RPC】Update object metadata: bucket={}, object={}", bucketName, objectKey);
        objectServiceImpl.updateObjectMetadata(bucketName, objectKey, metadata);
    }

    @Override
    public Map<String, String> getObjectMetadataMap(String bucketName, String objectKey) {
        log.info("【RPC】Get object metadata map: bucket={}, object={}", bucketName, objectKey);
        return objectServiceImpl.getObjectMetadataMap(bucketName, objectKey);
    }
}
