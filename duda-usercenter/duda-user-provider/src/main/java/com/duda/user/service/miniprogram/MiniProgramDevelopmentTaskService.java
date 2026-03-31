package com.duda.user.service.miniprogram;

import com.duda.user.dto.miniprogram.MiniProgramDevelopmentTaskDTO;

import java.util.List;

/**
 * 小程序开发任务服务接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface MiniProgramDevelopmentTaskService {

    /**
     * 根据ID查询开发任务（带缓存）
     *
     * @param id 任务ID
     * @return 开发任务信息
     */
    MiniProgramDevelopmentTaskDTO getTaskById(Long id);

    /**
     * 根据租户ID查询任务列表（带缓存）
     *
     * @param tenantId 租户ID
     * @return 任务列表
     */
    List<MiniProgramDevelopmentTaskDTO> listTasksByTenantId(Long tenantId);

    /**
     * 根据任务编号查询（带缓存）
     *
     * @param taskNo 任务编号
     * @return 开发任务信息
     */
    MiniProgramDevelopmentTaskDTO getTaskByNo(String taskNo);

    /**
     * 根据客户公司查询任务列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param clientCompanyId 客户公司ID
     * @return 任务列表
     */
    List<MiniProgramDevelopmentTaskDTO> listTasksByClient(Long tenantId, Long clientCompanyId);

    /**
     * 根据状态查询任务列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param taskStatus 任务状态
     * @return 任务列表
     */
    List<MiniProgramDevelopmentTaskDTO> listTasksByStatus(Long tenantId, String taskStatus);

    /**
     * 创建开发任务
     *
     * @param taskDTO 任务信息
     * @return 创建的任务信息
     */
    MiniProgramDevelopmentTaskDTO createTask(MiniProgramDevelopmentTaskDTO taskDTO);

    /**
     * 更新开发任务
     *
     * @param taskDTO 任务信息
     */
    void updateTask(MiniProgramDevelopmentTaskDTO taskDTO);

    /**
     * 删除开发任务
     *
     * @param id 任务ID
     */
    void deleteTask(Long id);

    /**
     * 统计租户下的任务数量（带缓存）
     *
     * @param tenantId 租户ID
     * @return 任务数量
     */
    int countTasksByTenantId(Long tenantId);
}
