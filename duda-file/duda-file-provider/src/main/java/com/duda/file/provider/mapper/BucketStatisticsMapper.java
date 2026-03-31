package com.duda.file.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.file.provider.entity.BucketStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Bucket统计 Mapper 接口
 * 对应bucket_statistics表
 *
 * @author duda
 * @date 2025-03-27
 */
@Mapper
public interface BucketStatisticsMapper extends BaseMapper<BucketStatistics> {

    /**
     * 根据Bucket名称查询统计信息
     */
    BucketStatistics selectByBucketName(@Param("bucketName") String bucketName);

    /**
     * 根据租户ID查询统计列表
     */
    List<BucketStatistics> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据状态查询统计列表
     */
    List<BucketStatistics> selectByStatus(@Param("status") String status);

    /**
     * 根据地域查询统计列表
     */
    List<BucketStatistics> selectByRegion(@Param("region") String region);

    /**
     * 根据存储类型查询统计列表
     */
    List<BucketStatistics> selectByStorageType(@Param("storageType") String storageType);

    /**
     * 查询最后更新时间早于指定时间的统计
     */
    List<BucketStatistics> selectByLastSyncTimeBefore(@Param("lastSyncTime") LocalDateTime lastSyncTime);

    /**
     * 更新统计信息
     */
    int updateStatistics(BucketStatistics bucketStatistics);

    /**
     * 更新同步时间
     */
    int updateLastSyncTime(@Param("bucketName") String bucketName);

    /**
     * 更新最后上传时间
     */
    int updateLastUploadTime(@Param("bucketName") String bucketName);

    /**
     * 更新最后下载时间
     */
    int updateLastDownloadTime(@Param("bucketName") String bucketName);

    /**
     * 更新最后删除时间
     */
    int updateLastDeleteTime(@Param("bucketName") String bucketName);

    /**
     * 查询所有活跃的统计
     */
    List<BucketStatistics> selectActiveStatistics();
}
