package com.duda.user.provider.service;

import com.duda.user.dto.OpenApiKeysDTO;

import java.util.List;

/**
 * 开放API密钥Service接口
 */
public interface OpenApiKeysService {

    OpenApiKeysDTO getOpenApiKeysById(Long id);

    List<OpenApiKeysDTO> listOpenApiKeysByTenantId(Long tenantId);

    OpenApiKeysDTO getOpenApiKeysByAppId(Long tenantId, String appId);

    List<OpenApiKeysDTO> listOpenApiKeysByTenantIdAndStatus(Long tenantId, Byte status);

    List<OpenApiKeysDTO> listOpenApiKeysByOwner(Long tenantId, Long appOwnerId);

    List<OpenApiKeysDTO> listOpenApiKeysPage(Long tenantId, Byte status,
                                               String appType, Integer pageNum, Integer pageSize);

    OpenApiKeysDTO createOpenApiKeys(OpenApiKeysDTO dto);

    void updateOpenApiKeys(OpenApiKeysDTO dto);

    void deleteOpenApiKeys(Long id);

    int countOpenApiKeysByTenantId(Long tenantId);

    void updateOpenApiKeysStatus(Long id, Byte status, String auditStatus, String auditRemark);
}
