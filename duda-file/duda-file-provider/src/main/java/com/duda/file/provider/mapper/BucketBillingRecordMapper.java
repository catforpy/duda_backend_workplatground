package com.duda.file.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.file.provider.entity.BucketBillingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Bucket费用账单Mapper
 *
 * @author duda
 * @date 2025-03-14
 */
@Mapper
public interface BucketBillingRecordMapper extends BaseMapper<BucketBillingRecord> {

    /**
     * 根据Bucket名称查询账单列表
     */
    List<BucketBillingRecord> selectByBucketName(@Param("bucketName") String bucketName);

    /**
     * 根据用户ID查询账单列表
     */
    List<BucketBillingRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据支付状态查询
     */
    List<BucketBillingRecord> selectByPaymentStatus(@Param("paymentStatus") String paymentStatus);

    /**
     * 根据周期查询账单
     */
    BucketBillingRecord selectByBucketNameAndCycle(
        @Param("bucketName") String bucketName,
        @Param("cycleStart") LocalDate cycleStart,
        @Param("cycleEnd") LocalDate cycleEnd
    );

    /**
     * 查询逾期未支付账单
     */
    List<BucketBillingRecord> selectOverdueRecords();

    /**
     * 更新支付状态
     */
    int updatePaymentStatus(
        @Param("id") Long id,
        @Param("paymentStatus") String paymentStatus,
        @Param("paymentMethod") String paymentMethod,
        @Param("transactionId") String transactionId
    );
}
