package com.duda.user.mapper.merchant;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.merchant.OpenApiKeys;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 开放API密钥Mapper
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Mapper
public interface OpenApiKeysMapper extends BaseMapper<OpenApiKeys> {

    /**
     * 根据租户ID查询API密钥列表
     *
     * @param tenantId 租户ID
     * @return API密钥列表
     */
    List<OpenApiKeys> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据AppID查询API密钥
     *
     * @param appId 应用ID
     * @return API密钥
     */
    OpenApiKeys selectByAppId(@Param("appId") String appId);

    /**
     * 根据租户ID和AppID查询API密钥
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @return API密钥
     */
    OpenApiKeys selectByTenantIdAndAppId(@Param("tenantId") Long tenantId, @Param("appId") String appId);

    /**
     * 根据状态查询API密钥列表
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @return API密钥列表
     */
    List<OpenApiKeys> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") Integer status);

    /**
     * 根据所有者查询API密钥列表
     *
     * @param tenantId 租户ID
     * @param appOwnerId 应用所有者ID
     * @return API密钥列表
     */
    List<OpenApiKeys> selectByTenantIdAndOwner(@Param("tenantId") Long tenantId, @Param("appOwnerId") Long appOwnerId);

    /**
     * 统计租户下的API密钥数量
     *
     * @param tenantId 租户ID
     * @return API密钥数量
     */
    Integer countByTenantId(@Param("tenantId") Long tenantId);
}
