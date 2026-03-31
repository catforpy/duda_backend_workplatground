package com.duda.user.api.service;

import com.duda.user.dto.merchant.OpenApiKeySpecDTO;

import java.util.List;

/**
 * 开放API密钥API服务接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface OpenApiKeysApiService {

    OpenApiKeySpecDTO getOpenApiKeysById(Long id);

    List<OpenApiKeySpecDTO> listOpenApiKeysByTenantId(Long tenantId);

    OpenApiKeySpecDTO getOpenApiKeysByAppId(Long tenantId, String appId);

    List<OpenApiKeySpecDTO> listOpenApiKeysByStatus(Long tenantId, Byte status);

    List<OpenApiKeySpecDTO> listOpenApiKeysByOwner(Long tenantId, Long appOwnerId);

    List<OpenApiKeySpecDTO> listOpenApiKeysPage(Long tenantId, Byte status, String appType,
                                                Integer pageNum, Integer pageSize);

    OpenApiKeySpecDTO createOpenApiKeys(OpenApiKeySpecDTO openApiKeysDTO);

    void updateOpenApiKeys(OpenApiKeySpecDTO openApiKeysDTO);

    void deleteOpenApiKeys(Long id);

    void updateOpenApiKeysStatus(Long id, Byte status, String auditStatus, String auditRemark);

    int countOpenApiKeysByTenantId(Long tenantId);
}
