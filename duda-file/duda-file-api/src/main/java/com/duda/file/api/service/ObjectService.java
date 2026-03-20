package com.duda.file.api.service;

import com.duda.file.dto.object.*;
import com.duda.file.dto.upload.MultipartUploadInfoDTO;

import java.util.List;
import java.util.Map;

/**
 * Object 本地服务接口
 * API层的服务接口,通过 Dubbo RPC 调用 Provider 服务
 *
 * @author DudaNexus
 * @since 2026-03-19
 */
public interface ObjectService {

    // ==================== 基础操作 ====================

    /**
     * 创建目录
     */
    void createDirectory(String bucketName, String directoryPath, Long userId);

    /**
     * 获取对象信息
     */
    ObjectDTO getObjectInfo(String bucketName, String objectKey);

    /**
     * 获取对象元数据
     */
    ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey);

    /**
     * 设置对象元数据
     */
    void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata);

    /**
     * 检查对象是否存在
     */
    Boolean doesObjectExist(String bucketName, String objectKey);

    // ==================== 删除操作 ====================

    /**
     * 删除对象
     */
    void deleteObject(String bucketName, String objectKey, Long userId);

    /**
     * 批量删除对象
     */
    BatchDeleteResultDTO deleteObjects(String bucketName, List<String> objectKeys, Long userId);

    // ==================== 复制和重命名 ====================

    /**
     * 复制对象
     */
    void copyObject(String sourceBucketName, String sourceObjectKey,
                    String destinationBucketName, String destinationObjectKey,
                    Long userId);

    /**
     * 重命名对象
     */
    void renameObject(String bucketName, String sourceObjectKey,
                      String destinationObjectKey, Long userId);

    // ==================== 列表操作 ====================

    /**
     * 列出对象
     */
    ListObjectsResultDTO listObjects(ListObjectsReqDTO request);

    /**
     * 递归列出对象
     */
    List<ObjectDTO> listObjectsRecursive(String bucketName, String prefix, Integer maxKeys);

    // ==================== 权限管理 ====================

    /**
     * 设置对象ACL
     */
    void setObjectAcl(String bucketName, String objectKey, com.duda.file.enums.AclType aclType);

    /**
     * 获取对象ACL
     */
    com.duda.file.enums.AclType getObjectAcl(String bucketName, String objectKey);

    // ==================== 标签管理 ====================

    /**
     * 设置对象标签
     */
    void setObjectTags(String bucketName, String objectKey, Map<String, String> tags);

    /**
     * 获取对象标签
     */
    Map<String, String> getObjectTags(String bucketName, String objectKey);

    /**
     * 删除对象标签
     */
    void deleteObjectTags(String bucketName, String objectKey, List<String> tagKeys);

    // ==================== 恢复操作 ====================

    /**
     * 恢复对象
     */
    void restoreObject(String bucketName, String objectKey, RestoreObjectReqDTO restoreRequest);

    /**
     * 获取恢复状态
     */
    RestoreStatusDTO getRestoreStatus(String bucketName, String objectKey);

    // ==================== 软链接操作 ====================

    /**
     * 创建软链接
     */
    void createSymlink(String bucketName, String symlinkKey, String targetKey, Long userId);

    /**
     * 获取软链接
     */
    String getSymlink(String bucketName, String symlinkKey);

    /**
     * 删除软链接
     */
    void deleteSymlink(String bucketName, String symlinkKey, Long userId);

    // ==================== 分片清理 ====================

    /**
     * 清理分片上传
     */
    Integer cleanupMultipartUploads(String bucketName, Integer daysBefore);

    /**
     * 列出分片上传
     */
    List<MultipartUploadInfoDTO> listMultipartUploads(String bucketName, String prefix, Integer maxUploads);

    // ==================== 工具方法 ====================

    /**
     * 检查权限
     */
    Boolean checkPermission(String bucketName, String objectKey, Long userId);

    /**
     * 获取完整路径
     */
    String getFullPath(String bucketName, String objectKey);

    // ==================== 元数据操作 ====================

    /**
     * 更新对象元数据
     */
    void updateObjectMetadata(String bucketName, String objectKey, Map<String, String> metadata);

    /**
     * 获取对象元数据Map
     */
    Map<String, String> getObjectMetadataMap(String bucketName, String objectKey);
}
