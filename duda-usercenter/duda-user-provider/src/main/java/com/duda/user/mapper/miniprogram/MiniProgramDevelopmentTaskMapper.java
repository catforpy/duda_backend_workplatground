package com.duda.user.mapper.miniprogram;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.miniprogram.MiniProgramDevelopmentTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小程序代开发任务Mapper接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Mapper
public interface MiniProgramDevelopmentTaskMapper extends BaseMapper<MiniProgramDevelopmentTask> {

    /**
     * 根据租户ID查询任务列表
     */
    List<MiniProgramDevelopmentTask> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据任务编号查询
     */
    MiniProgramDevelopmentTask selectByTaskNo(@Param("taskNo") String taskNo);

    /**
     * 根据租户ID和任务编号查询
     */
    MiniProgramDevelopmentTask selectByTenantAndTaskNo(@Param("tenantId") Long tenantId, @Param("taskNo") String taskNo);

    /**
     * 根据客户公司ID查询任务列表
     */
    List<MiniProgramDevelopmentTask> selectByClientCompany(@Param("tenantId") Long tenantId, @Param("clientCompanyId") Long clientCompanyId);

    /**
     * 根据租户ID和状态查询任务列表
     */
    List<MiniProgramDevelopmentTask> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("taskStatus") String taskStatus);

    /**
     * 统计租户下的任务数量
     */
    Integer countByTenantId(@Param("tenantId") Long tenantId);
}
