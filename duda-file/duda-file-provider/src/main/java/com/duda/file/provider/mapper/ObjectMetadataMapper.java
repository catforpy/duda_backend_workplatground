package com.duda.file.provider.mapper;

import com.duda.file.provider.entity.ObjectMetadata;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对象元数据Mapper接口
 *
 * @author duda
 * @date 2025-03-13
 */
@Mapper
public interface ObjectMetadataMapper {

    /**
     * 根据ID查询
     */
    ObjectMetadata selectById(@Param("id") Long id);

    /**
     * 根据Bucket名称和对象键查询
     */
    ObjectMetadata selectByBucketAndKey(@Param("bucketName") String bucketName, @Param("objectKey") String objectKey);

    /**
     * 根据Bucket名称和对象键及版本ID查询
     */
    ObjectMetadata selectByBucketAndKeyAndVersion(@Param("bucketName") String bucketName,
                                                   @Param("objectKey") String objectKey,
                                                   @Param("versionId") String versionId);

    /**
     * 根据Bucket名称和前缀查询对象列表
     */
    List<ObjectMetadata> selectByBucketAndPrefix(@Param("bucketName") String bucketName,
                                                  @Param("prefix") String prefix,
                                                  @Param("limit") Integer limit);

    /**
     * 根据Bucket名称查询所有对象
     */
    List<ObjectMetadata> selectByBucketName(@Param("bucketName") String bucketName);

    /**
     * 根据创建者查询对象列表
     */
    List<ObjectMetadata> selectByCreatedBy(@Param("createdBy") Long createdBy);

    /**
     * 根据状态查询对象列表
     */
    List<ObjectMetadata> selectByStatus(@Param("status") String status);

    /**
     * 插入对象元数据
     */
    int insert(ObjectMetadata objectMetadata);

    /**
     * 批量插入对象元数据
     */
    int batchInsert(@Param("list") List<ObjectMetadata> list);

    /**
     * 更新对象元数据
     */
    int update(ObjectMetadata objectMetadata);

    /**
     * 更新访问统计
     */
    int updateAccessStats(@Param("bucketName") String bucketName, @Param("objectKey") String objectKey);

    /**
     * 更新下载统计
     */
    int updateDownloadStats(@Param("bucketName") String bucketName, @Param("objectKey") String objectKey);

    /**
     * 删除对象元数据(软删除)
     */
    int deleteByBucketAndKey(@Param("bucketName") String bucketName, @Param("objectKey") String objectKey);

    /**
     * 批量删除对象元数据(软删除)
     */
    int batchDelete(@Param("bucketName") String bucketName, @Param("objectKeys") List<String> objectKeys);

    /**
     * 统计Bucket中的对象数量
     */
    Long countByBucket(@Param("bucketName") String bucketName);

    /**
     * 统计Bucket中对象的存储大小
     */
    Long sumSizeByBucket(@Param("bucketName") String bucketName);
}
