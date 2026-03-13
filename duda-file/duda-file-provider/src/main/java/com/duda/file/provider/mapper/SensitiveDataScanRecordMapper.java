package com.duda.file.provider.mapper;

import com.duda.file.provider.entity.SensitiveDataScanRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 敏感数据扫描记录Mapper接口
 *
 * @author duda
 * @date 2025-03-13
 */
@Mapper
public interface SensitiveDataScanRecordMapper {

    /**
     * 根据ID查询
     */
    SensitiveDataScanRecord selectById(@Param("id") Long id);

    /**
     * 根据任务ID查询
     */
    SensitiveDataScanRecord selectByTaskId(@Param("taskId") String taskId);

    /**
     * 根据Bucket名称查询扫描记录
     */
    List<SensitiveDataScanRecord> selectByBucketName(@Param("bucketName") String bucketName);

    /**
     * 根据状态查询扫描记录
     */
    List<SensitiveDataScanRecord> selectByStatus(@Param("status") String status);

    /**
     * 查询发现敏感数据的记录
     */
    List<SensitiveDataScanRecord> selectSensitiveDataFound(@Param("minCount") Long minCount);

    /**
     * 插入扫描记录
     */
    int insert(SensitiveDataScanRecord sensitiveDataScanRecord);

    /**
     * 更新扫描记录
     */
    int update(SensitiveDataScanRecord sensitiveDataScanRecord);

    /**
     * 更新扫描状态
     */
    int updateStatus(@Param("taskId") String taskId, @Param("status") String status);

    /**
     * 更新扫描进度
     */
    int updateProgress(@Param("taskId") String taskId,
                       @Param("scannedFiles") Long scannedFiles,
                       @Param("sensitiveFilesFound") Long sensitiveFilesFound);

    /**
     * 删除扫描记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除过期的记录
     */
    int batchDeleteExpired(@Param("expireDays") Integer expireDays);
}
