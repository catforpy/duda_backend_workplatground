package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.TenantSettlementRuleDTO;
import com.duda.tenant.api.rpc.TenantSettlementRuleRpc;
import com.duda.tenant.entity.TenantSettlementRule;
import com.duda.tenant.service.TenantSettlementRuleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 结算规则RPC实现
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantSettlementRuleRpcImpl implements TenantSettlementRuleRpc {

    @Autowired
    private TenantSettlementRuleService service;

    @Override
    public TenantSettlementRuleDTO getById(Long id) {
        log.info("RPC调用: getById, ruleId={}", id);
        return service.getRuleDTO(id);
    }

    @Override
    public TenantSettlementRuleDTO getTenantRule(Long tenantId, Long merchantId) {
        log.info("RPC调用: getTenantRule, tenantId={}, merchantId={}", tenantId, merchantId);
        return service.getTenantRule(tenantId, merchantId);
    }

    @Override
    public TenantSettlementRuleDTO create(TenantSettlementRuleDTO dto) {
        log.info("RPC调用: create, tenantId={}, merchantId={}", dto.getTenantId(), dto.getMerchantId());
        TenantSettlementRule entity = service.createRule(dto);
        return entityToDto(entity);
    }

    @Override
    public TenantSettlementRuleDTO update(TenantSettlementRuleDTO dto) {
        log.info("RPC调用: update, ruleId={}", dto.getId());
        TenantSettlementRule entity = service.updateRule(dto);
        return entityToDto(entity);
    }

    @Override
    public Boolean suspend(Long id) {
        log.info("RPC调用: suspend, ruleId={}", id);
        return service.suspend(id);
    }

    @Override
    public Boolean activate(Long id) {
        log.info("RPC调用: activate, ruleId={}", id);
        return service.activate(id);
    }

    private TenantSettlementRuleDTO entityToDto(TenantSettlementRule entity) {
        if (entity == null) return null;
        TenantSettlementRuleDTO dto = new TenantSettlementRuleDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
