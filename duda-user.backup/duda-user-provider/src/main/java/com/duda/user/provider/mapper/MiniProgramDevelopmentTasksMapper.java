package com.duda.user.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.provider.po.MiniProgramDevelopmentTasksPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 小程序代开发任务Mapper接口
 *
 * @author Claude
 * @date 2026-03-27
 */
public interface MiniProgramDevelopmentTasksMapper extends BaseMapper<MiniProgramDevelopmentTasksPO> {

    /**
     * 根据租户ID查询任务列表 ⭐ 新增
     */
    @Select("SELECT * FROM mini_program_development_tasks WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY id DESC")
    List<MiniProgramDevelopmentTasksPO> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据任务编号查询（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM mini_program_development_tasks WHERE tenant_id = #{tenantId} AND task_no = #{taskNo} AND deleted = 0")
    MiniProgramDevelopmentTasksPO selectByTenantAndTaskNo(@Param("tenantId") Long tenantId,
                                                         @Param("taskNo") String taskNo);

    /**
     * 根据客户公司ID查询任务列表（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM mini_program_development_tasks WHERE tenant_id = #{tenantId} AND client_company_id = #{clientCompanyId} AND deleted = 0 ORDER BY id DESC")
    List<MiniProgramDevelopmentTasksPO> selectByTenantAndClient(@Param("tenantId") Long tenantId,
                                                               @Param("clientCompanyId") Long clientCompanyId);

    /**
     * 根据任务状态查询（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM mini_program_development_tasks WHERE tenant_id = #{tenantId} AND task_status = #{status} AND deleted = 0 ORDER BY id DESC")
    List<MiniProgramDevelopmentTasksPO> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId,
                                                                 @Param("status") String status);
}
