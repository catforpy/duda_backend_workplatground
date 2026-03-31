package com.duda.tenant.api.service.impl;

import com.duda.tenant.api.dto.TenantUserRelationDTO;
import com.duda.tenant.api.rpc.TenantUserRelationRpc;
import com.duda.tenant.api.service.TenantUserRelationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租户用户关系Service实现类
 *
 * <p>负责处理租户用户关系相关的业务逻辑，通过Dubbo RPC调用duda-tenant-provider服务</p>
 *
 * @author Claude Code
 * @since 2026-03-31
 * @version 1.0.0
 */
@Slf4j
@Service
public class TenantUserRelationServiceImpl implements TenantUserRelationService {

    /**
     * Dubbo RPC引用 - 租户用户关系服务
     */
    @DubboReference(group = "DUDA_TENANT_GROUP", version = "1.0.0")
    private TenantUserRelationRpc tenantUserRelationRpc;

    @Override
    public TenantUserRelationDTO joinTenant(Long userId, Long tenantId, String roleCode, Integer isPrimary) {
        log.info("用户加入小程序: userId={}, tenantId={}, roleCode={}",
                userId, tenantId, roleCode);

        return tenantUserRelationRpc.joinTenant(userId, tenantId, roleCode, isPrimary);
    }

    @Override
    public List<TenantUserRelationDTO> getTenantsByUserId(Long userId) {
        log.info("查询用户的小程序列表: userId={}", userId);

        return tenantUserRelationRpc.getTenantsByUserId(userId);
    }

    @Override
    public List<TenantUserRelationDTO> getUsersByTenantId(Long tenantId) {
        log.info("查询小程序的用户列表: tenantId={}", tenantId);

        return tenantUserRelationRpc.getUsersByTenantId(tenantId);
    }

    @Override
    public Boolean checkRelation(Long userId, Long tenantId) {
        log.info("检查用户是否已关联小程序: userId={}, tenantId={}", userId, tenantId);

        return tenantUserRelationRpc.checkRelation(userId, tenantId);
    }

    @Override
    public Boolean leaveTenant(Long userId, Long tenantId) {
        log.info("用户离开小程序: userId={}, tenantId={}", userId, tenantId);

        return tenantUserRelationRpc.leaveTenant(userId, tenantId);
    }

    @Override
    public TenantUserRelationDTO getRelationById(Long id) {
        log.info("查询关联关系: id={}", id);

        return tenantUserRelationRpc.getRelationById(id);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        log.info("更新用户状态: id={}, status={}", id, status);

        return tenantUserRelationRpc.updateStatus(id, status);
    }
}
