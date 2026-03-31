package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantSubscriptionDTO;
import com.duda.tenant.entity.TenantSubscription;

import java.util.List;

/**
 * 租户订阅服务接口
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface TenantSubscriptionService extends IService<TenantSubscription> {

    /**
     * 创建订阅
     *
     * @param subscription 订阅实体
     * @return 创建的订阅
     */
    TenantSubscription createSubscription(TenantSubscription subscription);

    /**
     * 更新订阅
     *
     * @param subscription 订阅实体
     * @return 更新后的订阅
     */
    TenantSubscription updateSubscription(TenantSubscription subscription);

    /**
     * 根据ID查询订阅DTO
     *
     * @param id 订阅ID
     * @return 订阅DTO
     */
    TenantSubscriptionDTO getSubscriptionDTO(Long id);

    /**
     * 根据订阅编号查询
     *
     * @param subscriptionCode 订阅编号
     * @return 订阅DTO
     */
    TenantSubscriptionDTO getBySubscriptionCode(String subscriptionCode);

    /**
     * 查询租户的所有订阅
     *
     * @param tenantId 租户ID
     * @return 订阅列表
     */
    List<TenantSubscriptionDTO> listByTenantId(Long tenantId);

    /**
     * 查询用户的所有订阅
     *
     * @param userId 用户ID
     * @return 订阅列表
     */
    List<TenantSubscriptionDTO> listByUserId(Long userId);

    /**
     * 查询租户用户的订阅
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 订阅列表
     */
    List<TenantSubscriptionDTO> listByTenantAndUser(Long tenantId, Long userId);

    /**
     * 查询生效中的订阅
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 生效中的订阅
     */
    TenantSubscriptionDTO getActiveSubscription(Long tenantId, Long userId);

    /**
     * 查询即将到期的订阅（7天内）
     *
     * @return 订阅列表
     */
    List<TenantSubscriptionDTO> listExpiringSoon();

    /**
     * 查询已过期的订阅
     *
     * @return 订阅列表
     */
    List<TenantSubscriptionDTO> listExpired();

    /**
     * 取消订阅
     *
     * @param id 订阅ID
     * @param cancelReason 取消原因
     * @param cancelBy 取消操作人
     * @return 是否成功
     */
    Boolean cancelSubscription(Long id, String cancelReason, Long cancelBy);

    /**
     * 暂停订阅
     *
     * @param id 订阅ID
     * @return 是否成功
     */
    Boolean suspendSubscription(Long id);

    /**
     * 激活订阅
     *
     * @param id 订阅ID
     * @return 是否成功
     */
    Boolean activateSubscription(Long id);

    /**
     * 续费订阅
     *
     * @param id 订阅ID
     * @param months 续费月数
     * @return 是否成功
     */
    Boolean renewSubscription(Long id, Integer months);

    /**
     * 检查并更新过期订阅状态
     *
     * @return 更新的数量
     */
    Integer checkAndUpdateExpired();
}
