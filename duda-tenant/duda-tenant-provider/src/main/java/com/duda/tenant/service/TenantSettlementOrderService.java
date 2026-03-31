package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantSettlementOrderDTO;
import com.duda.tenant.entity.TenantSettlementOrder;

import java.util.List;

/**
 * 对账订单明细表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantSettlementOrderService extends IService<TenantSettlementOrder> {

    /**
     * 添加订单到对账周期
     */
    TenantSettlementOrder addOrder(TenantSettlementOrderDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantSettlementOrderDTO getOrderDTO(Long id);

    /**
     * 查询对账周期的所有订单
     */
    List<TenantSettlementOrderDTO> listByPeriodId(Long settlementPeriodId);

    /**
     * 查询订单是否已归集
     */
    Boolean isOrderCollected(Long orderId);
}
