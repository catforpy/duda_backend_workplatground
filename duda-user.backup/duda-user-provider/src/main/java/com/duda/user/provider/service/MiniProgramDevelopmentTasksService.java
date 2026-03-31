package com.duda.user.provider.service;

import com.duda.user.dto.MiniProgramDevelopmentTasksDTO;

import java.util.List;

/**
 * 小程序代开发任务Service接口
 */
public interface MiniProgramDevelopmentTasksService {

    MiniProgramDevelopmentTasksDTO getTaskById(Long id);

    List<MiniProgramDevelopmentTasksDTO> listTasksByTenantId(Long tenantId);

    MiniProgramDevelopmentTasksDTO getTaskByNo(Long tenantId, String taskNo);

    List<MiniProgramDevelopmentTasksDTO> listTasksByClient(Long tenantId, Long clientCompanyId);

    List<MiniProgramDevelopmentTasksDTO> listTasksByStatus(Long tenantId, String status);

    MiniProgramDevelopmentTasksDTO createTask(MiniProgramDevelopmentTasksDTO dto);

    void updateTask(MiniProgramDevelopmentTasksDTO dto);

    void deleteTask(Long id);
}
