package com.duda.file.service;

import com.duda.file.dto.*;

import java.util.List;

/**
 * Bucket管理服务接口
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
public interface BucketManagementService {

    /**
     * 创建存储空间
     *
     * @param request 创建请求
     * @return Bucket信息
     */
    BucketDTO createBucket(CreateBucketReqDTO request);

    /**
     * 删除存储空间
     *
     * @param bucketName Bucket名称
     * @param userId 用户ID
     */
    void deleteBucket(String bucketName, Long userId);

    /**
     * 列举存储空间
     *
     * @param userId 用户ID
     * @return Bucket列表
     */
    List<BucketDTO> listBuckets(Long userId);

    /**
     * 获取Bucket信息
     *
     * @param bucketName Bucket名称
     * @return Bucket信息
     */
    BucketDTO getBucketInfo(String bucketName);

    /**
     * 检查Bucket是否存在
     *
     * @param bucketName Bucket名称
     * @return 是否存在
     */
    Boolean doesBucketExist(String bucketName);

    /**
     * 设置Bucket权限
     *
     * @param bucketName Bucket名称
     * @param aclType 权限类型
     */
    void setBucketAcl(String bucketName, AclType aclType);

    /**
     * 获取Bucket权限
     *
     * @param bucketName Bucket名称
     * @return 权限类型
     */
    AclType getBucketAcl(String bucketName);

    /**
     * 获取Bucket地域信息
     *
     * @param bucketName Bucket名称
     * @return 地域信息
     */
    String getBucketLocation(String bucketName);

    /**
     * 设置Bucket标签
     *
     * @param bucketName Bucket名称
     * @param tags 标签
     */
    void setBucketTags(String bucketName, java.util.Map<String, String> tags);

    /**
     * 获取Bucket标签
     *
     * @param bucketName Bucket名称
     * @return 标签
     */
    java.util.Map<String, String> getBucketTags(String bucketName);

    /**
     * 获取Bucket用量统计
     *
     * @param bucketName Bucket名称
     * @return 用量统计
     */
    BucketStatisticsDTO getBucketStatistics(String bucketName);

    /**
     * 更新Bucket配额
     *
     * @param bucketName Bucket名称
     * @param maxSize 最大文件大小（字节）
     * @param maxCount 最大文件数量
     */
    void updateBucketQuota(String bucketName, Long maxSize, Integer maxCount);

    /**
     * 生成Bucket名称（自动生成唯一名称）
     *
     * @param userId 用户ID
     * @param userType 用户类型
     * @param category 分类
     * @return Bucket名称
     */
    String generateBucketName(Long userId, String userType, String category);

    /**
     * 验证Bucket名称是否合法
     *
     * @param bucketName Bucket名称
     * @return 是否合法
     */
    Boolean validateBucketName(String bucketName);
}
