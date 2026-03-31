package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.tenant.enums.TenantStatusEnum;
import com.duda.tenant.entity.Tenant;
import com.duda.tenant.entity.TenantPackage;
import com.duda.tenant.manager.TenantSchemaManager;
import com.duda.tenant.mapper.TenantMapper;
import com.duda.tenant.mq.TenantMessageProducer;
import com.duda.tenant.service.TenantPackageService;
import com.duda.tenant.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 租户服务实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {

    @Autowired
    private TenantPackageService tenantPackageService;

    @Autowired
    private TenantSchemaManager tenantSchemaManager;

    @Autowired
    private TenantMessageProducer tenantMessageProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tenant createTenant(Tenant tenant) {
        // 1. 保存租户基本信息
        tenant.setCreateTime(LocalDateTime.now());
        tenant.setUpdateTime(LocalDateTime.now());
        tenant.setTenantStatus(TenantStatusEnum.ACTIVE.getCode());
        tenant.setVersion(1);
        tenant.setDeleted(0);
        save(tenant);

        log.info("创建租户基本信息成功: tenantId={}, tenantCode={}", tenant.getId(), tenant.getTenantCode());

        // 2. 为租户创建独立Schema（物理隔离）
        try {
            boolean schemaCreated = tenantSchemaManager.createTenantSchema(
                tenant.getId(),
                tenant.getTenantCode()
            );

            if (schemaCreated) {
                log.info("创建租户Schema成功: tenantId={}, tenantCode={}",
                    tenant.getId(), tenant.getTenantCode());
            } else {
                log.error("创建租户Schema失败: tenantId={}, tenantCode={}",
                    tenant.getId(), tenant.getTenantCode());
                throw new RuntimeException("创建租户Schema失败");
            }
        } catch (Exception e) {
            log.error("创建租户Schema异常: tenantId={}, tenantCode={}",
                tenant.getId(), tenant.getTenantCode(), e);
            // Schema创建失败，回滚租户创建
            throw new RuntimeException("创建租户Schema失败: " + e.getMessage(), e);
        }

        log.info("创建租户完全成功: tenantId={}, tenantCode={}, schema=tenant_{}",
            tenant.getId(), tenant.getTenantCode(), tenant.getTenantCode());

        // 3. 发送租户创建消息到MQ（异步处理后续操作）
        tenantMessageProducer.sendTenantCreateMessage(tenant);

        return tenant;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tenant updateTenant(Tenant tenant) {
        tenant.setUpdateTime(LocalDateTime.now());
        updateById(tenant);
        log.info("更新租户成功: tenantId={}", tenant.getId());
        return tenant;
    }

    @Override
    public Tenant getByTenantCode(String tenantCode) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getTenantCode, tenantCode);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean suspendTenant(Long tenantId) {
        Tenant tenant = getById(tenantId);
        if (tenant == null) {
            log.warn("暂停租户失败，租户不存在: tenantId={}", tenantId);
            return false;
        }
        tenant.setTenantStatus(TenantStatusEnum.SUSPENDED.getCode());
        tenant.setUpdateTime(LocalDateTime.now());
        updateById(tenant);
        log.info("暂停租户成功: tenantId={}", tenantId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean activateTenant(Long tenantId) {
        Tenant tenant = getById(tenantId);
        if (tenant == null) {
            log.warn("激活租户失败，租户不存在: tenantId={}", tenantId);
            return false;
        }
        tenant.setTenantStatus(TenantStatusEnum.ACTIVE.getCode());
        tenant.setUpdateTime(LocalDateTime.now());
        updateById(tenant);
        log.info("激活租户成功: tenantId={}", tenantId);
        return true;
    }

    @Override
    public Boolean isValidTenant(Long tenantId) {
        Tenant tenant = getById(tenantId);
        if (tenant == null) {
            return false;
        }
        if (!TenantStatusEnum.ACTIVE.getCode().equals(tenant.getTenantStatus())) {
            return false;
        }
        if (tenant.getExpireTime() != null && tenant.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePackage(Long tenantId, Long packageId) {
        Tenant tenant = getById(tenantId);
        if (tenant == null) {
            log.warn("更新租户套餐失败，租户不存在: tenantId={}", tenantId);
            return false;
        }

        TenantPackage tenantPackage = tenantPackageService.getById(packageId);
        if (tenantPackage == null) {
            log.warn("更新租户套餐失败，套餐不存在: packageId={}", packageId);
            return false;
        }

        tenant.setPackageId(packageId);
        tenant.setPackageExpireTime(LocalDateTime.now().plusMonths(1));
        tenant.setMaxUsers(tenantPackage.getMaxUsers());
        tenant.setMaxStorageSize(tenantPackage.getMaxStorageSize());
        tenant.setMaxApiCallsPerDay(tenantPackage.getMaxApiCallsPerDay());
        tenant.setUpdateTime(LocalDateTime.now());
        updateById(tenant);

        log.info("更新租户套餐成功: tenantId={}, packageId={}", tenantId, packageId);
        return true;
    }

    @Override
    public Boolean checkQuota(Long tenantId, String quotaType) {
        Tenant tenant = getById(tenantId);
        if (tenant == null) {
            return false;
        }

        switch (quotaType) {
            case "user_count":
                // TODO: 实现用户数量检查
                return true;
            case "storage_size":
                // TODO: 实现存储空间检查
                return true;
            case "api_calls":
                // TODO: 实现API调用次数检查
                return true;
            default:
                log.warn("未知的配额类型: quotaType={}", quotaType);
                return false;
        }
    }
}
