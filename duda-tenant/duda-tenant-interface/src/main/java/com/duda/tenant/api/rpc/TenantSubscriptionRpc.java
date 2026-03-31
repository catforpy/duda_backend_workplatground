package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.TenantSubscriptionDTO;
import com.duda.tenant.api.dto.TenantSubscriptionLimitDTO;

import java.util.List;

/**
 * 租户订阅RPC接口
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface TenantSubscriptionRpc {

    /**
     * 根据ID查询订阅
     *
     * @param subscriptionId 订阅ID
     * @return 订阅DTO
     */
    TenantSubscriptionDTO getSubscriptionById(Long subscriptionId);

    /**
     * 根据订阅编号查询
     *
     * @param subscriptionCode 订阅编号
     * @return 订阅DTO
     */
    TenantSubscriptionDTO getSubscriptionByCode(String subscriptionCode);

    /**
     * 查询租户的所有订阅
     *
     * @param tenantId 租户ID
     * @return 订阅列表
     */
    List<TenantSubscriptionDTO> listSubscriptionsByTenantId(Long tenantId);

    /**
     * 查询用户的所有订阅
     *
     * @param userId 用户ID
     * @return 订阅列表
     */
    List<TenantSubscriptionDTO> listSubscriptionsByUserId(Long userId);

    /**
     * 查询租户用户的订阅
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 订阅列表
     */
    List<TenantSubscriptionDTO> listSubscriptionsByTenantAndUser(Long tenantId, Long userId);

    /**
     * 查询生效中的订阅
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 生效中的订阅
     */
    TenantSubscriptionDTO getActiveSubscription(Long tenantId, Long userId);

    /**
     * 创建订阅
     *
     * @param subscriptionDTO 订阅DTO
     * @return 创建的订阅
     */
    TenantSubscriptionDTO createSubscription(TenantSubscriptionDTO subscriptionDTO);

    /**
     * 更新订阅
     *
     * @param subscriptionDTO 订阅DTO
     * @return 更新后的订阅
     */
    TenantSubscriptionDTO updateSubscription(TenantSubscriptionDTO subscriptionDTO);

    /**
     * 取消订阅
     *
     * @param subscriptionId 订阅ID
     * @param cancelReason 取消原因
     * @param cancelBy 取消操作人
     * @return 是否成功
     */
    Boolean cancelSubscription(Long subscriptionId, String cancelReason, Long cancelBy);

    /**
     * 暂停订阅
     *
     * @param subscriptionId 订阅ID
     * @return 是否成功
     */
    Boolean suspendSubscription(Long subscriptionId);

    /**
     * 激活订阅
     *
     * @param subscriptionId 订阅ID
     * @return 是否成功
     */
    Boolean activateSubscription(Long subscriptionId);

    /**
     * 续费订阅
     *
     * @param subscriptionId 订阅ID
     * @param months 续费月数
     * @return 是否成功
     */
    Boolean renewSubscription(Long subscriptionId, Integer months);

    /**
     * 查询订阅的所有限制条件
     *
     * @param subscriptionId 订阅ID
     * @return 限制条件列表
     */
    List<TenantSubscriptionLimitDTO> listSubscriptionLimits(Long subscriptionId);

    /**
     * 根据订阅ID和限制类型查询
     *
     * @param subscriptionId 订阅ID
     * @param limitKey 限制类型
     * @return 限制条件DTO
     */
    TenantSubscriptionLimitDTO getSubscriptionLimit(Long subscriptionId, String limitKey);

    /**
     * 增加使用量
     *
     * @param limitId 限制条件ID
     * @param increment 增量
     * @return 是否成功
     */
    Boolean increaseUsage(Long limitId, Long increment);

    /**
     * 检查限制是否超出
     *
     * @param subscriptionId 订阅ID
     * @param limitKey 限制类型
     * @param requiredValue 需要的值
     * @return 是否超出
     */
    Boolean checkLimitExceeded(Long subscriptionId, String limitKey, Long requiredValue);
}
