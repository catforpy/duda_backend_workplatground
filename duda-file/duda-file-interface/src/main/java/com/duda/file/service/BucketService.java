package com.duda.file.service;

import com.duda.file.dto.bucket.*;
import com.duda.file.common.exception.StorageException;

import java.util.List;
import java.util.Map;

/**
 * Bucket服务接口
 * 负责存储空间(Bucket)的管理业务
 *
 * @author duda
 * @date 2025-03-13
 */
public interface BucketService {

    // ==================== 基础操作 ====================

    /**
     * 创建存储空间
     *
     * @param request 创建请求
     * @return Bucket信息
     * @throws StorageException 存储异常
     */
    BucketDTO createBucket(CreateBucketReqDTO request) throws StorageException;

    /**
     * 删除存储空间
     *
     * @param bucketName 存储空间名称
     * @param userId 用户ID
     * @throws StorageException 存储异常
     */
    void deleteBucket(String bucketName, Long userId) throws StorageException;

    /**
     * 获取存储空间信息
     *
     * @param bucketName 存储空间名称
     * @return Bucket信息
     */
    BucketDTO getBucketInfo(String bucketName);

    /**
     * 判断存储空间是否存在
     *
     * @param bucketName 存储空间名称
     * @return 是否存在
     */
    Boolean doesBucketExist(String bucketName);

    /**
     * 列出用户的存储空间
     *
     * @param userId 用户ID
     * @return Bucket列表
     */
    List<BucketDTO> listBuckets(Long userId);

    // ==================== 权限管理 ====================

    /**
     * 设置存储空间ACL
     *
     * @param bucketName 存储空间名称
     * @param aclType ACL类型
     */
    void setBucketAcl(String bucketName, com.duda.file.enums.AclType aclType);

    /**
     * 获取存储空间ACL
     *
     * @param bucketName 存储空间名称
     * @return ACL类型
     */
    com.duda.file.enums.AclType getBucketAcl(String bucketName);

    // ==================== 位置和区域 ====================

    /**
     * 获取存储空间所在区域
     *
     * @param bucketName 存储空间名称
     * @return 区域
     */
    String getBucketLocation(String bucketName);

    // ==================== 标签管理 ====================

    /**
     * 设置存储空间标签
     *
     * @param bucketName 存储空间名称
     * @param tags 标签Map
     */
    void setBucketTags(String bucketName, Map<String, String> tags);

    /**
     * 获取存储空间标签
     *
     * @param bucketName 存储空间名称
     * @return 标签Map
     */
    Map<String, String> getBucketTags(String bucketName);

    /**
     * 删除存储空间标签
     *
     * @param bucketName 存储空间名称
     * @param tagKeys 要删除的标签键列表
     */
    void deleteBucketTags(String bucketName, List<String> tagKeys);

    // ==================== 统计信息 ====================

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

    // ==================== 配置管理 ====================

    /**
     * 设置存储空间策略(如生命周期、跨域配置等)
     *
     * @param bucketName 存储空间名称
     * @param policyType 策略类型
     * @param policyConfig 策略配置
     */
    void setBucketPolicy(String bucketName, String policyType, String policyConfig);

    /**
     * 获取存储空间策略
     *
     * @param bucketName 存储空间名称
     * @param policyType 策略类型
     * @return 策略配置
     */
    String getBucketPolicy(String bucketName, String policyType);

    /**
     * 删除存储空间策略
     *
     * @param bucketName 存储空间名称
     * @param policyType 策略类型
     */
    void deleteBucketPolicy(String bucketName, String policyType);

    // ==================== 工具方法 ====================

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
