package com.duda.file.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.file.provider.entity.BucketBillingConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * Bucket费用配置Mapper
 *
 * @author duda
 * @date 2025-03-14
 */
@Mapper
public interface BucketBillingConfigMapper extends BaseMapper<BucketBillingConfig> {

    /**
     * 根据Bucket名称查询
     */
    BucketBillingConfig selectByBucketName(@Param("bucketName") String bucketName);

    /**
     * 根据用户ID查询列表
     */
    java.util.List<BucketBillingConfig> selectByUserId(@Param("userId") Long userId);

    /**
     * 更新余额
     */
    int updateBalance(@Param("bucketName") String bucketName, @Param("amount") BigDecimal amount);

    /**
     * 更新状态
     */
    int updateStatus(@Param("bucketName") String bucketName, @Param("status") String status, @Param("reason") String reason);
}
