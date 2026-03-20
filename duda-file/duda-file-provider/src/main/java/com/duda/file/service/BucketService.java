package com.duda.file.service;

import com.duda.file.dto.bucket.*;

import java.util.List;

/**
 * Bucket Service 接口
 * 内部业务逻辑接口
 *
 * @author DudaNexus
 * @since 2026-03-17
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
    boolean bucketExists(String bucketName);

    /**
     * 获取用户在指定API密钥下的所有存储空间
     *
     * @param userId 用户ID
     * @param keyName API密钥名称
     * @return Bucket列表
     */
    List<BucketDTO> listBuckets(Long userId, String keyName);

    // TODO: 需要创建 UpdateBucketConfigReqDTO 和 BucketConfigDTO
    // /**
    //  * 更新存储空间配置
    //  */
    // void updateBucketConfig(String bucketName, UpdateBucketConfigReqDTO request);
    //
    // /**
    //  * 获取存储空间配置
    //  */
    // BucketConfigDTO getBucketConfig(String bucketName);

    /**
     * 设置默认存储空间
     */
    void setDefaultBucket(String bucketName, Long userId);

    /**
     * 获取用户的默认存储空间
     */
    BucketDTO getDefaultBucket(Long userId);

    /**
     * 获取存储空间统计信息
     */
    BucketStatisticsDTO getBucketStatistics(String bucketName);
}
