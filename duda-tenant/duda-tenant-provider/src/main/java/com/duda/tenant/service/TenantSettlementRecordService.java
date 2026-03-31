package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantSettlementRecordDTO;
import com.duda.tenant.entity.TenantSettlementRecord;

import java.util.List;

/**
 * 结算流水表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantSettlementRecordService extends IService<TenantSettlementRecord> {

    /**
     * 创建结算流水
     */
    TenantSettlementRecord createRecord(TenantSettlementRecordDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantSettlementRecordDTO getRecordDTO(Long id);

    /**
     * 根据转账流水号查询
     */
    TenantSettlementRecordDTO getByTransferNo(String transferNo);

    /**
     * 查询对账周期的所有结算流水
     */
    List<TenantSettlementRecordDTO> listByPeriodId(Long settlementPeriodId);

    /**
     * 发起转账
     */
    Boolean initiateTransfer(Long id);

    /**
     * 重试转账
     */
    Boolean retryTransfer(Long id);

    /**
     * 确认转账成功
     */
    Boolean confirmSuccess(Long id);
}
