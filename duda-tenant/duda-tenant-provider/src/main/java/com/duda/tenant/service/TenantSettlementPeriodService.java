package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantSettlementPeriodDTO;
import com.duda.tenant.entity.TenantSettlementPeriod;

import java.util.List;

/**
 * 对账周期表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantSettlementPeriodService extends IService<TenantSettlementPeriod> {

    /**
     * 创建对账周期
     */
    TenantSettlementPeriod createPeriod(TenantSettlementPeriodDTO dto);

    /**
     * 更新对账周期
     */
    TenantSettlementPeriod updatePeriod(TenantSettlementPeriodDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantSettlementPeriodDTO getPeriodDTO(Long id);

    /**
     * 根据周期号查询
     */
    TenantSettlementPeriodDTO getByPeriodNo(String periodNo);

    /**
     * 查询待对账的周期列表
     */
    List<TenantSettlementPeriodDTO> listPendingPeriods();

    /**
     * 生成对账周期
     */
    TenantSettlementPeriod generatePeriod(Long tenantId, Long merchantId);

    /**
     * 完成对账
     */
    Boolean settle(Long id, Long settledBy);
}
