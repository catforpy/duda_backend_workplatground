package com.duda.user.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.provider.po.OpenApiKeysPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 开放API密钥Mapper接口
 *
 * @author Claude
 * @date 2026-03-27
 */
public interface OpenApiKeysMapper extends BaseMapper<OpenApiKeysPO> {

    /**
     * 根据租户ID查询API密钥列表 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @return API密钥列表
     */
    @Select("SELECT * FROM open_api_keys WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY id DESC")
    List<OpenApiKeysPO> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据租户ID和AppID查询API密钥 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @return API密钥信息
     */
    @Select("SELECT * FROM open_api_keys WHERE tenant_id = #{tenantId} AND app_id = #{appId} AND deleted = 0")
    OpenApiKeysPO selectByTenantAndAppId(@Param("tenantId") Long tenantId,
                                        @Param("appId") String appId);

    /**
     * 根据租户ID和状态查询API密钥列表 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @return API密钥列表
     */
    @Select("SELECT * FROM open_api_keys WHERE tenant_id = #{tenantId} AND status = #{status} AND deleted = 0 ORDER BY id DESC")
    List<OpenApiKeysPO> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId,
                                                 @Param("status") Byte status);

    /**
     * 统计租户下的API密钥数量 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @return API密钥数量
     */
    @Select("SELECT COUNT(*) FROM open_api_keys WHERE tenant_id = #{tenantId} AND deleted = 0")
    int countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据所有者查询API密钥列表（带租户隔离）⭐ 新增
     *
     * @param tenantId 租户ID
     * @param appOwnerId 应用所有者ID
     * @return API密钥列表
     */
    @Select("SELECT * FROM open_api_keys WHERE tenant_id = #{tenantId} AND app_owner_id = #{appOwnerId} AND deleted = 0 ORDER BY id DESC")
    List<OpenApiKeysPO> selectByTenantAndOwner(@Param("tenantId") Long tenantId,
                                              @Param("appOwnerId") Long appOwnerId);

    /**
     * 分页查询API密钥列表（带租户隔离）⭐ 新增
     *
     * @param tenantId 租户ID
     * @param status 状态（可选）
     * @param appType 应用类型（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return API密钥列表
     */
    List<OpenApiKeysPO> selectPageWithTenant(@Param("tenantId") Long tenantId,
                                            @Param("status") Byte status,
                                            @Param("appType") String appType,
                                            @Param("offset") Long offset,
                                            @Param("limit") Integer limit);

    /**
     * 更新API密钥状态（带乐观锁）⭐ 新增
     *
     * @param apiKey API密钥信息
     * @return 影响行数
     */
    int updateStatusWithVersion(OpenApiKeysPO apiKey);
}
