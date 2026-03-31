package com.duda.tenant.api.service.impl;

import com.duda.tenant.api.dto.TenantDTO;
import com.duda.tenant.api.dto.TenantCheckDTO;
import com.duda.tenant.api.rpc.TenantRpc;
import com.duda.tenant.api.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * 租户Service实现类
 * 
 * <p>负责处理租户相关的业务逻辑，通过Dubbo RPC调用duda-tenant-provider服务</p>
 * 
 * @author Claude Code
 * @since 2026-03-31
 * @version 1.0.0
 */
@Slf4j
@Service
public class TenantServiceImpl implements TenantService {

    /**
     * Dubbo RPC引用 - 租户服务
     */
    @DubboReference(group = "DUDA_TENANT_GROUP", version = "1.0.0")
    private TenantRpc tenantRpc;

    /**
     * 根据ID查询租户信息
     * 
     * @param id 租户ID
     * @return 租户信息DTO，如果不存在返回null
     */
    @Override
    public TenantDTO getTenantById(Long id) {
        log.debug("根据ID查询租户: id={}", id);
        return tenantRpc.getTenantById(id);
    }

    /**
     * 根据租户编码查询租户信息
     * 
     * @param tenantCode 租户编码（唯一标识）
     * @return 租户信息DTO，如果不存在返回null
     */
    @Override
    public TenantDTO getTenantByCode(String tenantCode) {
        log.debug("根据租户编码查询租户: tenantCode={}", tenantCode);
        return tenantRpc.getTenantByCode(tenantCode);
    }

    /**
     * 创建新租户
     * 
     * <p>此方法会创建租户基本信息，并自动创建对应的数据库Schema</p>
     * 
     * @param tenantDTO 租户信息DTO
     * @return 创建后的租户信息DTO（包含生成的ID）
     * @throws IllegalArgumentException 如果租户编码已存在
     */
    @Override
    public TenantDTO createTenant(TenantDTO tenantDTO) {
        log.info("创建租户: tenantCode={}, tenantName={}",
                tenantDTO.getTenantCode(), tenantDTO.getTenantName());
        return tenantRpc.createTenant(tenantDTO);
    }

    /**
     * 更新租户信息
     * 
     * @param tenantDTO 租户信息DTO（必须包含ID）
     * @return 更新后的租户信息DTO
     * @throws IllegalArgumentException 如果租户不存在
     */
    @Override
    public TenantDTO updateTenant(TenantDTO tenantDTO) {
        log.info("更新租户: id={}", tenantDTO.getId());
        return tenantRpc.updateTenant(tenantDTO);
    }

    /**
     * 暂停租户
     * 
     * <p>暂停后，租户无法访问系统，但数据保留</p>
     * 
     * @param id 租户ID
     * @return true-暂停成功，false-暂停失败
     */
    @Override
    public Boolean suspendTenant(Long id) {
        log.info("暂停租户: id={}", id);
        return tenantRpc.suspendTenant(id);
    }

    /**
     * 激活租户
     * 
     * <p>激活已暂停的租户，恢复其系统访问权限</p>
     * 
     * @param id 租户ID
     * @return true-激活成功，false-激活失败
     */
    @Override
    public Boolean activateTenant(Long id) {
        log.info("激活租户: id={}", id);
        return tenantRpc.activateTenant(id);
    }

    /**
     * 更新租户套餐
     * 
     * @param id 租户ID
     * @param packageId 套餐ID
     * @return true-更新成功，false-更新失败
     */
    @Override
    public Boolean updatePackage(Long id, Long packageId) {
        log.info("更新租户套餐: id={}, packageId={}", id, packageId);
        return tenantRpc.updatePackage(id, packageId);
    }

    /**
     * 检查租户是否有效
     * 
     * <p>验证租户状态、过期时间等信息</p>
     * 
     * @param id 租户ID
     * @return 租户检查结果DTO，包含是否有效、状态等信息
     */
    @Override
    public TenantCheckDTO checkTenantValid(Long id) {
        log.debug("检查租户有效性: id={}", id);
        return tenantRpc.checkTenantValid(id);
    }
}
