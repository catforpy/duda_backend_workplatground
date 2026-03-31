package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.user.api.service.MiniProgramCertificationApiService;
import com.duda.user.dto.miniprogram.MiniProgramCertificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序认证Controller - API层
 *
 * 提供REST API，管理小程序认证信息
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Tag(name = "小程序认证管理", description = "小程序认证相关接口")
@RestController
@RequestMapping("/miniprogram/certification")
public class MiniProgramCertificationController {

    @Resource
    private MiniProgramCertificationApiService miniProgramCertificationApiService;

    @Operation(summary = "查询认证详情", description = "根据ID查询小程序认证详细信息")
    @GetMapping("/{id}")
    public Result<MiniProgramCertificationDTO> getCertificationById(
            @Parameter(description = "认证ID", required = true) @PathVariable("id") Long id) {
        MiniProgramCertificationDTO certification = miniProgramCertificationApiService.getCertificationById(id);
        return Result.success(certification);
    }

    @Operation(summary = "查询租户认证列表", description = "根据租户ID查询所有小程序认证")
    @GetMapping("/list/tenant/{tenantId}")
    public Result<List<MiniProgramCertificationDTO>> listCertificationsByTenantId(
            @Parameter(description = "租户ID", required = true) @PathVariable("tenantId") Long tenantId) {
        List<MiniProgramCertificationDTO> list = miniProgramCertificationApiService.listCertificationsByTenantId(tenantId);
        return Result.success(list);
    }

    @Operation(summary = "根据小程序ID查询认证", description = "查询指定小程序的认证信息")
    @GetMapping("/miniprogram/{miniProgramId}")
    public Result<MiniProgramCertificationDTO> getCertificationByMiniProgramId(
            @Parameter(description = "小程序ID", required = true) @PathVariable("miniProgramId") Long miniProgramId) {
        MiniProgramCertificationDTO certification = miniProgramCertificationApiService.getCertificationByMiniProgramId(miniProgramId);
        return Result.success(certification);
    }

    @Operation(summary = "根据状态查询认证列表", description = "根据认证状态查询小程序列表")
    @GetMapping("/list/status")
    public Result<List<MiniProgramCertificationDTO>> listCertificationsByStatus(
            @Parameter(description = "租户ID", required = true) @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "认证状态", required = true) @RequestParam("certificationStatus") String certificationStatus) {
        List<MiniProgramCertificationDTO> list = miniProgramCertificationApiService.listCertificationsByStatus(
                tenantId, certificationStatus);
        return Result.success(list);
    }

    @Operation(summary = "创建认证信息", description = "创建新的小程序认证记录")
    @PostMapping("/create")
    public Result<MiniProgramCertificationDTO> createCertification(
            @Valid @RequestBody MiniProgramCertificationDTO certificationDTO) {
        MiniProgramCertificationDTO created = miniProgramCertificationApiService.createCertification(certificationDTO);
        return Result.success(created);
    }

    @Operation(summary = "更新认证信息", description = "更新小程序认证信息")
    @PutMapping("/update")
    public Result<Void> updateCertification(
            @Valid @RequestBody MiniProgramCertificationDTO certificationDTO) {
        miniProgramCertificationApiService.updateCertification(certificationDTO);
        return Result.success();
    }

    @Operation(summary = "删除认证信息", description = "删除小程序认证记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCertification(
            @Parameter(description = "认证ID", required = true) @PathVariable("id") Long id) {
        miniProgramCertificationApiService.deleteCertification(id);
        return Result.success();
    }
}
