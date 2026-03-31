package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.TenantCommissionRuleDTO;

import java.util.List;

/**
 * 分佣规则RPC接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantCommissionRuleRpc {

    /**
     * 根据ID查询
     */
    TenantCommissionRuleDTO getById(Long id);

    /**
     * 根据规则编码查询
     */
    TenantCommissionRuleDTO getByRuleCode(String ruleCode);

    /**
     * 查询租户的所有有效规则
     */
    List<TenantCommissionRuleDTO> listActiveRules(Long tenantId);

    /**
     * 创建分佣规则
     */
    TenantCommissionRuleDTO create(TenantCommissionRuleDTO dto);

    /**
     * 更新分佣规则
     */
    TenantCommissionRuleDTO update(TenantCommissionRuleDTO dto);

    /**
     * 暂停规则
     */
    Boolean suspend(Long id);

    /**
     * 激活规则
     */
    Boolean activate(Long id);
}
