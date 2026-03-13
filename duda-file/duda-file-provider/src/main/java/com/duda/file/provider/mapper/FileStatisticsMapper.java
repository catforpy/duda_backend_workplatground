package com.duda.file.provider.mapper;

import com.duda.file.provider.entity.FileStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 文件统计Mapper接口
 *
 * @author duda
 * @date 2025-03-13
 */
@Mapper
public interface FileStatisticsMapper {

    /**
     * 根据ID查询
     */
    FileStatistics selectById(@Param("id") Long id);

    /**
     * 根据Bucket名称、日期和类型查询
     */
    FileStatistics selectByBucketAndDateAndType(@Param("bucketName") String bucketName,
                                                 @Param("statDate") LocalDate statDate,
                                                 @Param("statType") String statType);

    /**
     * 根据Bucket名称查询统计列表
     */
    List<FileStatistics> selectByBucket(@Param("bucketName") String bucketName);

    /**
     * 根据日期范围查询统计列表
     */
    List<FileStatistics> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据统计类型查询统计列表
     */
    List<FileStatistics> selectByType(@Param("statType") String statType);

    /**
     * 插入统计记录
     */
    int insert(FileStatistics fileStatistics);

    /**
     * 更新统计记录
     */
    int update(FileStatistics fileStatistics);

    /**
     * 批量插入统计记录
     */
    int batchInsert(@Param("list") List<FileStatistics> list);

    /**
     * 删除统计记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询所有统计
     */
    List<FileStatistics> selectAll();
}
