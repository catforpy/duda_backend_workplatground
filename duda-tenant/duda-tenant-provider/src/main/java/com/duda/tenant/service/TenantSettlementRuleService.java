package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantSettlementRuleDTO;
import com.duda.tenant.entity.TenantSettlementRule;

/**
 * 结算规则表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantSettlementRuleService extends IService<TenantSettlementRule> {

    /**
     * 创建结算规则
     */
    TenantSettlementRule createRule(TenantSettlementRuleDTO dto);

    /**
     * 更新结算规则
     */
    TenantSettlementRule updateRule(TenantSettlementRuleDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantSettlementRuleDTO getRuleDTO(Long id);

    /**
     * 查询租户的结算规则
     */
    TenantSettlementRuleDTO getTenantRule(Long tenantId, Long merchantId);

    /**
     * 暂停结算规则
     */
    Boolean suspend(Long id);

    /**
     * 激活结算规则
     */
    Boolean activate(Long id);
}
