package com.duda.file.service;

import com.duda.file.dto.object.*;
import com.duda.file.dto.upload.MultipartUploadInfoDTO;

import java.util.List;
import java.util.Map;

/**
 * Object Service 接口
 * 内部业务逻辑接口
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
public interface ObjectService {

    /**
     * 创建目录
     *
     * @param bucketName Bucket名称
     * @param directoryPath 目录路径
     * @param userId 用户ID
     */
    void createDirectory(String bucketName, String directoryPath, Long userId);

    ObjectDTO getObjectInfo(String bucketName, String objectKey);

    ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey);

    void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata);

    Boolean doesObjectExist(String bucketName, String objectKey);

    void deleteObject(String bucketName, String objectKey, Long userId);

    BatchDeleteResultDTO deleteObjects(String bucketName, List<String> objectKeys, Long userId);

    void copyObject(String sourceBucketName, String sourceObjectKey,
                    String destinationBucketName, String destinationObjectKey,
                    Long userId);

    void renameObject(String bucketName, String sourceObjectKey,
                      String destinationObjectKey, Long userId);

    ListObjectsResultDTO listObjects(ListObjectsReqDTO request);

    List<ObjectDTO> listObjectsRecursive(String bucketName, String prefix, Integer maxKeys);

    void setObjectAcl(String bucketName, String objectKey, com.duda.file.enums.AclType aclType);

    com.duda.file.enums.AclType getObjectAcl(String bucketName, String objectKey);

    Boolean checkPermission(String bucketName, String objectKey, Long userId);

    String getFullPath(String bucketName, String objectKey);

    // ==================== 标签管理 ====================

    void setObjectTags(String bucketName, String objectKey, Map<String, String> tags);

    Map<String, String> getObjectTags(String bucketName, String objectKey);

    void deleteObjectTags(String bucketName, String objectKey, List<String> tagKeys);

    // ==================== 恢复操作 ====================

    void restoreObject(String bucketName, String objectKey, RestoreObjectReqDTO restoreRequest);

    RestoreStatusDTO getRestoreStatus(String bucketName, String objectKey);

    // ==================== 软链接操作 ====================

    void createSymlink(String bucketName, String symlinkKey, String targetKey, Long userId);

    String getSymlink(String bucketName, String symlinkKey);

    void deleteSymlink(String bucketName, String symlinkKey, Long userId);

    // ==================== 分片清理 ====================

    Integer cleanupMultipartUploads(String bucketName, Integer daysBefore);

    List<MultipartUploadInfoDTO> listMultipartUploads(String bucketName, String prefix, Integer maxUploads);

    // ==================== 元数据操作 ====================

    void updateObjectMetadata(String bucketName, String objectKey, Map<String, String> metadata);

    Map<String, String> getObjectMetadataMap(String bucketName, String objectKey);
}
