package com.duda.file.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.file.provider.entity.BucketAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * Bucket账户 Mapper 接口
 * 对应bucket_account表
 *
 * @author duda
 * @date 2025-03-27
 */
@Mapper
public interface BucketAccountMapper extends BaseMapper<BucketAccount> {

    /**
     * 根据Bucket名称查询账户
     */
    BucketAccount selectByBucketName(@Param("bucketName") String bucketName);

    /**
     * 根据租户ID查询账户列表
     */
    java.util.List<BucketAccount> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据状态查询账户列表
     */
    java.util.List<BucketAccount> selectByStatus(@Param("status") String status);

    /**
     * 充值
     */
    int recharge(@Param("bucketName") String bucketName,
                 @Param("amount") BigDecimal amount,
                 @Param("userId") Long userId);

    /**
     * 扣费
     */
    int deduct(@Param("bucketName") String bucketName,
               @Param("amount") BigDecimal amount,
               @Param("version") Integer version);

    /**
     * 冻结余额
     */
    int freeze(@Param("bucketName") String bucketName,
               @Param("amount") BigDecimal amount,
               @Param("version") Integer version);

    /**
     * 解冻余额
     */
    int unfreeze(@Param("bucketName") String bucketName,
                 @Param("amount") BigDecimal amount,
                 @Param("version") Integer version);

    /**
     * 更新信用额度
     */
    int updateCreditLine(@Param("bucketName") String bucketName,
                         @Param("creditLine") BigDecimal creditLine);

    /**
     * 更新账户状态
     */
    int updateStatus(@Param("bucketName") String bucketName,
                     @Param("status") String status,
                     @Param("reason") String reason);
}
