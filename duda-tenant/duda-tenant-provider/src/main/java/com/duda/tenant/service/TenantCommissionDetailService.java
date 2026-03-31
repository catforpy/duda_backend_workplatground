package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantCommissionDetailDTO;
import com.duda.tenant.entity.TenantCommissionDetail;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分佣明细表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantCommissionDetailService extends IService<TenantCommissionDetail> {

    /**
     * 创建分佣明细
     */
    TenantCommissionDetail createDetail(TenantCommissionDetailDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantCommissionDetailDTO getDetailDTO(Long id);

    /**
     * 查询订单的分佣明细
     */
    List<TenantCommissionDetailDTO> listByOrderId(Long orderId);

    /**
     * 查询受益人的分佣明细
     */
    List<TenantCommissionDetailDTO> listByBeneficiary(Long beneficiaryId, String status);

    /**
     * 查询对账周期的分佣明细
     */
    List<TenantCommissionDetailDTO> listByPeriodId(Long settlementPeriodId);

    /**
     * 结算分佣
     */
    Boolean settle(Long id, String settledMethod);

    /**
     * 批量结算分佣
     */
    Boolean batchSettle(Long settlementPeriodId, String settledMethod);

    /**
     * 计算订单分佣金额
     */
    BigDecimal calculateCommission(Long orderId, BigDecimal orderAmount);
}
