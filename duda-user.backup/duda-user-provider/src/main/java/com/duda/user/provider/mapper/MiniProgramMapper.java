package com.duda.user.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.provider.po.MiniProgramPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 小程序Mapper接口
 *
 * @author Claude
 * @date 2026-03-27
 */
public interface MiniProgramMapper extends BaseMapper<MiniProgramPO> {

    /**
     * 根据租户ID查询小程序列表 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @return 小程序列表
     */
    @Select("SELECT * FROM mini_programs WHERE tenant_id = #{tenantId} ORDER BY id DESC")
    List<MiniProgramPO> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据租户ID和AppID查询小程序 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @param appid 小程序AppID
     * @return 小程序信息
     */
    @Select("SELECT * FROM mini_programs WHERE tenant_id = #{tenantId} AND appid = #{appid}")
    MiniProgramPO selectByTenantAndAppid(@Param("tenantId") Long tenantId,
                                        @Param("appid") String appid);

    /**
     * 根据租户ID和状态查询小程序列表 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @return 小程序列表
     */
    @Select("SELECT * FROM mini_programs WHERE tenant_id = #{tenantId} AND status = #{status} ORDER BY id DESC")
    List<MiniProgramPO> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId,
                                                 @Param("status") String status);

    /**
     * 根据租户ID和公司ID查询小程序列表 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @param companyId 公司ID
     * @return 小程序列表
     */
    @Select("SELECT * FROM mini_programs WHERE tenant_id = #{tenantId} AND company_id = #{companyId} ORDER BY id DESC")
    List<MiniProgramPO> selectByTenantAndCompany(@Param("tenantId") Long tenantId,
                                                @Param("companyId") Long companyId);

    /**
     * 统计租户下的小程序数量 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @return 小程序数量
     */
    @Select("SELECT COUNT(*) FROM mini_programs WHERE tenant_id = #{tenantId}")
    int countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 分页查询小程序列表（带租户隔离）⭐ 新增
     *
     * @param tenantId 租户ID
     * @param status 状态（可选）
     * @param onlineStatus 上线状态（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 小程序列表
     */
    List<MiniProgramPO> selectPageWithTenant(@Param("tenantId") Long tenantId,
                                            @Param("status") String status,
                                            @Param("onlineStatus") String onlineStatus,
                                            @Param("offset") Long offset,
                                            @Param("limit") Integer limit);

    /**
     * 根据开发者公司ID查询小程序列表（带租户隔离）⭐ 新增
     *
     * @param tenantId 租户ID
     * @param developerCompanyId 开发者公司ID
     * @return 小程序列表
     */
    @Select("SELECT * FROM mini_programs WHERE tenant_id = #{tenantId} AND developer_company_id = #{developerCompanyId} ORDER BY id DESC")
    List<MiniProgramPO> selectByTenantAndDeveloper(@Param("tenantId") Long tenantId,
                                                  @Param("developerCompanyId") Long developerCompanyId);
}
