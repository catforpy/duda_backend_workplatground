package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantSubscriptionLimitDTO;
import com.duda.tenant.entity.TenantSubscriptionLimit;

import java.util.List;

/**
 * 租户订阅限制条件服务接口
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface TenantSubscriptionLimitService extends IService<TenantSubscriptionLimit> {

    /**
     * 创建限制条件
     *
     * @param limit 限制条件实体
     * @return 创建的限制条件
     */
    TenantSubscriptionLimit createLimit(TenantSubscriptionLimit limit);

    /**
     * 更新限制条件
     *
     * @param limit 限制条件实体
     * @return 更新后的限制条件
     */
    TenantSubscriptionLimit updateLimit(TenantSubscriptionLimit limit);

    /**
     * 根据ID查询限制条件DTO
     *
     * @param id 限制条件ID
     * @return 限制条件DTO
     */
    TenantSubscriptionLimitDTO getLimitDTO(Long id);

    /**
     * 查询订阅的所有限制条件
     *
     * @param subscriptionId 订阅ID
     * @return 限制条件列表
     */
    List<TenantSubscriptionLimitDTO> listBySubscriptionId(Long subscriptionId);

    /**
     * 根据订阅ID和限制类型查询
     *
     * @param subscriptionId 订阅ID
     * @param limitKey 限制类型
     * @return 限制条件DTO
     */
    TenantSubscriptionLimitDTO getBySubscriptionAndKey(Long subscriptionId, String limitKey);

    /**
     * 增加使用量
     *
     * @param id 限制条件ID
     * @param increment 增量
     * @return 是否成功
     */
    Boolean increaseUsage(Long id, Long increment);

    /**
     * 重置使用量
     *
     * @param id 限制条件ID
     * @return 是否成功
     */
    Boolean resetUsage(Long id);

    /**
     * 批量重置周期性限制的使用量
     *
     * @return 重置的数量
     */
    Integer resetPeriodicLimits();

    /**
     * 检查限制是否超出
     *
     * @param subscriptionId 订阅ID
     * @param limitKey 限制类型
     * @param requiredValue 需要的值
     * @return 是否超出
     */
    Boolean checkLimitExceeded(Long subscriptionId, String limitKey, Long requiredValue);

    /**
     * 查询需要告警的限制条件
     *
     * @return 限制条件列表
     */
    List<TenantSubscriptionLimitDTO> listAlertLimits();

    /**
     * 批量创建限制条件
     *
     * @param subscriptionId 订阅ID
     * @param limitList 限制条件列表
     * @return 创建的数量
     */
    Integer batchCreateLimits(Long subscriptionId, List<TenantSubscriptionLimit> limitList);
}
