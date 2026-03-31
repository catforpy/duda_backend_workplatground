package com.duda.user.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.provider.po.MiniProgramFilingPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 小程序备案Mapper接口
 *
 * @author Claude
 * @date 2026-03-27
 */
public interface MiniProgramFilingMapper extends BaseMapper<MiniProgramFilingPO> {

    /**
     * 根据租户ID查询备案列表 ⭐ 新增
     */
    @Select("SELECT * FROM mini_program_filing WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY id DESC")
    List<MiniProgramFilingPO> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据小程序ID查询备案信息（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM mini_program_filing WHERE tenant_id = #{tenantId} AND mini_program_id = #{miniProgramId} AND deleted = 0")
    MiniProgramFilingPO selectByTenantAndMiniProgram(@Param("tenantId") Long tenantId,
                                                     @Param("miniProgramId") Long miniProgramId);

    /**
     * 根据备案状态查询（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM mini_program_filing WHERE tenant_id = #{tenantId} AND filing_status = #{status} AND deleted = 0 ORDER BY id DESC")
    List<MiniProgramFilingPO> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId,
                                                        @Param("status") String status);
}
