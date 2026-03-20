package com.duda.file.rpc;

import com.duda.file.dto.bucket.*;

import java.util.List;

/**
 * Bucket RPC 接口
 * 对外提供 Dubbo RPC 服务
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
public interface IBucketRpc {

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

    /**
     * 判断存储空间是否存在
     */
    Boolean doesBucketExist(String bucketName);

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
    void setBucketTags(String bucketName, java.util.Map<String, String> tags);

    /**
     * 获取存储空间标签
     */
    java.util.Map<String, String> getBucketTags(String bucketName);

    /**
     * 获取存储空间所在区域
     */
    String getBucketLocation(String bucketName);

    /**
     * 更新存储空间配额
     */
    void updateBucketQuota(String bucketName, Long maxSize, Integer maxCount);

    /**
     * 列出OSS账号下所有真实的存储空间
     */
    java.util.List<BucketDTO> listAllOssBuckets();
}
