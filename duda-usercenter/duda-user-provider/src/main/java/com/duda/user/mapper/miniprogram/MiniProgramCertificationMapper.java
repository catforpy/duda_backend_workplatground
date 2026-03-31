package com.duda.user.mapper.miniprogram;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.miniprogram.MiniProgramCertification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小程序认证Mapper接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Mapper
public interface MiniProgramCertificationMapper extends BaseMapper<MiniProgramCertification> {

    /**
     * 根据租户ID查询认证列表
     */
    List<MiniProgramCertification> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据小程序ID查询认证
     */
    MiniProgramCertification selectByMiniProgramId(@Param("miniProgramId") Long miniProgramId);

    /**
     * 根据租户ID和小程序ID查询认证
     */
    MiniProgramCertification selectByTenantAndMiniProgram(@Param("tenantId") Long tenantId, @Param("miniProgramId") Long miniProgramId);

    /**
     * 根据租户ID和状态查询认证列表
     */
    List<MiniProgramCertification> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("certificationStatus") String certificationStatus);
}
