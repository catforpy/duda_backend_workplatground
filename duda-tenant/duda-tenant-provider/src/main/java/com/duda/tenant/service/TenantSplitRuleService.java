package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantSplitRuleDTO;
import com.duda.tenant.entity.TenantSplitRule;

import java.util.List;

/**
 * 分账规则表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantSplitRuleService extends IService<TenantSplitRule> {

    /**
     * 创建分账规则
     */
    TenantSplitRule createRule(TenantSplitRuleDTO dto);

    /**
     * 更新分账规则
     */
    TenantSplitRule updateRule(TenantSplitRuleDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantSplitRuleDTO getRuleDTO(Long id);

    /**
     * 查询合作的分账规则
     */
    List<TenantSplitRuleDTO> listByCooperation(Long cooperationId);
}
