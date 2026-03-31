package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.TenantSalesAgentDTO;
import com.duda.tenant.api.rpc.TenantSalesAgentRpc;
import com.duda.tenant.entity.TenantSalesAgent;
import com.duda.tenant.service.TenantSalesAgentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 销售商RPC实现
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantSalesAgentRpcImpl implements TenantSalesAgentRpc {

    @Autowired
    private TenantSalesAgentService service;

    @Override
    public TenantSalesAgentDTO getById(Long id) {
        log.info("RPC调用: getById, agentId={}", id);
        return service.getAgentDTO(id);
    }

    @Override
    public TenantSalesAgentDTO getByReferralCode(String referralCode) {
        log.info("RPC调用: getByReferralCode, referralCode={}", referralCode);
        return service.getByReferralCode(referralCode);
    }

    @Override
    public List<TenantSalesAgentDTO> listAllAgents() {
        log.info("RPC调用: listAllAgents");
        return service.listAllAgents();
    }

    @Override
    public TenantSalesAgentDTO create(TenantSalesAgentDTO dto) {
        log.info("RPC调用: create, agentName={}", dto.getAgentName());
        TenantSalesAgent entity = service.createAgent(dto);
        return entityToDto(entity);
    }

    @Override
    public TenantSalesAgentDTO update(TenantSalesAgentDTO dto) {
        log.info("RPC调用: update, agentId={}", dto.getId());
        TenantSalesAgent entity = service.updateAgent(dto);
        return entityToDto(entity);
    }

    private TenantSalesAgentDTO entityToDto(TenantSalesAgent entity) {
        if (entity == null) return null;
        TenantSalesAgentDTO dto = new TenantSalesAgentDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
