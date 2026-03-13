package com.duda.file.manager.impl;

import com.duda.file.adapter.StorageService;
import com.duda.file.dto.object.*;
import com.duda.file.manager.ObjectManager;
import com.duda.file.manager.support.ObjectKeyValidator;
import com.duda.file.manager.support.PermissionChecker;
import com.duda.file.manager.support.QuotaValidator;
import com.duda.file.common.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Object管理器实现
 * 负责Object相关的业务逻辑处理
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
@Component
public class ObjectManagerImpl implements ObjectManager {

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    private QuotaValidator quotaValidator;

    @Autowired
    private ObjectKeyValidator objectKeyValidator;

    // TODO: 注入Mapper进行数据库操作
    // @Autowired
    // private ObjectMetadataMapper objectMetadataMapper;

    @Override
    public ObjectDTO getObjectInfo(String bucketName, String objectKey, StorageService storageAdapter) {
        log.debug("Getting object info: {}/{}", bucketName, objectKey);

        // 验证对象键
        objectKeyValidator.validateObjectKeyOrThrow(objectKey);

        // 调用适配器获取对象信息
        ObjectDTO objectDTO = storageAdapter.getObjectInfo(bucketName, objectKey);

        if (objectDTO == null) {
            throw new StorageException("OBJECT_NOT_FOUND",
                    "Object not found: " + bucketName + "/" + objectKey);
        }

        // TODO: 从数据库补充元数据信息
        enrichObjectInfo(objectDTO);

        return objectDTO;
    }

