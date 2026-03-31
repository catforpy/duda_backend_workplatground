package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.TenantAuthorizationDTO;
import com.duda.tenant.api.rpc.TenantAuthorizationRpc;
import com.duda.tenant.entity.TenantAuthorization;
import com.duda.tenant.service.TenantAuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 授权管理RPC实现
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantAuthorizationRpcImpl implements TenantAuthorizationRpc {

    @Autowired
    private TenantAuthorizationService service;

    @Override
    public TenantAuthorizationDTO getById(Long id) {
        log.info("RPC调用: getById, authorizationId={}", id);
        return service.getAuthorizationDTO(id);
    }

    @Override
    public TenantAuthorizationDTO getByAuthorizationCode(String authorizationCode) {
        log.info("RPC调用: getByAuthorizationCode, authorizationCode={}", authorizationCode);
        return service.getByAuthorizationCode(authorizationCode);
    }

    @Override
    public List<TenantAuthorizationDTO> listByTenant(Long tenantId) {
        log.info("RPC调用: listByTenant, tenantId={}", tenantId);
        return service.listByTenant(tenantId);
    }

    @Override
    public TenantAuthorizationDTO create(TenantAuthorizationDTO dto) {
        log.info("RPC调用: create, tenantId={}, merchantId={}", dto.getTenantId(), dto.getMerchantId());
        TenantAuthorization entity = service.createAuthorization(dto);
        return entityToDto(entity);
    }

    @Override
    public TenantAuthorizationDTO update(TenantAuthorizationDTO dto) {
        log.info("RPC调用: update, authorizationId={}", dto.getId());
        TenantAuthorization entity = service.updateAuthorization(dto);
        return entityToDto(entity);
    }

    @Override
    public Boolean suspend(Long id) {
        log.info("RPC调用: suspend, authorizationId={}", id);
        return service.suspend(id);
    }

    @Override
    public Boolean activate(Long id) {
        log.info("RPC调用: activate, authorizationId={}", id);
        return service.activate(id);
    }

    @Override
    public Boolean terminate(Long id) {
        log.info("RPC调用: terminate, authorizationId={}", id);
        return service.terminate(id);
    }

    private TenantAuthorizationDTO entityToDto(TenantAuthorization entity) {
        if (entity == null) return null;
        TenantAuthorizationDTO dto = new TenantAuthorizationDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
