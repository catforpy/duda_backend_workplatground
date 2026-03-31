package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.user.api.service.MiniProgramApiService;
import com.duda.user.dto.miniprogram.MiniProgramDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序Controller - API层
 *
 * 提供REST API，与前端对接
 * 内部调用Provider的RPC服务（通过MiniProgramApiService）
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Tag(name = "小程序管理", description = "小程序相关接口")
@RestController
@RequestMapping("/miniprogram")
public class MiniProgramController {

    @Resource
    private MiniProgramApiService miniProgramApiService;

    /**
     * 根据ID查询小程序
     */
    @Operation(summary = "查询小程序详情", description = "根据小程序ID查询详细信息")
    @GetMapping("/{id}")
    public Result<MiniProgramDTO> getMiniProgramById(
            @Parameter(description = "小程序ID", required = true)
            @PathVariable("id") Long id) {
        MiniProgramDTO miniProgram = miniProgramApiService.getMiniProgramById(id);
        return Result.success(miniProgram);
    }

    /**
     * 根据租户ID查询小程序列表
     */
    @Operation(summary = "查询租户小程序列表", description = "根据租户ID查询该租户下的所有小程序")
    @GetMapping("/list/tenant/{tenantId}")
    public Result<List<MiniProgramDTO>> listMiniProgramsByTenantId(
            @Parameter(description = "租户ID", required = true)
            @PathVariable("tenantId") Long tenantId) {
        List<MiniProgramDTO> list = miniProgramApiService.listMiniProgramsByTenantId(tenantId);
        return Result.success(list);
    }

    /**
     * 根据AppID查询小程序
     */
    @Operation(summary = "根据AppID查询小程序", description = "根据微信小程序AppID查询小程序信息")
    @GetMapping("/appid")
    public Result<MiniProgramDTO> getMiniProgramByAppId(
            @Parameter(description = "小程序AppID", required = true)
            @RequestParam("appid") String appid) {
        MiniProgramDTO miniProgram = miniProgramApiService.getMiniProgramByAppId(appid);
        return Result.success(miniProgram);
    }

    /**
     * 根据状态查询小程序列表
     */
    @Operation(summary = "根据状态查询小程序", description = "根据状态查询小程序列表")
    @GetMapping("/list/status")
    public Result<List<MiniProgramDTO>> listMiniProgramsByStatus(
            @Parameter(description = "租户ID", required = true)
            @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "状态", required = true)
            @RequestParam("status") String status) {
        List<MiniProgramDTO> list = miniProgramApiService.listMiniProgramsByStatus(tenantId, status);
        return Result.success(list);
    }

    /**
     * 根据上线状态查询小程序列表
     */
    @Operation(summary = "根据上线状态查询小程序", description = "根据上线状态查询小程序列表")
    @GetMapping("/list/online")
    public Result<List<MiniProgramDTO>> listMiniProgramsByOnlineStatus(
            @Parameter(description = "租户ID", required = true)
            @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "上线状态", required = true)
            @RequestParam("onlineStatus") String onlineStatus) {
        List<MiniProgramDTO> list = miniProgramApiService.listMiniProgramsByOnlineStatus(tenantId, onlineStatus);
        return Result.success(list);
    }

    /**
     * 根据公司ID查询小程序列表
     */
    @Operation(summary = "根据公司ID查询小程序", description = "根据开发公司ID查询小程序列表")
    @GetMapping("/list/company")
    public Result<List<MiniProgramDTO>> listMiniProgramsByCompanyId(
            @Parameter(description = "公司ID", required = true)
            @RequestParam("companyId") Long companyId) {
        List<MiniProgramDTO> list = miniProgramApiService.listMiniProgramsByCompanyId(companyId);
        return Result.success(list);
    }

    /**
     * 分页查询小程序列表
     */
    @Operation(summary = "分页查询小程序", description = "分页查询小程序列表")
    @GetMapping("/page")
    public Result<List<MiniProgramDTO>> listMiniProgramsPage(
            @Parameter(description = "租户ID", required = true)
            @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "状态")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "上线状态")
            @RequestParam(value = "onlineStatus", required = false) String onlineStatus,
            @Parameter(description = "页码", required = true)
            @RequestParam("pageNum") Integer pageNum,
            @Parameter(description = "页大小", required = true)
            @RequestParam("pageSize") Integer pageSize) {
        List<MiniProgramDTO> list = miniProgramApiService.listMiniProgramsPage(
                tenantId, status, onlineStatus, pageNum, pageSize);
        return Result.success(list);
    }

    /**
     * 创建小程序
     */
    @Operation(summary = "创建小程序", description = "创建新的小程序")
    @PostMapping("/create")
    public Result<MiniProgramDTO> createMiniProgram(
            @Valid @RequestBody MiniProgramDTO miniProgramDTO) {
        MiniProgramDTO created = miniProgramApiService.createMiniProgram(miniProgramDTO);
        return Result.success(created);
    }

    /**
     * 更新小程序
     */
    @Operation(summary = "更新小程序", description = "更新小程序信息")
    @PutMapping("/update")
    public Result<Void> updateMiniProgram(
            @Valid @RequestBody MiniProgramDTO miniProgramDTO) {
        miniProgramApiService.updateMiniProgram(miniProgramDTO);
        return Result.success();
    }

    /**
     * 删除小程序
     */
    @Operation(summary = "删除小程序", description = "删除小程序（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMiniProgram(
            @Parameter(description = "小程序ID", required = true)
            @PathVariable("id") Long id) {
        miniProgramApiService.deleteMiniProgram(id);
        return Result.success();
    }

    /**
     * 统计租户下的小程序数量
     */
    @Operation(summary = "统计小程序数量", description = "统计租户下的小程序总数")
    @GetMapping("/count/tenant/{tenantId}")
    public Result<Integer> countMiniProgramsByTenantId(
            @Parameter(description = "租户ID", required = true)
            @PathVariable("tenantId") Long tenantId) {
        int count = miniProgramApiService.countMiniProgramsByTenantId(tenantId);
        return Result.success(count);
    }
}
