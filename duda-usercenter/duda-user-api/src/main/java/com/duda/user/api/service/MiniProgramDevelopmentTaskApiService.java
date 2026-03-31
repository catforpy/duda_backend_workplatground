package com.duda.user.api.service;

import com.duda.user.dto.miniprogram.MiniProgramDevelopmentTaskDTO;

import java.util.List;

/**
 * 小程序开发任务API服务接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface MiniProgramDevelopmentTaskApiService {

    MiniProgramDevelopmentTaskDTO getTaskById(Long id);

    List<MiniProgramDevelopmentTaskDTO> listTasksByTenantId(Long tenantId);

    MiniProgramDevelopmentTaskDTO getTaskByNo(String taskNo);

    List<MiniProgramDevelopmentTaskDTO> listTasksByClient(Long tenantId, Long clientCompanyId);

    List<MiniProgramDevelopmentTaskDTO> listTasksByStatus(Long tenantId, String taskStatus);

    MiniProgramDevelopmentTaskDTO createTask(MiniProgramDevelopmentTaskDTO taskDTO);

    void updateTask(MiniProgramDevelopmentTaskDTO taskDTO);

    void deleteTask(Long id);

    int countTasksByTenantId(Long tenantId);
}
