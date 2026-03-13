package com.duda.file.provider.mapper;

import com.duda.file.provider.entity.FileAccessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件访问日志Mapper接口
 *
 * @author duda
 * @date 2025-03-13
 */
@Mapper
public interface FileAccessLogMapper {

    /**
     * 根据ID查询
     */
    FileAccessLog selectById(@Param("id") Long id);

    /**
     * 根据Bucket和对象键查询日志
     */
    List<FileAccessLog> selectByBucketAndKey(@Param("bucketName") String bucketName, @Param("objectKey") String objectKey);

    /**
     * 根据用户ID查询日志
     */
    List<FileAccessLog> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据操作类型查询日志
     */
    List<FileAccessLog> selectByOperation(@Param("operation") String operation);

    /**
     * 根据结果状态查询日志
     */
    List<FileAccessLog> selectByResultStatus(@Param("resultStatus") String resultStatus);

    /**
     * 根据时间范围查询日志
     */
    List<FileAccessLog> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最近的日志
     */
    List<FileAccessLog> selectRecent(@Param("limit") Integer limit);

    /**
     * 插入访问日志
     */
    int insert(FileAccessLog fileAccessLog);

    /**
     * 批量插入访问日志
     */
    int batchInsert(@Param("list") List<FileAccessLog> list);

    /**
     * 删除日志
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除过期的日志
     */
    int batchDeleteExpired(@Param("expireDays") Integer expireDays);

    /**
     * 统计访问次数
     */
    Long countByOperation(@Param("operation") String operation, @Param("days") Integer days);

    /**
     * 统计流量
     */
    Long sumTrafficByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
