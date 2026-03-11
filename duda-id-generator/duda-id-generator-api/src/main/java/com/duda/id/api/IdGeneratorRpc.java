package com.duda.id.api;

/**
 * 雪花ID生成RPC接口
 * 提供分布式唯一ID生成服务
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
public interface IdGeneratorRpc {

    /**
     * 生成用户ID
     * @return 用户ID
     */
    Long generateUserId();

    /**
     * 生成订单ID
     * @return 订单ID
     */
    Long generateOrderId();

    /**
     * 生成通用业务ID
     * @param businessType 业务类型（如：user、order、payment等）
     * @return 业务ID
     */
    Long generateBusinessId(String businessType);

    /**
     * 批量生成ID
     * @param businessType 业务类型
     * @param count 生成数量
     * @return ID数组
     */
    Long[] generateBatch(String businessType, int count);
}
