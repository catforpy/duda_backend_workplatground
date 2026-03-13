package com.duda.file.provider.mapper;

import com.duda.file.provider.entity.ContentDetectionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 内容安全检测记录Mapper接口
 *
 * @author duda
 * @date 2025-03-13
 */
@Mapper
public interface ContentDetectionRecordMapper {

    /**
     * 根据ID查询
     */
    ContentDetectionRecord selectById(@Param("id") Long id);

    /**
     * 根据任务ID查询
     */
    ContentDetectionRecord selectByTaskId(@Param("taskId") String taskId);

    /**
     * 根据Bucket名称查询检测记录
     */
    List<ContentDetectionRecord> selectByBucketName(@Param("bucketName") String bucketName);

    /**
     * 根据Bucket和对象键查询
     */
    List<ContentDetectionRecord> selectByBucketAndKey(@Param("bucketName") String bucketName,
                                                      @Param("objectKey") String objectKey);

    /**
     * 根据检测类型查询记录
     */
    List<ContentDetectionRecord> selectByDetectionType(@Param("detectionType") String detectionType);

    /**
     * 根据状态查询记录
     */
    List<ContentDetectionRecord> selectByStatus(@Param("status") String status);

    /**
     * 根据风险等级查询记录
     */
    List<ContentDetectionRecord> selectByRiskLevel(@Param("riskLevel") String riskLevel);

    /**
     * 插入检测记录
     */
    int insert(ContentDetectionRecord contentDetectionRecord);

    /**
     * 更新检测记录
     */
    int update(ContentDetectionRecord contentDetectionRecord);

    /**
     * 更新检测状态
     */
    int updateStatus(@Param("taskId") String taskId, @Param("status") String status);

    /**
     * 更新检测结果
     */
    int updateResult(@Param("taskId") String taskId, @Param("riskLevel") String riskLevel, @Param("actionTaken") String actionTaken);

    /**
     * 删除检测记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除过期的记录
     */
    int batchDeleteExpired(@Param("expireDays") Integer expireDays);
}
