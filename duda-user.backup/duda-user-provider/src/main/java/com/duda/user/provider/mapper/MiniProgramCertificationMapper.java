package com.duda.user.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.provider.po.MiniProgramCertificationPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 小程序微信认证Mapper接口
 *
 * @author Claude
 * @date 2026-03-27
 */
public interface MiniProgramCertificationMapper extends BaseMapper<MiniProgramCertificationPO> {

    /**
     * 根据租户ID查询认证列表 ⭐ 新增
     */
    @Select("SELECT * FROM mini_program_certification WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY id DESC")
    List<MiniProgramCertificationPO> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据小程序ID查询认证信息（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM mini_program_certification WHERE tenant_id = #{tenantId} AND mini_program_id = #{miniProgramId} AND deleted = 0")
    MiniProgramCertificationPO selectByTenantAndMiniProgram(@Param("tenantId") Long tenantId,
                                                             @Param("miniProgramId") Long miniProgramId);

    /**
     * 根据认证状态查询（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM mini_program_certification WHERE tenant_id = #{tenantId} AND certification_status = #{status} AND deleted = 0 ORDER BY id DESC")
    List<MiniProgramCertificationPO> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId,
                                                               @Param("status") String status);
}
