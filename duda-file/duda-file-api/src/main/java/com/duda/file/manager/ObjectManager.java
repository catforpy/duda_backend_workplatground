package com.duda.file.manager;

import com.duda.file.adapter.StorageService;
import com.duda.file.dto.object.*;
import com.duda.file.common.exception.StorageException;

import java.util.List;
import java.util.Map;

/**
 * Object管理器
 * 负责Object相关的业务逻辑处理
 * <p>
 * 职责：
 * 1. 封装Object操作的业务逻辑
 * 2. 协调StorageService适配器
 * 3. 处理权限检查、目录操作、元数据管理等
 * 4. 维护Object元数据
 *
 * @author duda
 * @date 2025-03-13
 */
public interface ObjectManager {

    /**
     * 获取对象信息
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param storageAdapter 存储适配器
     * @return 对象信息
     */
    ObjectDTO getObjectInfo(String bucketName, String objectKey, StorageService storageAdapter);

    /**
     * 获取对象元数据
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param storageAdapter 存储适配器
     * @return 对象元数据
     */
    ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey, StorageService storageAdapter);

    /**
     * 设置对象元数据
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param metadata 元数据
     * @param storageAdapter 存储适配器
     */
    void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata, StorageService storageAdapter);

    /**
     * 判断对象是否存在
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param storageAdapter 存储适配器
     * @return 是否存在
     */
    Boolean doesObjectExist(String bucketName, String objectKey, StorageService storageAdapter);

    /**
     * 删除单个对象
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param userId 用户ID
     * @param storageAdapter 存储适配器
     * @throws StorageException 存储异常
     */
    void deleteObject(String bucketName, String objectKey, Long userId, StorageService storageAdapter) throws StorageException;

    /**
     * 批量删除对象
     *
     * @param bucketName 存储空间名称
     * @param objectKeys 对象键列表
     * @param userId 用户ID
     * @param storageAdapter 存储适配器
     * @return 删除结果(成功数量、失败数量)
     * @throws StorageException 存储异常
     */
    BatchDeleteResultDTO deleteObjects(String bucketName, List<String> objectKeys, Long userId, StorageService storageAdapter) throws StorageException;

    /**
     * 复制对象
     *
     * @param sourceBucketName 源存储空间
     * @param sourceObjectKey 源对象键
     * @param destinationBucketName 目标存储空间
     * @param destinationObjectKey 目标对象键
     * @param userId 用户ID
     * @param storageAdapter 存储适配器
     * @throws StorageException 存储异常
     */
    void copyObject(String sourceBucketName, String sourceObjectKey,
                    String destinationBucketName, String destinationObjectKey,
                    Long userId, StorageService storageAdapter) throws StorageException;

    /**
     * 重命名对象
     *
     * @param bucketName 存储空间名称
     * @param sourceObjectKey 源对象键
     * @param destinationObjectKey 目标对象键
     * @param userId 用户ID
     * @param storageAdapter 存储适配器
     * @throws StorageException 存储异常
     */
    void renameObject(String bucketName, String sourceObjectKey,
                      String destinationObjectKey,
                      Long userId, StorageService storageAdapter) throws StorageException;

    /**
     * 列出对象
     *
     * @param request 列表请求
     * @param storageAdapter 存储适配器
     * @return 对象列表结果
     */
    ListObjectsResultDTO listObjects(ListObjectsReqDTO request, StorageService storageAdapter);

    /**
     * 递归列出指定前缀下的所有对象
     *
     * @param bucketName 存储空间名称
     * @param prefix 前缀
     * @param maxKeys 最大数量
     * @param storageAdapter 存储适配器
     * @return 对象列表
     */
    List<ObjectDTO> listObjectsRecursive(String bucketName, String prefix, Integer maxKeys, StorageService storageAdapter);

    /**
     * 设置对象ACL
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param aclType ACL类型
     * @param storageAdapter 存储适配器
     */
    void setObjectAcl(String bucketName, String objectKey, com.duda.file.enums.AclType aclType, StorageService storageAdapter);

    /**
     * 获取对象ACL
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param storageAdapter 存储适配器
     * @return ACL类型
     */
    com.duda.file.enums.AclType getObjectAcl(String bucketName, String objectKey, StorageService storageAdapter);

    /**
     * 设置对象标签
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param tags 标签Map
     * @param storageAdapter 存储适配器
     */
    void setObjectTags(String bucketName, String objectKey, Map<String, String> tags, StorageService storageAdapter);

    /**
     * 获取对象标签
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param storageAdapter 存储适配器
     * @return 标签Map
     */
    Map<String, String> getObjectTags(String bucketName, String objectKey, StorageService storageAdapter);

    /**
     * 恢复归档存储对象
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param restoreRequest 恢复请求
     * @param storageAdapter 存储适配器
     * @throws StorageException 存储异常
     */
    void restoreObject(String bucketName, String objectKey, RestoreObjectReqDTO restoreRequest, StorageService storageAdapter) throws StorageException;

    /**
     * 获取对象恢复状态
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param storageAdapter 存储适配器
     * @return 恢复状态
     */
    RestoreStatusDTO getRestoreStatus(String bucketName, String objectKey, StorageService storageAdapter);

    /**
     * 创建软链接
     *
     * @param bucketName 存储空间名称
     * @param symlinkKey 软链接键
     * @param targetKey 目标对象键
     * @param userId 用户ID
     * @param storageAdapter 存储适配器
     * @throws StorageException 存储异常
     */
    void createSymlink(String bucketName, String symlinkKey, String targetKey, Long userId, StorageService storageAdapter) throws StorageException;

    /**
     * 获取软链接目标
     *
     * @param bucketName 存储空间名称
     * @param symlinkKey 软链接键
     * @param storageAdapter 存储适配器
     * @return 目标对象键
     */
    String getSymlink(String bucketName, String symlinkKey, StorageService storageAdapter);

    /**
     * 删除软链接
     *
     * @param bucketName 存储空间名称
     * @param symlinkKey 软链接键
     * @param userId 用户ID
     * @param storageAdapter 存储适配器
     * @throws StorageException 存储异常
     */
    void deleteSymlink(String bucketName, String symlinkKey, Long userId, StorageService storageAdapter) throws StorageException;

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

    // ==================== 目录操作 ====================

    /**
     * 创建目录
     *
     * @param bucketName 存储空间名称
     * @param directoryPath 目录路径
     * @param userId 用户ID
     * @param storageAdapter 存储适配器
     * @throws StorageException 存储异常
     */
    void createDirectory(String bucketName, String directoryPath, Long userId, StorageService storageAdapter) throws StorageException;

    /**
     * 删除目录
     *
     * @param bucketName 存储空间名称
     * @param directoryPath 目录路径
     * @param userId 用户ID
     * @param recursive 是否递归删除
     * @param storageAdapter 存储适配器
     * @throws StorageException 存储异常
     */
    void deleteDirectory(String bucketName, String directoryPath, Long userId, Boolean recursive, StorageService storageAdapter) throws StorageException;

    /**
     * 重命名目录
     *
     * @param bucketName 存储空间名称
     * @param sourceDirectoryPath 源目录路径
     * @param destinationDirectoryPath 目标目录路径
     * @param userId 用户ID
     * @param storageAdapter 存储适配器
     * @throws StorageException 存储异常
     */
    void renameDirectory(String bucketName, String sourceDirectoryPath, String destinationDirectoryPath, Long userId, StorageService storageAdapter) throws StorageException;

    /**
     * 获取目录统计信息
     *
     * @param bucketName 存储空间名称
     * @param directoryPath 目录路径
     * @param storageAdapter 存储适配器
     * @return 目录统计信息
     */
    DirectoryStatisticsDTO getDirectoryStatistics(String bucketName, String directoryPath, StorageService storageAdapter);
}
