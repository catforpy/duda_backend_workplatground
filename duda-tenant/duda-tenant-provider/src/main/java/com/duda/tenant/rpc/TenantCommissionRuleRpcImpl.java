package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.TenantCommissionRuleDTO;
import com.duda.tenant.api.rpc.TenantCommissionRuleRpc;
import com.duda.tenant.entity.TenantCommissionRule;
import com.duda.tenant.service.TenantCommissionRuleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 分佣规则RPC实现
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantCommissionRuleRpcImpl implements TenantCommissionRuleRpc {

    @Autowired
    private TenantCommissionRuleService service;

    @Override
    public TenantCommissionRuleDTO getById(Long id) {
        log.info("RPC调用: getById, ruleId={}", id);
        return service.getRuleDTO(id);
    }

    @Override
    public TenantCommissionRuleDTO getByRuleCode(String ruleCode) {
        log.info("RPC调用: getByRuleCode, ruleCode={}", ruleCode);
        return service.getByRuleCode(ruleCode);
    }

    @Override
    public List<TenantCommissionRuleDTO> listActiveRules(Long tenantId) {
        log.info("RPC调用: listActiveRules, tenantId={}", tenantId);
        return service.listActiveRules(tenantId);
    }

    @Override
    public TenantCommissionRuleDTO create(TenantCommissionRuleDTO dto) {
        log.info("RPC调用: create, tenantId={}, ruleCode={}", dto.getTenantId(), dto.getRuleCode());
        TenantCommissionRule entity = service.createRule(dto);
        return entityToDto(entity);
    }

    @Override
    public TenantCommissionRuleDTO update(TenantCommissionRuleDTO dto) {
        log.info("RPC调用: update, ruleId={}", dto.getId());
        TenantCommissionRule entity = service.updateRule(dto);
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

    private TenantCommissionRuleDTO entityToDto(TenantCommissionRule entity) {
        if (entity == null) return null;
        TenantCommissionRuleDTO dto = new TenantCommissionRuleDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
