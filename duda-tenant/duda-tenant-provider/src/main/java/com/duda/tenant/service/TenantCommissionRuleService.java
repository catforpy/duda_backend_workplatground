package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantCommissionRuleDTO;
import com.duda.tenant.entity.TenantCommissionRule;

import java.util.List;

/**
 * 分佣规则表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantCommissionRuleService extends IService<TenantCommissionRule> {

    /**
     * 创建分佣规则
     */
    TenantCommissionRule createRule(TenantCommissionRuleDTO dto);

    /**
     * 更新分佣规则
     */
    TenantCommissionRule updateRule(TenantCommissionRuleDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantCommissionRuleDTO getRuleDTO(Long id);

    /**
     * 根据规则编码查询
     */
    TenantCommissionRuleDTO getByRuleCode(String ruleCode);

    /**
     * 查询租户的所有有效规则
     */
    List<TenantCommissionRuleDTO> listActiveRules(Long tenantId);

    /**
     * 暂停规则
     */
    Boolean suspend(Long id);

    /**
     * 激活规则
     */
    Boolean activate(Long id);
}
