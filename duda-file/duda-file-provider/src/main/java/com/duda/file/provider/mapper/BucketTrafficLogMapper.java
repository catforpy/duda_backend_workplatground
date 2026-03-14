package com.duda.file.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.file.provider.entity.BucketTrafficLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Bucket流量统计日志Mapper
 *
 * @author duda
 * @date 2025-03-14
 */
@Mapper
public interface BucketTrafficLogMapper extends BaseMapper<BucketTrafficLog> {

    /**
     * 根据Bucket名称和日期范围查询
     */
    List<BucketTrafficLog> selectByBucketNameAndDateRange(
        @Param("bucketName") String bucketName,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 根据用户ID和日期范围查询
     */
    List<BucketTrafficLog> selectByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 查询指定日期的总流量
     */
    Long selectTotalTrafficByDate(@Param("bucketName") String bucketName, @Param("statDate") LocalDate statDate);

    /**
     * 批量插入流量日志
     */
    int batchInsert(@Param("list") List<BucketTrafficLog> list);
}
