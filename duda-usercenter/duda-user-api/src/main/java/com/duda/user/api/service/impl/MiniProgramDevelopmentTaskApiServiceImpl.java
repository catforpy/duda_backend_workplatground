package com.duda.user.api.service.impl;

import com.duda.user.api.service.MiniProgramDevelopmentTaskApiService;
import com.duda.user.dto.miniprogram.MiniProgramDevelopmentTaskDTO;
import com.duda.user.rpc.IMiniProgramDevelopmentTaskRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小程序开发任务API服务实现
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Service
public class MiniProgramDevelopmentTaskApiServiceImpl implements MiniProgramDevelopmentTaskApiService {

    private static final Logger log = LoggerFactory.getLogger(MiniProgramDevelopmentTaskApiServiceImpl.class);

    @DubboReference(version = "1.0.0", group = "DUDA_USER_GROUP", timeout = 30000, check = false)
    private IMiniProgramDevelopmentTaskRpc miniProgramDevelopmentTaskRpc;

    @Override
    public MiniProgramDevelopmentTaskDTO getTaskById(Long id) {
        log.info("【API服务】查询开发任务，id={}", id);
        return miniProgramDevelopmentTaskRpc.getTaskById(id);
    }

    @Override
    public List<MiniProgramDevelopmentTaskDTO> listTasksByTenantId(Long tenantId) {
        log.info("【API服务】查询租户任务列表，tenantId={}", tenantId);
        return miniProgramDevelopmentTaskRpc.listTasksByTenantId(tenantId);
    }

    @Override
    public MiniProgramDevelopmentTaskDTO getTaskByNo(String taskNo) {
        log.info("【API服务】根据任务编号查询，taskNo={}", taskNo);
        return miniProgramDevelopmentTaskRpc.getTaskByNo(taskNo);
    }

    @Override
    public List<MiniProgramDevelopmentTaskDTO> listTasksByClient(Long tenantId, Long clientCompanyId) {
        log.info("【API服务】根据客户公司查询任务，tenantId={}, clientCompanyId={}", tenantId, clientCompanyId);
        return miniProgramDevelopmentTaskRpc.listTasksByClient(tenantId, clientCompanyId);
    }

    @Override
    public List<MiniProgramDevelopmentTaskDTO> listTasksByStatus(Long tenantId, String taskStatus) {
        log.info("【API服务】根据状态查询任务列表，tenantId={}, status={}", tenantId, taskStatus);
        return miniProgramDevelopmentTaskRpc.listTasksByStatus(tenantId, taskStatus);
    }

    @Override
    public MiniProgramDevelopmentTaskDTO createTask(MiniProgramDevelopmentTaskDTO taskDTO) {
        log.info("【API服务】创建开发任务，taskNo={}", taskDTO.getTaskNo());
        return miniProgramDevelopmentTaskRpc.createTask(taskDTO);
    }

    @Override
    public void updateTask(MiniProgramDevelopmentTaskDTO taskDTO) {
        log.info("【API服务】更新开发任务，id={}", taskDTO.getId());
        miniProgramDevelopmentTaskRpc.updateTask(taskDTO);
    }

    @Override
    public void deleteTask(Long id) {
        log.info("【API服务】删除开发任务，id={}", id);
        miniProgramDevelopmentTaskRpc.deleteTask(id);
    }

    @Override
    public int countTasksByTenantId(Long tenantId) {
        log.info("【API服务】统计租户任务数量，tenantId={}", tenantId);
        return miniProgramDevelopmentTaskRpc.countTasksByTenantId(tenantId);
    }
}
