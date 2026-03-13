package com.duda.file.provider.mapper;

import com.duda.file.provider.entity.SensitiveDataDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 敏感数据详情Mapper接口
 *
 * @author duda
 * @date 2025-03-13
 */
@Mapper
public interface SensitiveDataDetailMapper {

    /**
     * 根据ID查询
     */
    SensitiveDataDetail selectById(@Param("id") Long id);

    /**
     * 根据扫描任务ID查询详情列表
     */
    List<SensitiveDataDetail> selectByScanTaskId(@Param("scanTaskId") String scanTaskId);

    /**
     * 根据Bucket和对象键查询
     */
    List<SensitiveDataDetail> selectByBucketAndKey(@Param("bucketName") String bucketName, @Param("objectKey") String objectKey);

    /**
     * 根据敏感数据类型查询
     */
    List<SensitiveDataDetail> selectByDataType(@Param("dataType") String dataType);

    /**
     * 根据敏感等级查询
     */
    List<SensitiveDataDetail> selectByDataLevel(@Param("dataLevel") String dataLevel);

    /**
     * 根据风险等级查询
     */
    List<SensitiveDataDetail> selectByRiskLevel(@Param("riskLevel") String riskLevel);

    /**
     * 插入敏感数据详情
     */
    int insert(SensitiveDataDetail sensitiveDataDetail);

    /**
     * 批量插入敏感数据详情
     */
    int batchInsert(@Param("list") List<SensitiveDataDetail> list);

    /**
     * 更新敏感数据详情
     */
    int update(SensitiveDataDetail sensitiveDataDetail);

    /**
     * 更新处理操作
     */
    int updateAction(@Param("id") Long id, @Param("actionTaken") String actionTaken);

    /**
     * 删除敏感数据详情
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除根据任务ID
     */
    int batchDeleteByScanTaskId(@Param("scanTaskId") String scanTaskId);

    /**
     * 统计扫描任务发现的敏感数据数量
     */
    Long countByScanTaskId(@Param("scanTaskId") String scanTaskId);
}
