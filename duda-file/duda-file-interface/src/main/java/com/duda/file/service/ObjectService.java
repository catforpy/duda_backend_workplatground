package com.duda.file.service;

import com.duda.file.dto.object.*;
import com.duda.file.dto.upload.UploadResultDTO;
import com.duda.file.dto.upload.MultipartUploadInfoDTO;
import com.duda.file.common.exception.StorageException;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Object服务接口
 * 负责对象(Object)的管理业务
 *
 * @author duda
 * @date 2025-03-13
 */
public interface ObjectService {

    // ==================== 基础操作 ====================

    /**
     * 获取对象信息
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @return 对象信息
     */
    ObjectDTO getObjectInfo(String bucketName, String objectKey);

    /**
     * 获取对象元数据
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @return 对象元数据
     */
    ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey);

    /**
     * 设置对象元数据
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param metadata 元数据
     */
    void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata);

    /**
     * 判断对象是否存在
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @return 是否存在
     */
    Boolean doesObjectExist(String bucketName, String objectKey);

    // ==================== 删除操作 ====================

    /**
     * 删除单个对象
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param userId 用户ID
     * @throws StorageException 存储异常
     */
    void deleteObject(String bucketName, String objectKey, Long userId) throws StorageException;

    /**
     * 批量删除对象
     *
     * @param bucketName 存储空间名称
     * @param objectKeys 对象键列表
     * @param userId 用户ID
     * @return 删除结果(成功数量、失败数量)
     * @throws StorageException 存储异常
     */
    BatchDeleteResultDTO deleteObjects(String bucketName, List<String> objectKeys, Long userId) throws StorageException;

    // ==================== 复制和重命名 ====================

    /**
     * 复制对象
     *
     * @param sourceBucketName 源存储空间
     * @param sourceObjectKey 源对象键
     * @param destinationBucketName 目标存储空间
     * @param destinationObjectKey 目标对象键
     * @param userId 用户ID
     * @throws StorageException 存储异常
     */
    void copyObject(String sourceBucketName, String sourceObjectKey,
                    String destinationBucketName, String destinationObjectKey,
                    Long userId) throws StorageException;

    /**
     * 重命名对象
     *
     * @param bucketName 存储空间名称
     * @param sourceObjectKey 源对象键
     * @param destinationObjectKey 目标对象键
     * @param userId 用户ID
     * @throws StorageException 存储异常
     */
    void renameObject(String bucketName, String sourceObjectKey,
                      String destinationObjectKey, Long userId) throws StorageException;

    // ==================== 列表操作 ====================

    /**
     * 列出对象
     *
     * @param request 列表请求
     * @return 对象列表结果
     */
    ListObjectsResultDTO listObjects(ListObjectsReqDTO request);

    /**
     * 递归列出指定前缀下的所有对象
     *
     * @param bucketName 存储空间名称
     * @param prefix 前缀
     * @param maxKeys 最大数量
     * @return 对象列表
     */
    List<ObjectDTO> listObjectsRecursive(String bucketName, String prefix, Integer maxKeys);

    // ==================== 权限管理 ====================

    /**
     * 设置对象ACL
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param aclType ACL类型
     */
    void setObjectAcl(String bucketName, String objectKey, com.duda.file.enums.AclType aclType);

    /**
     * 获取对象ACL
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @return ACL类型
     */
    com.duda.file.enums.AclType getObjectAcl(String bucketName, String objectKey);

    // ==================== 标签管理 ====================

    /**
     * 设置对象标签
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param tags 标签Map
     */
    void setObjectTags(String bucketName, String objectKey, Map<String, String> tags);

    /**
     * 获取对象标签
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @return 标签Map
     */
    Map<String, String> getObjectTags(String bucketName, String objectKey);

    /**
     * 删除对象标签
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param tagKeys 要删除的标签键列表
     */
    void deleteObjectTags(String bucketName, String objectKey, List<String> tagKeys);

    // ==================== 恢复操作 ====================

    /**
     * 恢复归档存储对象
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param restoreRequest 恢复请求
     * @throws StorageException 存储异常
     */
    void restoreObject(String bucketName, String objectKey, RestoreObjectReqDTO restoreRequest) throws StorageException;

    /**
     * 获取对象恢复状态
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @return 恢复状态
     */
    RestoreStatusDTO getRestoreStatus(String bucketName, String objectKey);

    // ==================== 软链接操作 ====================

    /**
     * 创建软链接
     *
     * @param bucketName 存储空间名称
     * @param symlinkKey 软链接键
     * @param targetKey 目标对象键
     * @param userId 用户ID
     * @throws StorageException 存储异常
     */
    void createSymlink(String bucketName, String symlinkKey, String targetKey, Long userId) throws StorageException;

    /**
     * 获取软链接目标
     *
     * @param bucketName 存储空间名称
     * @param symlinkKey 软链接键
     * @return 目标对象键
     */
    String getSymlink(String bucketName, String symlinkKey);

    /**
     * 删除软链接
     *
     * @param bucketName 存储空间名称
     * @param symlinkKey 软链接键
     * @param userId 用户ID
     * @throws StorageException 存储异常
     */
    void deleteSymlink(String bucketName, String symlinkKey, Long userId) throws StorageException;

    // ==================== 分片清理 ====================

    /**
     * 清理未完成的分片上传
     *
     * @param bucketName 存储空间名称
     * @param daysBefore 多天前的分片
     * @return 清理的分片数量
     */
    Integer cleanupMultipartUploads(String bucketName, Integer daysBefore);

    /**
     * 列出未完成的分片上传
     *
     * @param bucketName 存储空间名称
     * @param prefix 前缀
     * @param maxUploads 最大上传数量
     * @return 分片上传列表
     */
    List<MultipartUploadInfoDTO> listMultipartUploads(String bucketName, String prefix, Integer maxUploads);

    // ==================== 工具方法 ====================

    /**
     * 检查用户是否有权操作该对象
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @param userId 用户ID
     * @return 是否有权限
     */
    Boolean checkPermission(String bucketName, String objectKey, Long userId);

    /**
     * 计算对象的完整路径(包含Bucket)
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @return 完整路径
     */
    String getFullPath(String bucketName, String objectKey);
}
