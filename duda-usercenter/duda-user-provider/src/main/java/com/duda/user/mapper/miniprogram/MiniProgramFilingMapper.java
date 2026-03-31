package com.duda.user.mapper.miniprogram;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.miniprogram.MiniProgramFiling;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小程序备案Mapper接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Mapper
public interface MiniProgramFilingMapper extends BaseMapper<MiniProgramFiling> {

    /**
     * 根据租户ID查询备案列表
     */
    List<MiniProgramFiling> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据小程序ID查询备案
     */
    MiniProgramFiling selectByMiniProgramId(@Param("miniProgramId") Long miniProgramId);

    /**
     * 根据租户ID和小程序ID查询备案
     */
    MiniProgramFiling selectByTenantAndMiniProgram(@Param("tenantId") Long tenantId, @Param("miniProgramId") Long miniProgramId);

    /**
     * 根据租户ID和状态查询备案列表
     */
    List<MiniProgramFiling> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("filingStatus") String filingStatus);
}
