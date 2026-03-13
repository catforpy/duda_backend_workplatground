package com.duda.file.provider.mapper;

import com.duda.file.provider.entity.VirusScanRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 病毒扫描记录Mapper接口
 *
 * @author duda
 * @date 2025-03-13
 */
@Mapper
public interface VirusScanRecordMapper {

    /**
     * 根据ID查询
     */
    VirusScanRecord selectById(@Param("id") Long id);

    /**
     * 根据任务ID查询
     */
    VirusScanRecord selectByTaskId(@Param("taskId") String taskId);

    /**
     * 根据Bucket名称查询扫描记录
     */
    List<VirusScanRecord> selectByBucketName(@Param("bucketName") String bucketName);

    /**
     * 根据Bucket和对象键查询
     */
    List<VirusScanRecord> selectByBucketAndKey(@Param("bucketName") String bucketName, @Param("objectKey") String objectKey);

    /**
     * 根据状态查询扫描记录
     */
    List<VirusScanRecord> selectByStatus(@Param("status") String status);

    /**
     * 查询发现病毒的记录
     */
    List<VirusScanRecord> selectVirusFound(@Param("virusFound") Boolean virusFound);

    /**
     * 插入扫描记录
     */
    int insert(VirusScanRecord virusScanRecord);

    /**
     * 更新扫描记录
     */
    int update(VirusScanRecord virusScanRecord);

    /**
     * 更新扫描状态
     */
    int updateStatus(@Param("taskId") String taskId, @Param("status") String status);

    /**
     * 更新扫描结果
     */
    int updateResult(@Param("taskId") String taskId,
                     @Param("virusFound") Boolean virusFound,
                     @Param("virusType") String virusType,
                     @Param("virusName") String virusName,
                     @Param("actionTaken") String actionTaken);

    /**
     * 删除扫描记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除过期的记录
     */
    int batchDeleteExpired(@Param("expireDays") Integer expireDays);
}
