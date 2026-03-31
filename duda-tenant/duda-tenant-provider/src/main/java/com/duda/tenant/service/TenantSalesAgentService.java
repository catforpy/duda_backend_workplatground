package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantSalesAgentDTO;
import com.duda.tenant.entity.TenantSalesAgent;

import java.util.List;

/**
 * 销售商表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantSalesAgentService extends IService<TenantSalesAgent> {

    /**
     * 创建销售商
     */
    TenantSalesAgent createAgent(TenantSalesAgentDTO dto);

    /**
     * 更新销售商
     */
    TenantSalesAgent updateAgent(TenantSalesAgentDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantSalesAgentDTO getAgentDTO(Long id);

    /**
     * 根据推荐码查询
     */
    TenantSalesAgentDTO getByReferralCode(String referralCode);

    /**
     * 查询所有销售商
     */
    List<TenantSalesAgentDTO> listAllAgents();
}
