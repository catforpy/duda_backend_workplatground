package com.duda.file.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.file.provider.entity.BucketStorageLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Bucket存储统计日志Mapper
 *
 * @author duda
 * @date 2025-03-14
 */
@Mapper
public interface BucketStorageLogMapper extends BaseMapper<BucketStorageLog> {

    /**
     * 根据Bucket名称和日期范围查询
     */
    List<BucketStorageLog> selectByBucketNameAndDateRange(
        @Param("bucketName") String bucketName,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 根据用户ID和日期范围查询
     */
    List<BucketStorageLog> selectByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 查询最新的存储统计
     */
    BucketStorageLog selectLatest(@Param("bucketName") String bucketName);

    /**
     * 批量插入存储日志
     */
    int batchInsert(@Param("list") List<BucketStorageLog> list);
}
