package com.duda.user.service.merchant;

import com.duda.user.dto.merchant.OpenApiKeySpecDTO;

import java.util.List;

/**
 * 开放API密钥服务接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface OpenApiKeysService {

    /**
     * 根据ID查询API密钥（带缓存）
     *
     * @param id API密钥ID
     * @return API密钥信息
     */
    OpenApiKeySpecDTO getOpenApiKeysById(Long id);

    /**
     * 根据租户ID查询API密钥列表（带缓存）
     *
     * @param tenantId 租户ID
     * @return API密钥列表
     */
    List<OpenApiKeySpecDTO> listOpenApiKeysByTenantId(Long tenantId);

    /**
     * 根据AppID查询API密钥（带缓存）
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @return API密钥信息
     */
    OpenApiKeySpecDTO getOpenApiKeysByAppId(Long tenantId, String appId);

    /**
     * 根据状态查询API密钥列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @return API密钥列表
     */
    List<OpenApiKeySpecDTO> listOpenApiKeysByStatus(Long tenantId, Integer status);

    /**
     * 根据所有者查询API密钥列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param appOwnerId 应用所有者ID
     * @return API密钥列表
     */
    List<OpenApiKeySpecDTO> listOpenApiKeysByOwner(Long tenantId, Long appOwnerId);

    /**
     * 分页查询API密钥列表
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @param appType 应用类型
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return API密钥列表
     */
    List<OpenApiKeySpecDTO> listOpenApiKeysPage(Long tenantId, Integer status, String appType,
                                                Integer pageNum, Integer pageSize);

    /**
     * 创建API密钥
     *
     * @param openApiKeysDTO API密钥信息
     * @return 创建的API密钥信息
     */
    OpenApiKeySpecDTO createOpenApiKeys(OpenApiKeySpecDTO openApiKeysDTO);

    /**
     * 更新API密钥
     *
     * @param openApiKeysDTO API密钥信息
     */
    void updateOpenApiKeys(OpenApiKeySpecDTO openApiKeysDTO);

    /**
     * 删除API密钥
     *
     * @param id API密钥ID
     */
    void deleteOpenApiKeys(Long id);

    /**
     * 更新API密钥状态
     *
     * @param id API密钥ID
     * @param status 状态
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     */
    void updateOpenApiKeysStatus(Long id, Integer status, String auditStatus, String auditRemark);

    /**
     * 统计租户下的API密钥数量（带缓存）
     *
     * @param tenantId 租户ID
     * @return API密钥数量
     */
    int countOpenApiKeysByTenantId(Long tenantId);
}
