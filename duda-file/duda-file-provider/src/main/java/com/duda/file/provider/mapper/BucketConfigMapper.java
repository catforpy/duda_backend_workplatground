package com.duda.file.provider.mapper;

import com.duda.file.provider.entity.BucketConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Bucket配置Mapper接口
 *
 * @author duda
 * @date 2025-03-13
 */
@Mapper
public interface BucketConfigMapper {

    /**
     * 根据ID查询
     */
    BucketConfig selectById(@Param("id") Long id);

    /**
     * 根据Bucket名称查询
     */
    BucketConfig selectByBucketName(@Param("bucketName") String bucketName);

    /**
     * 根据用户ID查询Bucket列表
     */
    List<BucketConfig> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据API密钥ID查询Bucket列表
     */
    List<BucketConfig> selectByApiKeyId(@Param("apiKeyId") Long apiKeyId);

    /**
     * 根据用户ID和分片编号查询Bucket列表
     */
    List<BucketConfig> selectByUserIdAndShard(@Param("userId") Long userId, @Param("userShard") Integer userShard);

    /**
     * 根据状态查询Bucket列表
     */
    List<BucketConfig> selectByStatus(@Param("status") String status);

    /**
     * 插入Bucket配置
     */
    int insert(BucketConfig bucketConfig);

    /**
     * 更新Bucket配置
     */
    int update(BucketConfig bucketConfig);

    /**
     * 更新Bucket使用统计
     */
    int updateUsage(@Param("bucketName") String bucketName, @Param("size") Long size, @Param("count") Integer count);

    /**
     * 更新Bucket状态
     */
    int updateStatus(@Param("bucketName") String bucketName, @Param("status") String status);

    /**
     * 删除Bucket(软删除)
     */
    int deleteByBucketName(@Param("bucketName") String bucketName);

    /**
     * 查询所有Bucket
     */
    List<BucketConfig> selectAll();

    /**
     * 查询所有激活的Bucket
     */
    List<BucketConfig> selectActiveBuckets();
}
