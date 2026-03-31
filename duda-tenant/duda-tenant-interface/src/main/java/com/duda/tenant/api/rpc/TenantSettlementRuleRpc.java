package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.TenantSettlementRuleDTO;

/**
 * 结算规则RPC接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantSettlementRuleRpc {

    /**
     * 根据ID查询
     */
    TenantSettlementRuleDTO getById(Long id);

    /**
     * 查询租户的结算规则
     */
    TenantSettlementRuleDTO getTenantRule(Long tenantId, Long merchantId);

    /**
     * 创建结算规则
     */
    TenantSettlementRuleDTO create(TenantSettlementRuleDTO dto);

    /**
     * 更新结算规则
     */
    TenantSettlementRuleDTO update(TenantSettlementRuleDTO dto);

    /**
     * 暂停规则
     */
    Boolean suspend(Long id);

    /**
     * 激活规则
     */
    Boolean activate(Long id);
}
