package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.TenantUserRelationDTO;
import com.duda.tenant.api.rpc.TenantUserRelationRpc;
import com.duda.tenant.entity.TenantUserRelation;
import com.duda.tenant.service.TenantUserRelationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户用户关系RPC实现
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantUserRelationRpcImpl implements TenantUserRelationRpc {

    @Autowired
    private TenantUserRelationService tenantUserRelationService;

    @Override
    public TenantUserRelationDTO joinTenant(Long userId, Long tenantId, String roleCode, Integer isPrimary) {
        log.info("RPC调用: joinTenant, userId={}, tenantId={}, roleCode={}",
                userId, tenantId, roleCode);

        Long relationId = tenantUserRelationService.joinTenant(userId, tenantId, roleCode, isPrimary);

        TenantUserRelation relation = tenantUserRelationService.getById(relationId);
        return entityToDto(relation);
    }

    @Override
    public List<TenantUserRelationDTO> getTenantsByUserId(Long userId) {
        log.info("RPC调用: getTenantsByUserId, userId={}", userId);

        List<TenantUserRelation> relations = tenantUserRelationService.getTenantsByUserId(userId);
        return relations.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantUserRelationDTO> getUsersByTenantId(Long tenantId) {
        log.info("RPC调用: getUsersByTenantId, tenantId={}", tenantId);

        List<TenantUserRelation> relations = tenantUserRelationService.getUsersByTenantId(tenantId);
        return relations.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean checkRelation(Long userId, Long tenantId) {
        log.info("RPC调用: checkRelation, userId={}, tenantId={}", userId, tenantId);

        return tenantUserRelationService.checkRelation(userId, tenantId);
    }

    @Override
    public Boolean leaveTenant(Long userId, Long tenantId) {
        log.info("RPC调用: leaveTenant, userId={}, tenantId={}", userId, tenantId);

        return tenantUserRelationService.leaveTenant(userId, tenantId);
    }

    @Override
    public TenantUserRelationDTO getRelationById(Long id) {
        log.info("RPC调用: getRelationById, id={}", id);

        TenantUserRelation relation = tenantUserRelationService.getById(id);
        return entityToDto(relation);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        log.info("RPC调用: updateStatus, id={}, status={}", id, status);

        return tenantUserRelationService.updateStatus(id, status);
    }

    /**
     * Entity转DTO
     */
    private TenantUserRelationDTO entityToDto(TenantUserRelation entity) {
        if (entity == null) {
            return null;
        }
        TenantUserRelationDTO dto = new TenantUserRelationDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
