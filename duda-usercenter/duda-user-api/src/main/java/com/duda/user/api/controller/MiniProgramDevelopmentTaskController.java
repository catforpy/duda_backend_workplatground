package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.user.api.service.MiniProgramDevelopmentTaskApiService;
import com.duda.user.dto.miniprogram.MiniProgramDevelopmentTaskDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序开发任务Controller - API层
 *
 * 提供REST API，管理小程序开发任务
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Tag(name = "小程序开发任务管理", description = "小程序开发任务相关接口")
@RestController
@RequestMapping("/miniprogram/development-task")
public class MiniProgramDevelopmentTaskController {

    @Resource
    private MiniProgramDevelopmentTaskApiService miniProgramDevelopmentTaskApiService;

    @Operation(summary = "查询开发任务详情", description = "根据ID查询开发任务详细信息")
    @GetMapping("/{id}")
    public Result<MiniProgramDevelopmentTaskDTO> getTaskById(
            @Parameter(description = "任务ID", required = true) @PathVariable("id") Long id) {
        MiniProgramDevelopmentTaskDTO task = miniProgramDevelopmentTaskApiService.getTaskById(id);
        return Result.success(task);
    }

    @Operation(summary = "查询租户任务列表", description = "根据租户ID查询所有开发任务")
    @GetMapping("/list/tenant/{tenantId}")
    public Result<List<MiniProgramDevelopmentTaskDTO>> listTasksByTenantId(
            @Parameter(description = "租户ID", required = true) @PathVariable("tenantId") Long tenantId) {
        List<MiniProgramDevelopmentTaskDTO> list = miniProgramDevelopmentTaskApiService.listTasksByTenantId(tenantId);
        return Result.success(list);
    }

    @Operation(summary = "根据任务编号查询", description = "根据任务编号查询开发任务")
    @GetMapping("/no/{taskNo}")
    public Result<MiniProgramDevelopmentTaskDTO> getTaskByNo(
            @Parameter(description = "任务编号", required = true) @PathVariable("taskNo") String taskNo) {
        MiniProgramDevelopmentTaskDTO task = miniProgramDevelopmentTaskApiService.getTaskByNo(taskNo);
        return Result.success(task);
    }

    @Operation(summary = "根据客户公司查询任务", description = "查询指定客户的开发任务列表")
    @GetMapping("/list/client")
    public Result<List<MiniProgramDevelopmentTaskDTO>> listTasksByClient(
            @Parameter(description = "租户ID", required = true) @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "客户公司ID", required = true) @RequestParam("clientCompanyId") Long clientCompanyId) {
        List<MiniProgramDevelopmentTaskDTO> list = miniProgramDevelopmentTaskApiService.listTasksByClient(
                tenantId, clientCompanyId);
        return Result.success(list);
    }

    @Operation(summary = "根据状态查询任务列表", description = "根据任务状态查询开发任务")
    @GetMapping("/list/status")
    public Result<List<MiniProgramDevelopmentTaskDTO>> listTasksByStatus(
            @Parameter(description = "租户ID", required = true) @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "任务状态", required = true) @RequestParam("taskStatus") String taskStatus) {
        List<MiniProgramDevelopmentTaskDTO> list = miniProgramDevelopmentTaskApiService.listTasksByStatus(
                tenantId, taskStatus);
        return Result.success(list);
    }

    @Operation(summary = "创建开发任务", description = "创建新的小程序开发任务")
    @PostMapping("/create")
    public Result<MiniProgramDevelopmentTaskDTO> createTask(
            @Valid @RequestBody MiniProgramDevelopmentTaskDTO taskDTO) {
        MiniProgramDevelopmentTaskDTO created = miniProgramDevelopmentTaskApiService.createTask(taskDTO);
        return Result.success(created);
    }

    @Operation(summary = "更新开发任务", description = "更新开发任务信息")
    @PutMapping("/update")
    public Result<Void> updateTask(
            @Valid @RequestBody MiniProgramDevelopmentTaskDTO taskDTO) {
        miniProgramDevelopmentTaskApiService.updateTask(taskDTO);
        return Result.success();
    }

    @Operation(summary = "删除开发任务", description = "删除开发任务记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(
            @Parameter(description = "任务ID", required = true) @PathVariable("id") Long id) {
        miniProgramDevelopmentTaskApiService.deleteTask(id);
        return Result.success();
    }

    @Operation(summary = "统计任务数量", description = "统计租户下的任务总数")
    @GetMapping("/count/tenant/{tenantId}")
    public Result<Integer> countTasksByTenantId(
            @Parameter(description = "租户ID", required = true) @PathVariable("tenantId") Long tenantId) {
        int count = miniProgramDevelopmentTaskApiService.countTasksByTenantId(tenantId);
        return Result.success(count);
    }
}
