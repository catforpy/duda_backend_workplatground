package com.duda.user.rpc;

import com.duda.user.dto.miniprogram.MiniProgramDevelopmentTaskDTO;
import com.duda.user.service.miniprogram.MiniProgramDevelopmentTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 小程序开发任务RPC实现类
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Slf4j
@DubboService(
    version = "1.0.0",
    group = "DUDA_USER_GROUP",
    timeout = 30000
)
public class MiniProgramDevelopmentTaskRpcImpl implements IMiniProgramDevelopmentTaskRpc {

    @Resource
    private MiniProgramDevelopmentTaskService miniProgramDevelopmentTaskService;

    @Override
    public MiniProgramDevelopmentTaskDTO getTaskById(Long id) {
        log.info("【RPC Provider】获取开发任务，id={}", id);
        return miniProgramDevelopmentTaskService.getTaskById(id);
    }

    @Override
    public List<MiniProgramDevelopmentTaskDTO> listTasksByTenantId(Long tenantId) {
        log.info("【RPC Provider】查询租户任务列表，tenantId={}", tenantId);
        return miniProgramDevelopmentTaskService.listTasksByTenantId(tenantId);
    }

    @Override
    public MiniProgramDevelopmentTaskDTO getTaskByNo(String taskNo) {
        log.info("【RPC Provider】根据任务编号查询，taskNo={}", taskNo);
        return miniProgramDevelopmentTaskService.getTaskByNo(taskNo);
    }

    @Override
    public List<MiniProgramDevelopmentTaskDTO> listTasksByClient(Long tenantId, Long clientCompanyId) {
        log.info("【RPC Provider】根据客户公司查询任务列表，tenantId={}, clientCompanyId={}", tenantId, clientCompanyId);
        return miniProgramDevelopmentTaskService.listTasksByClient(tenantId, clientCompanyId);
    }

    @Override
    public List<MiniProgramDevelopmentTaskDTO> listTasksByStatus(Long tenantId, String taskStatus) {
        log.info("【RPC Provider】根据状态查询任务列表，tenantId={}, taskStatus={}", tenantId, taskStatus);
        return miniProgramDevelopmentTaskService.listTasksByStatus(tenantId, taskStatus);
    }

    @Override
    public MiniProgramDevelopmentTaskDTO createTask(MiniProgramDevelopmentTaskDTO taskDTO) {
        log.info("【RPC Provider】创建开发任务，taskNo={}", taskDTO.getTaskNo());
        return miniProgramDevelopmentTaskService.createTask(taskDTO);
    }

    @Override
    public void updateTask(MiniProgramDevelopmentTaskDTO taskDTO) {
        log.info("【RPC Provider】更新开发任务，id={}", taskDTO.getId());
        miniProgramDevelopmentTaskService.updateTask(taskDTO);
    }

    @Override
    public void deleteTask(Long id) {
        log.info("【RPC Provider】删除开发任务，id={}", id);
        miniProgramDevelopmentTaskService.deleteTask(id);
    }

    @Override
    public int countTasksByTenantId(Long tenantId) {
        log.info("【RPC Provider】统计租户任务数量，tenantId={}", tenantId);
        return miniProgramDevelopmentTaskService.countTasksByTenantId(tenantId);
    }
}
