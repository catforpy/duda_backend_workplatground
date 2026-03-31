package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.TenantSettlementPeriodDTO;

import java.util.List;

/**
 * 对账周期RPC接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantSettlementPeriodRpc {

    /**
     * 根据ID查询
     */
    TenantSettlementPeriodDTO getById(Long id);

    /**
     * 根据周期号查询
     */
    TenantSettlementPeriodDTO getByPeriodNo(String periodNo);

    /**
     * 查询待对账的周期列表
     */
    List<TenantSettlementPeriodDTO> listPendingPeriods();

    /**
     * 完成对账
     */
    Boolean settle(Long id, Long settledBy);
}
