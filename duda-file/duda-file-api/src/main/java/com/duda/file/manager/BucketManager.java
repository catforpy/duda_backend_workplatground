package com.duda.file.manager;

import com.duda.file.adapter.StorageService;
import com.duda.file.dto.bucket.*;
import com.duda.file.common.exception.StorageException;

import java.util.List;
import java.util.Map;

/**
 * Bucket管理器
 * 负责Bucket相关的业务逻辑处理
 * <p>
 * 职责：
 * 1. 封装Bucket操作的业务逻辑
 * 2. 协调StorageService适配器
 * 3. 处理权限检查、配额管理、数据验证等
 * 4. 维护Bucket元数据
 *
 * @author duda
 * @date 2025-03-13
 */
public interface BucketManager {

    /**
     * 创建存储空间
     *
     * @param request 创建请求
     * @param storageAdapter 存储适配器
     * @return Bucket信息
     * @throws StorageException 存储异常
     */
    BucketDTO createBucket(CreateBucketReqDTO request, StorageService storageAdapter) throws StorageException;

    /**
     * 删除存储空间
     *
     * @param bucketName 存储空间名称
     * @param userId 用户ID
     * @param storageAdapter 存储适配器
     * @throws StorageException 存储异常
     */
    void deleteBucket(String bucketName, Long userId, StorageService storageAdapter) throws StorageException;

    /**
     * 获取存储空间信息
     *
     * @param bucketName 存储空间名称
     * @param storageAdapter 存储适配器
     * @return Bucket信息
     */
    BucketDTO getBucketInfo(String bucketName, StorageService storageAdapter);

    /**
     * 判断存储空间是否存在
     *
     * @param bucketName 存储空间名称
     * @param storageAdapter 存储适配器
     * @return 是否存在
     */
    Boolean doesBucketExist(String bucketName, StorageService storageAdapter);

    /**
     * 列出用户的存储空间
     *
     * @param userId 用户ID
     * @param storageAdapter 存储适配器
     * @return Bucket列表
     */
    List<BucketDTO> listBuckets(Long userId, StorageService storageAdapter);

    /**
     * 设置存储空间ACL
     *
     * @param bucketName 存储空间名称
     * @param aclType ACL类型
     * @param storageAdapter 存储适配器
     */
    void setBucketAcl(String bucketName, com.duda.file.enums.AclType aclType, StorageService storageAdapter);

    /**
     * 获取存储空间ACL
     *
     * @param bucketName 存储空间名称
     * @param storageAdapter 存储适配器
     * @return ACL类型
     */
    com.duda.file.enums.AclType getBucketAcl(String bucketName, StorageService storageAdapter);

    /**
     * 获取存储空间所在区域
     *
     * @param bucketName 存储空间名称
     * @param storageAdapter 存储适配器
     * @return 区域
     */
    String getBucketLocation(String bucketName, StorageService storageAdapter);

    /**
     * 设置存储空间标签
     *
     * @param bucketName 存储空间名称
     * @param tags 标签Map
     * @param storageAdapter 存储适配器
     */
    void setBucketTags(String bucketName, Map<String, String> tags, StorageService storageAdapter);

    /**
     * 获取存储空间标签
     *
     * @param bucketName 存储空间名称
     * @param storageAdapter 存储适配器
     * @return 标签Map
     */
    Map<String, String> getBucketTags(String bucketName, StorageService storageAdapter);

    /**
     * 获取存储空间统计信息
     *
     * @param bucketName 存储空间名称
     * @return 统计信息
     */
    BucketStatisticsDTO getBucketStatistics(String bucketName);

    /**
     * 更新存储空间配额
     *
     * @param bucketName 存储空间名称
     * @param maxSize 最大存储容量(字节)
     * @param maxCount 最大文件数量
     */
    void updateBucketQuota(String bucketName, Long maxSize, Integer maxCount);

    /**
     * 生成存储空间名称
     *
     * @param userId 用户ID
     * @param userType 用户类型
     * @param category 分类
     * @return Bucket名称
     */
    String generateBucketName(Long userId, String userType, String category);

    /**
     * 验证存储空间名称是否合法
     *
     * @param bucketName Bucket名称
     * @return 是否合法
     */
    Boolean validateBucketName(String bucketName);

    /**
     * 检查用户是否有权操作该Bucket
     *
     * @param bucketName Bucket名称
     * @param userId 用户ID
     * @return 是否有权限
     */
    Boolean checkPermission(String bucketName, Long userId);
}