    @Override
    public ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey, StorageService storageAdapter) {
        log.debug("Getting object metadata: {}/{}", bucketName, objectKey);

        // 验证对象键
        objectKeyValidator.validateObjectKeyOrThrow(objectKey);

        // 调用适配器获取元数据
        return storageAdapter.getObjectMetadata(bucketName, objectKey);
    }

    @Override
    public void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata, StorageService storageAdapter) {
        log.info("Setting object metadata: {}/{}", bucketName, objectKey);

        // 验证对象键
        objectKeyValidator.validateObjectKeyOrThrow(objectKey);

        // 调用适配器设置元数据
        storageAdapter.setObjectMetadata(bucketName, objectKey, metadata);
    }

    @Override
    public Boolean doesObjectExist(String bucketName, String objectKey, StorageService storageAdapter) {
        return storageAdapter.doesObjectExist(bucketName, objectKey);
    }

    @Override
    public void deleteObject(String bucketName, String objectKey, Long userId, StorageService storageAdapter) throws StorageException {
        log.info("Deleting object: {}/{}", bucketName, objectKey);

        // 1. 验证权限
        permissionChecker.validateObjectPermission(bucketName, objectKey, userId);

        // 2. 验证对象键
        objectKeyValidator.validateObjectKeyOrThrow(objectKey);

        // 3. 检查对象是否存在
        if (!storageAdapter.doesObjectExist(bucketName, objectKey)) {
            throw new StorageException("OBJECT_NOT_FOUND",
                    "Object not found: " + bucketName + "/" + objectKey);
        }

        // 4. 调用适配器删除对象
        storageAdapter.deleteObject(bucketName, objectKey);

        // 5. 更新数据库统计信息
        // TODO: 更新bucket_config的file_count和storage_used

        log.info("Object deleted successfully: {}/{}", bucketName, objectKey);
    }

    @Override
    public BatchDeleteResultDTO deleteObjects(String bucketName, List<String> objectKeys, Long userId, StorageService storageAdapter) throws StorageException {
        log.info("Batch deleting objects: {}, count: {}", bucketName, objectKeys.size());

        // 1. 验证权限
        permissionChecker.validateBucketPermission(bucketName, userId);

        // 2. 验证所有对象键
        if (!objectKeyValidator.validateObjectKeys(objectKeys)) {
            throw new StorageException("INVALID_OBJECT_KEYS", "Invalid object keys in batch delete request");
        }

        // 3. 调用适配器批量删除
        int successCount = storageAdapter.deleteObjects(bucketName, objectKeys);

        // 4. 构建删除结果
        BatchDeleteResultDTO result = BatchDeleteResultDTO.builder()
                .bucketName(bucketName)
                .totalRequested(objectKeys.size())
                .successCount(successCount)
                .failureCount(objectKeys.size() - successCount)
                .allSuccess(successCount == objectKeys.size())
                .deletedKeys(objectKeys.subList(0, successCount))
                .build();

        // 5. 更新数据库统计信息
        // TODO: 更新bucket_config的file_count和storage_used

        log.info("Batch delete completed: success={}, failure={}", successCount, result.getFailureCount());
        return result;
    }

    @Override
    public void copyObject(String sourceBucketName, String sourceObjectKey,
                          String destinationBucketName, String destinationObjectKey,
                          Long userId, StorageService storageAdapter) throws StorageException {
        log.info("Copying object: {}/{} -> {}/{}",
                sourceBucketName, sourceObjectKey, destinationBucketName, destinationObjectKey);

        // 1. 验证权限
        permissionChecker.validateObjectPermission(sourceBucketName, sourceObjectKey, userId);
        permissionChecker.validateBucketPermission(destinationBucketName, userId);

        // 2. 验证对象键
        objectKeyValidator.validateObjectKeyOrThrow(sourceObjectKey);
        objectKeyValidator.validateObjectKeyOrThrow(destinationObjectKey);

        // 3. 检查源对象是否存在
        if (!storageAdapter.doesObjectExist(sourceBucketName, sourceObjectKey)) {
            throw new StorageException("SOURCE_OBJECT_NOT_FOUND",
                    "Source object not found: " + sourceBucketName + "/" + sourceObjectKey);
        }

        // 4. 检查目标对象是否已存在
        if (storageAdapter.doesObjectExist(destinationBucketName, destinationObjectKey)) {
            log.warn("Destination object already exists, will be overwritten: {}/{}",
                    destinationBucketName, destinationObjectKey);
        }

        // 5. 验证目标Bucket配额
        // TODO: 查询目标Bucket当前使用量和配额
        // Long destBucketSize = getBucketStorageUsed(destinationBucketName);
        // Long destBucketMaxSize = getBucketStorageMaxSize(destinationBucketName);
        // Long sourceObjectSize = storageAdapter.getObjectInfo(sourceBucketName, sourceObjectKey).getSize();
        // quotaValidator.validateBucketQuota(destinationBucketName, destBucketSize, null,
        //         destBucketMaxSize, null, sourceObjectSize);

        // 6. 调用适配器复制对象
        storageAdapter.copyObject(sourceBucketName, sourceObjectKey, destinationBucketName, destinationObjectKey);

        // 7. 更新数据库统计信息
        // TODO: 更新两个bucket_config的file_count和storage_used

        log.info("Object copied successfully");
    }

    @Override
    public void renameObject(String bucketName, String sourceObjectKey,
                            String destinationObjectKey,
                            Long userId, StorageService storageAdapter) throws StorageException {
        log.info("Renaming object: {}/{} -> {}/{}", bucketName, sourceObjectKey, destinationObjectKey);

        // 重命名 = 复制 + 删除
        // 1. 先复制
        copyObject(bucketName, sourceObjectKey, bucketName, destinationObjectKey, userId, storageAdapter);

        // 2. 再删除源文件
        deleteObject(bucketName, sourceObjectKey, userId, storageAdapter);

        log.info("Object renamed successfully");
    }

    @Override
    public ListObjectsResultDTO listObjects(ListObjectsReqDTO request, StorageService storageAdapter) {
        log.debug("Listing objects: bucket={}, prefix={}", request.getBucketName(), request.getPrefix());

        // 验证前缀
        if (request.getPrefix() != null && !objectKeyValidator.validatePrefix(request.getPrefix())) {
            throw new StorageException("INVALID_PREFIX", "Invalid prefix: " + request.getPrefix());
        }

        // 调用适配器列出对象
        List<ObjectDTO> objects = storageAdapter.listObjects(
                request.getBucketName(),
                request.getPrefix(),
                request.getMaxKeys(),
                request.getMarker(),
                request.getDelimiter()
        );

        // 构建结果
        ListObjectsResultDTO result = ListObjectsResultDTO.builder()
                .bucketName(request.getBucketName())
                .prefix(request.getPrefix())
                .delimiter(request.getDelimiter())
                .marker(request.getMarker())
                .objects(objects)
                .objectCount(objects.size())
                .truncated(false) // TODO: 检查是否还有更多对象
                .build();

        // 分离对象和目录
        List<ObjectDTO> fileList = new ArrayList<>();
        List<String> dirList = new ArrayList<>();

        for (ObjectDTO obj : objects) {
            if (obj.getIsDirectory() != null && obj.getIsDirectory()) {
                dirList.add(obj.getObjectKey());
            } else {
                fileList.add(obj);
            }
        }

        result.setObjects(fileList);
        result.setCommonPrefixes(dirList);
        result.setObjectCount(fileList.size());

        return result;
    }

    @Override
    public List<ObjectDTO> listObjectsRecursive(String bucketName, String prefix, Integer maxKeys, StorageService storageAdapter) {
        log.debug("Listing objects recursively: bucket={}, prefix={}", bucketName, prefix);

        // 递归列出所有对象(不使用delimiter)
        List<ObjectDTO> allObjects = new ArrayList<>();
        String marker = null;

        do {
            List<ObjectDTO> objects = storageAdapter.listObjects(
                    bucketName,
                    prefix,
                    maxKeys,
                    marker,
                    null // 不使用delimiter,递归列出
            );

            if (objects == null || objects.isEmpty()) {
                break;
            }

            // 过滤掉目录(以/结尾的)
            List<ObjectDTO> fileObjects = objects.stream()
                    .filter(obj -> obj.getIsDirectory() == null || !obj.getIsDirectory())
                    .collect(Collectors.toList());

            allObjects.addAll(fileObjects);

            // 检查是否还有更多对象
            if (objects.size() < (maxKeys != null ? maxKeys : 100)) {
                break;
            }

            // 获取最后一个对象的key作为下一页的marker
            marker = objects.get(objects.size() - 1).getObjectKey();

        } while (true);

        return allObjects;
    }

    @Override
    public void setObjectAcl(String bucketName, String objectKey, com.duda.file.enums.AclType aclType, StorageService storageAdapter) {
        log.info("Setting object ACL: {}/{} -> {}", bucketName, objectKey, aclType);

        // 验证对象键
        objectKeyValidator.validateObjectKeyOrThrow(objectKey);

        // 调用适配器设置ACL
        storageAdapter.setObjectAcl(bucketName, objectKey, aclType.name());
    }

    @Override
    public com.duda.file.enums.AclType getObjectAcl(String bucketName, String objectKey, StorageService storageAdapter) {
        String acl = storageAdapter.getObjectAcl(bucketName, objectKey);
        return com.duda.file.enums.AclType.valueOf(acl);
    }

    @Override
    public void setObjectTags(String bucketName, String objectKey, java.util.Map<String, String> tags, StorageService storageAdapter) {
        log.info("Setting object tags: {}/{}", bucketName, objectKey);

        // 验证对象键
        objectKeyValidator.validateObjectKeyOrThrow(objectKey);

        // TODO: 调用适配器设置标签
        // storageAdapter.setObjectTags(bucketName, objectKey, tags);
    }

    @Override
    public java.util.Map<String, String> getObjectTags(String bucketName, String objectKey, StorageService storageAdapter) {
        // TODO: 从数据库或适配器获取标签
        // return storageAdapter.getObjectTags(bucketName, objectKey);
        return new java.util.HashMap<>();
    }

    @Override
    public void restoreObject(String bucketName, String objectKey, RestoreObjectReqDTO restoreRequest, StorageService storageAdapter) throws StorageException {
        log.info("Restoring object: {}/{}", bucketName, objectKey);

        // 验证对象键
        objectKeyValidator.validateObjectKeyOrThrow(objectKey);

        // 检查对象是否存在
        if (!storageAdapter.doesObjectExist(bucketName, objectKey)) {
            throw new StorageException("OBJECT_NOT_FOUND",
                    "Object not found: " + bucketName + "/" + objectKey);
        }

        // 调用适配器恢复对象
        storageAdapter.restoreObject(bucketName, objectKey, restoreRequest.getDays());

        log.info("Object restore initiated: {}/{}", bucketName, objectKey);
    }

    @Override
    public RestoreStatusDTO getRestoreStatus(String bucketName, String objectKey, StorageService storageAdapter) {
        log.debug("Getting restore status: {}/{}", bucketName, objectKey);

        // TODO: 查询恢复状态
        // 可以从对象元数据中获取恢复状态
        ObjectMetadataDTO metadata = storageAdapter.getObjectMetadata(bucketName, objectKey);

        return RestoreStatusDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .restoreStatus(metadata != null && metadata.getIsArchive() != null ? "IN_PROGRESS" : "COMPLETED")
                .build();
    }

    @Override
    public void createSymlink(String bucketName, String symlinkKey, String targetKey, Long userId, StorageService storageAdapter) throws StorageException {
        log.info("Creating symlink: {} -> {}", symlinkKey, targetKey);

        // 验证权限
        permissionChecker.validateObjectPermission(bucketName, symlinkKey, userId);

        // 验证对象键
        objectKeyValidator.validateObjectKeyOrThrow(symlinkKey);
        objectKeyValidator.validateObjectKeyOrThrow(targetKey);

        // 检查目标对象是否存在
        if (!storageAdapter.doesObjectExist(bucketName, targetKey)) {
            throw new StorageException("TARGET_OBJECT_NOT_FOUND",
                    "Target object not found: " + bucketName + "/" + targetKey);
        }

        // 调用适配器创建软链接
        storageAdapter.createSymlink(bucketName, symlinkKey, targetKey);

        log.info("Symlink created successfully: {}", symlinkKey);
    }

    @Override
    public String getSymlink(String bucketName, String symlinkKey, StorageService storageAdapter) {
        log.debug("Getting symlink target: {}/{}", bucketName, symlinkKey);

        return storageAdapter.getSymlink(bucketName, symlinkKey);
    }

    @Override
    public void deleteSymlink(String bucketName, String symlinkKey, Long userId, StorageService storageAdapter) throws StorageException {
        log.info("Deleting symlink: {}/{}", bucketName, symlinkKey);

        // 删除软链接就是删除对象(软链接本身就是一个对象)
        deleteObject(bucketName, symlinkKey, userId, storageAdapter);
    }

    @Override
    public Boolean checkPermission(String bucketName, String objectKey, Long userId) {
        return permissionChecker.checkObjectPermission(bucketName, objectKey, userId);
    }

    @Override
    public String getFullPath(String bucketName, String objectKey) {
        return bucketName + "/" + objectKey;
    }

    // ==================== 目录操作 ====================

    @Override
    public void createDirectory(String bucketName, String directoryPath, Long userId, StorageService storageAdapter) throws StorageException {
        log.info("Creating directory: {}/{}", bucketName, directoryPath);

        // 验证权限
        permissionChecker.validateBucketPermission(bucketName, userId);

        // 规范化目录路径
        String normalizedPath = objectKeyValidator.normalizeObjectKey(directoryPath);
        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        // OSS没有真正的目录,目录是通过对象键的前缀模拟的
        // 创建一个以/结尾的空对象表示目录
        // TODO: 上传一个空对象,键为directoryPath + "/"

        log.info("Directory created: {}/{}", bucketName, normalizedPath);
    }

    @Override
    public void deleteDirectory(String bucketName, String directoryPath, Long userId, Boolean recursive, StorageService storageAdapter) throws StorageException {
        log.info("Deleting directory: {}/{} (recursive: {})", bucketName, directoryPath, recursive);

        // 验证权限
        permissionChecker.validateBucketPermission(bucketName, userId);

        // 规范化目录路径
        String normalizedPath = objectKeyValidator.normalizeObjectKey(directoryPath);
        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        if (recursive) {
            // 递归删除: 删除所有以该前缀开头的对象
            List<ObjectDTO> objects = listObjectsRecursive(bucketName, normalizedPath, null, storageAdapter);
            List<String> objectKeys = objects.stream()
                    .map(ObjectDTO::getObjectKey)
                    .collect(Collectors.toList());

            if (!objectKeys.isEmpty()) {
                deleteObjects(bucketName, objectKeys, userId, storageAdapter);
            }
        } else {
            // 非递归: 只删除目录对象本身(以/结尾的对象)
            if (storageAdapter.doesObjectExist(bucketName, normalizedPath)) {
                deleteObject(bucketName, normalizedPath, userId, storageAdapter);
            }
        }

        log.info("Directory deleted: {}/{}", bucketName, normalizedPath);
    }

    @Override
    public void renameDirectory(String bucketName, String sourceDirectoryPath, String destinationDirectoryPath, Long userId, StorageService storageAdapter) throws StorageException {
        log.info("Renaming directory: {} -> {}", sourceDirectoryPath, destinationDirectoryPath);

        // 重命名目录需要:
        // 1. 列出所有以源目录路径为前缀的对象
        // 2. 对每个对象,复制到新路径
        // 3. 删除所有旧对象

        // 1. 列出所有对象
        List<ObjectDTO> objects = listObjectsRecursive(bucketName, sourceDirectoryPath, null, storageAdapter);

        // 2. 复制所有对象到新路径
        for (ObjectDTO obj : objects) {
            String oldKey = obj.getObjectKey();
            String newKey = oldKey.replace(sourceDirectoryPath, destinationDirectoryPath);

            copyObject(bucketName, oldKey, bucketName, newKey, userId, storageAdapter);
        }

        // 3. 删除旧对象
        deleteDirectory(bucketName, sourceDirectoryPath, userId, true, storageAdapter);

        log.info("Directory renamed successfully");
    }

    @Override
    public DirectoryStatisticsDTO getDirectoryStatistics(String bucketName, String directoryPath, StorageService storageAdapter) {
        log.debug("Getting directory statistics: {}/{}", bucketName, directoryPath);

        // 规范化目录路径
        String normalizedPath = objectKeyValidator.normalizeObjectKey(directoryPath);
        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        // 递归列出所有对象
        List<ObjectDTO> objects = listObjectsRecursive(bucketName, normalizedPath, null, storageAdapter);

        // 计算统计信息
        long totalSize = 0L;
        int fileCount = objects.size();
        long maxSize = 0L;
        long minSize = Long.MAX_VALUE;

        for (ObjectDTO obj : objects) {
            Long size = obj.getSize();
            if (size != null) {
                totalSize += size;
                if (size > maxSize) {
                    maxSize = size;
                }
                if (size < minSize) {
                    minSize = size;
                }
            }
        }

        if (fileCount == 0) {
            minSize = 0L;
        }

        return DirectoryStatisticsDTO.builder()
                .bucketName(bucketName)
                .directoryPath(normalizedPath)
                .fileCount((long) fileCount)
                .directoryCount(0L) // TODO: 统计子目录数量
                .totalSize(totalSize)
                .averageSize(fileCount > 0 ? totalSize / fileCount : 0L)
                .maxSize(maxSize)
                .minSize(minSize)
                .statisticsTime(System.currentTimeMillis())
                .realTime(true)
                .build();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 从数据库补充对象信息
     */
    private void enrichObjectInfo(ObjectDTO objectDTO) {
        // TODO: 从数据库查询额外的元数据信息
        log.debug("Enriching object info: {}/{}", objectDTO.getBucketName(), objectDTO.getObjectKey());
    }
}
