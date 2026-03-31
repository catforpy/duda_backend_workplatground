package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.TenantSalesAgentDTO;

import java.util.List;

/**
 * 销售商RPC接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantSalesAgentRpc {

    /**
     * 根据ID查询
     */
    TenantSalesAgentDTO getById(Long id);

    /**
     * 根据推荐码查询
     */
    TenantSalesAgentDTO getByReferralCode(String referralCode);

    /**
     * 查询所有销售商
     */
    List<TenantSalesAgentDTO> listAllAgents();

    /**
     * 创建销售商
     */
    TenantSalesAgentDTO create(TenantSalesAgentDTO dto);

    /**
     * 更新销售商
     */
    TenantSalesAgentDTO update(TenantSalesAgentDTO dto);
}
