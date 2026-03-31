package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.TenantSettlementPeriodDTO;
import com.duda.tenant.api.rpc.TenantSettlementPeriodRpc;
import com.duda.tenant.service.TenantSettlementPeriodService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 对账周期RPC实现
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantSettlementPeriodRpcImpl implements TenantSettlementPeriodRpc {

    @Autowired
    private TenantSettlementPeriodService service;

    @Override
    public TenantSettlementPeriodDTO getById(Long id) {
        log.info("RPC调用: getById, periodId={}", id);
        return service.getPeriodDTO(id);
    }

    @Override
    public TenantSettlementPeriodDTO getByPeriodNo(String periodNo) {
        log.info("RPC调用: getByPeriodNo, periodNo={}", periodNo);
        return service.getByPeriodNo(periodNo);
    }

    @Override
    public List<TenantSettlementPeriodDTO> listPendingPeriods() {
        log.info("RPC调用: listPendingPeriods");
        return service.listPendingPeriods();
    }

    @Override
    public Boolean settle(Long id, Long settledBy) {
        log.info("RPC调用: settle, periodId={}, settledBy={}", id, settledBy);
        return service.settle(id, settledBy);
    }
}
