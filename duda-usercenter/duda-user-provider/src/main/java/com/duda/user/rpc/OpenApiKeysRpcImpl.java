package com.duda.user.rpc;

import com.duda.user.dto.merchant.OpenApiKeySpecDTO;
import com.duda.user.service.merchant.OpenApiKeysService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * API密钥RPC实现类
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Slf4j
@DubboService(
    version = "1.0.0",
    group = "DUDA_USER_GROUP",
    timeout = 30000
)
public class OpenApiKeysRpcImpl implements IOpenApiKeysRpc {

    @Resource
    private OpenApiKeysService openApiKeysService;

    @Override
    public OpenApiKeySpecDTO getOpenApiKeysById(Long id) {
        log.info("【RPC Provider】获取API密钥，id={}", id);
        return openApiKeysService.getOpenApiKeysById(id);
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysByTenantId(Long tenantId) {
        log.info("【RPC Provider】查询租户API密钥列表，tenantId={}", tenantId);
        return openApiKeysService.listOpenApiKeysByTenantId(tenantId);
    }

    @Override
    public OpenApiKeySpecDTO getOpenApiKeysByAppId(Long tenantId, String appId) {
        log.info("【RPC Provider】根据AppID查询API密钥，tenantId={}, appId={}", tenantId, appId);
        return openApiKeysService.getOpenApiKeysByAppId(tenantId, appId);
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysByStatus(Long tenantId, Byte status) {
        log.info("【RPC Provider】根据状态查询API密钥列表，tenantId={}, status={}", tenantId, status);
        return openApiKeysService.listOpenApiKeysByStatus(tenantId, status != null ? status.intValue() : null);
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysByOwner(Long tenantId, Long appOwnerId) {
        log.info("【RPC Provider】根据所有者查询API密钥列表，tenantId={}, appOwnerId={}", tenantId, appOwnerId);
        return openApiKeysService.listOpenApiKeysByOwner(tenantId, appOwnerId);
    }

    @Override
    public List<OpenApiKeySpecDTO> listOpenApiKeysPage(Long tenantId, Byte status, String appType,
                                                        Integer pageNum, Integer pageSize) {
        log.info("【RPC Provider】分页查询API密钥列表，tenantId={}, status={}, appType={}, pageNum={}, pageSize={}",
                tenantId, status, appType, pageNum, pageSize);
        return openApiKeysService.listOpenApiKeysPage(tenantId, status != null ? status.intValue() : null, appType, pageNum, pageSize);
    }

    @Override
    public OpenApiKeySpecDTO createOpenApiKeys(OpenApiKeySpecDTO openApiKeysDTO) {
        log.info("【RPC Provider】创建API密钥，appId={}", openApiKeysDTO.getAppId());
        return openApiKeysService.createOpenApiKeys(openApiKeysDTO);
    }

    @Override
    public void updateOpenApiKeys(OpenApiKeySpecDTO openApiKeysDTO) {
        log.info("【RPC Provider】更新API密钥，id={}", openApiKeysDTO.getId());
        openApiKeysService.updateOpenApiKeys(openApiKeysDTO);
    }

    @Override
    public void deleteOpenApiKeys(Long id) {
        log.info("【RPC Provider】删除API密钥，id={}", id);
        openApiKeysService.deleteOpenApiKeys(id);
    }

    @Override
    public void updateOpenApiKeysStatus(Long id, Byte status, String auditStatus, String auditRemark) {
        log.info("【RPC Provider】更新API密钥状态，id={}, status={}, auditStatus={}", id, status, auditStatus);
        openApiKeysService.updateOpenApiKeysStatus(id, status != null ? status.intValue() : null, auditStatus, auditRemark);
    }

    @Override
    public int countOpenApiKeysByTenantId(Long tenantId) {
        log.info("【RPC Provider】统计API密钥数量，tenantId={}", tenantId);
        return openApiKeysService.countOpenApiKeysByTenantId(tenantId);
    }
}
