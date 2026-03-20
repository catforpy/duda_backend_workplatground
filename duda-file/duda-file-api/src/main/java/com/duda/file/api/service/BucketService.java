package com.duda.file.api.service;

import com.duda.file.dto.bucket.*;

import java.util.List;
import java.util.Map;

/**
 * Bucket 本地服务接口
 * API 层内部使用的业务服务
 *
 * @author DudaNexus
 * @since 2026-03-18
 */
public interface BucketService {

    /**
     * 创建存储空间
     */
    BucketDTO createBucket(CreateBucketReqDTO request);

    /**
     * 删除存储空间
     */
    void deleteBucket(String bucketName, Long userId);

    /**
     * 获取存储空间信息
     */
    BucketDTO getBucketInfo(String bucketName);

    /**
     * 判断存储空间是否存在
     */
    Boolean doesBucketExist(String bucketName);

    /**
     * 列出用户的存储空间
     */
    List<BucketDTO> listBuckets(Long userId, String keyName);

    /**
     * 列出OSS账号下所有真实的存储空间
     */
    List<BucketDTO> listAllOssBuckets();

    /**
     * 设置存储空间ACL
     */
    void setBucketAcl(String bucketName, com.duda.file.enums.AclType aclType);

    /**
     * 获取存储空间ACL
     */
    com.duda.file.enums.AclType getBucketAcl(String bucketName);

    /**
     * 设置存储空间标签
     */
    void setBucketTags(String bucketName, Map<String, String> tags);

    /**
     * 获取存储空间标签
     */
    Map<String, String> getBucketTags(String bucketName);

    /**
     * 获取存储空间所在区域
     */
    String getBucketLocation(String bucketName);

    /**
     * 更新存储空间配额
     */
    void updateBucketQuota(String bucketName, Long maxSize, Integer maxCount);

    /**
     * 获取存储空间统计信息
     */
    BucketStatisticsDTO getBucketStatistics(String bucketName);

    /**
     * 获取存储空间容量信息
     */
    Map<String, Object> getBucketCapacity(String bucketName);
}
