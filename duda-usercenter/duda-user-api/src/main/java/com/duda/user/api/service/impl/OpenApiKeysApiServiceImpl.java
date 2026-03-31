package com.duda.user.api.service.impl;

import com.duda.user.api.service.OpenApiKeysApiService;
import com.duda.user.dto.merchant.OpenApiKeySpecDTO;
import com.duda.user.rpc.IOpenApiKeysRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 开放API密钥API服务实现
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Service
public class OpenApiKeysApiServiceImpl implements OpenApiKeysApiService {

    private static final Logger log = LoggerFactory.getLogger(OpenApiKeysApiServiceImpl.class);

    @DubboReference(version = "1.0.0", group = "DUDA_USER_GROUP", timeout = 30000, check = false)
    private IOpenApiKeysRpc openApiKeysRpc;

    @Override
    public OpenApiKeySpecDTO getOpenApiKeysById(Long id) {
        log.info("【API服务】查询API密钥，id={}", id);
        return openApiKeysRpc.getOpenApiKeysById(id);
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysByTenantId(Long tenantId) {
        log.info("【API服务】查询租户API密钥列表，tenantId={}", tenantId);
        return openApiKeysRpc.listOpenApiKeysByTenantId(tenantId);
    }

    @Override
    public OpenApiKeySpecDTO getOpenApiKeysByAppId(Long tenantId, String appId) {
        log.info("【API服务】根据AppID查询API密钥，tenantId={}, appId={}", tenantId, appId);
        return openApiKeysRpc.getOpenApiKeysByAppId(tenantId, appId);
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysByStatus(Long tenantId, Byte status) {
        log.info("【API服务】根据状态查询API密钥列表，tenantId={}, status={}", tenantId, status);
        return openApiKeysRpc.listOpenApiKeysByStatus(tenantId, status);
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysByOwner(Long tenantId, Long appOwnerId) {
        log.info("【API服务】根据所有者查询API密钥列表，tenantId={}, appOwnerId={}", tenantId, appOwnerId);
        return openApiKeysRpc.listOpenApiKeysByOwner(tenantId, appOwnerId);
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysPage(Long tenantId, Byte status, String appType,
                                                       Integer pageNum, Integer pageSize) {
        log.info("【API服务】分页查询API密钥列表，tenantId={}, status={}, appType={}, pageNum={}, pageSize={}",
                tenantId, status, appType, pageNum, pageSize);
        return openApiKeysRpc.listOpenApiKeysPage(tenantId, status, appType, pageNum, pageSize);
    }

    @Override
    public OpenApiKeySpecDTO createOpenApiKeys(OpenApiKeySpecDTO openApiKeysDTO) {
        log.info("【API服务】创建API密钥，appId={}", openApiKeysDTO.getAppId());
        return openApiKeysRpc.createOpenApiKeys(openApiKeysDTO);
    }

    @Override
    public void updateOpenApiKeys(OpenApiKeySpecDTO openApiKeysDTO) {
        log.info("【API服务】更新API密钥，id={}", openApiKeysDTO.getId());
        openApiKeysRpc.updateOpenApiKeys(openApiKeysDTO);
    }

    @Override
    public void deleteOpenApiKeys(Long id) {
        log.info("【API服务】删除API密钥，id={}", id);
        openApiKeysRpc.deleteOpenApiKeys(id);
    }

    @Override
    public void updateOpenApiKeysStatus(Long id, Byte status, String auditStatus, String auditRemark) {
        log.info("【API服务】更新API密钥状态，id={}, status={}, auditStatus={}", id, status, auditStatus);
        openApiKeysRpc.updateOpenApiKeysStatus(id, status, auditStatus, auditRemark);
    }

    @Override
    public int countOpenApiKeysByTenantId(Long tenantId) {
        log.info("【API服务】统计租户API密钥数量，tenantId={}", tenantId);
        return openApiKeysRpc.countOpenApiKeysByTenantId(tenantId);
    }
}
