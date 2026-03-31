package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.user.api.service.OpenApiKeysApiService;
import com.duda.user.dto.merchant.OpenApiKeySpecDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 开放API密钥Controller - API层
 *
 * 提供REST API，管理开放API密钥
 * 用于PHP后端等第三方系统调用
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Tag(name = "开放API密钥管理", description = "开放API密钥相关接口")
@RestController
@RequestMapping("/openapi/keys")
public class OpenApiKeysController {

    @Resource
    private OpenApiKeysApiService openApiKeysApiService;

    @Operation(summary = "查询API密钥详情", description = "根据ID查询API密钥详细信息")
    @GetMapping("/{id}")
    public Result<OpenApiKeySpecDTO> getOpenApiKeysById(
            @Parameter(description = "API密钥ID", required = true) @PathVariable("id") Long id) {
        OpenApiKeySpecDTO openApiKeys = openApiKeysApiService.getOpenApiKeysById(id);
        return Result.success(openApiKeys);
    }

    @Operation(summary = "查询租户API密钥列表", description = "根据租户ID查询所有API密钥")
    @GetMapping("/list/tenant/{tenantId}")
    public Result<List<OpenApiKeySpecDTO>> listOpenApiKeysByTenantId(
            @Parameter(description = "租户ID", required = true) @PathVariable("tenantId") Long tenantId) {
        List<OpenApiKeySpecDTO> list = openApiKeysApiService.listOpenApiKeysByTenantId(tenantId);
        return Result.success(list);
    }

    @Operation(summary = "根据AppID查询密钥", description = "根据应用ID查询API密钥")
    @GetMapping("/appid")
    public Result<OpenApiKeySpecDTO> getOpenApiKeysByAppId(
            @Parameter(description = "租户ID", required = true) @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "应用ID", required = true) @RequestParam("appId") String appId) {
        OpenApiKeySpecDTO openApiKeys = openApiKeysApiService.getOpenApiKeysByAppId(tenantId, appId);
        return Result.success(openApiKeys);
    }

    @Operation(summary = "根据状态查询密钥列表", description = "根据状态查询API密钥列表")
    @GetMapping("/list/status")
    public Result<List<OpenApiKeySpecDTO>> listOpenApiKeysByStatus(
            @Parameter(description = "租户ID", required = true) @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "状态", required = true) @RequestParam("status") Byte status) {
        List<OpenApiKeySpecDTO> list = openApiKeysApiService.listOpenApiKeysByStatus(tenantId, status);
        return Result.success(list);
    }

    @Operation(summary = "根据所有者查询密钥列表", description = "根据应用所有者查询API密钥列表")
    @GetMapping("/list/owner")
    public Result<List<OpenApiKeySpecDTO>> listOpenApiKeysByOwner(
            @Parameter(description = "租户ID", required = true) @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "应用所有者ID", required = true) @RequestParam("appOwnerId") Long appOwnerId) {
        List<OpenApiKeySpecDTO> list = openApiKeysApiService.listOpenApiKeysByOwner(tenantId, appOwnerId);
        return Result.success(list);
    }

    @Operation(summary = "分页查询API密钥", description = "分页查询API密钥列表")
    @GetMapping("/page")
    public Result<List<OpenApiKeySpecDTO>> listOpenApiKeysPage(
            @Parameter(description = "租户ID", required = true) @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "状态") @RequestParam(value = "status", required = false) Byte status,
            @Parameter(description = "应用类型") @RequestParam(value = "appType", required = false) String appType,
            @Parameter(description = "页码", required = true) @RequestParam("pageNum") Integer pageNum,
            @Parameter(description = "页大小", required = true) @RequestParam("pageSize") Integer pageSize) {
        List<OpenApiKeySpecDTO> list = openApiKeysApiService.listOpenApiKeysPage(
                tenantId, status, appType, pageNum, pageSize);
        return Result.success(list);
    }

    @Operation(summary = "创建API密钥", description = "创建新的API密钥")
    @PostMapping("/create")
    public Result<OpenApiKeySpecDTO> createOpenApiKeys(
            @Valid @RequestBody OpenApiKeySpecDTO openApiKeysDTO) {
        OpenApiKeySpecDTO created = openApiKeysApiService.createOpenApiKeys(openApiKeysDTO);
        return Result.success(created);
    }

    @Operation(summary = "更新API密钥", description = "更新API密钥信息")
    @PutMapping("/update")
    public Result<Void> updateOpenApiKeys(
            @Valid @RequestBody OpenApiKeySpecDTO openApiKeysDTO) {
        openApiKeysApiService.updateOpenApiKeys(openApiKeysDTO);
        return Result.success();
    }

    @Operation(summary = "删除API密钥", description = "删除API密钥")
    @DeleteMapping("/{id}")
    public Result<Void> deleteOpenApiKeys(
            @Parameter(description = "API密钥ID", required = true) @PathVariable("id") Long id) {
        openApiKeysApiService.deleteOpenApiKeys(id);
        return Result.success();
    }

    @Operation(summary = "更新API密钥状态", description = "更新API密钥状态和审核状态")
    @PutMapping("/status")
    public Result<Void> updateOpenApiKeysStatus(
            @Parameter(description = "API密钥ID", required = true) @RequestParam("id") Long id,
            @Parameter(description = "状态", required = true) @RequestParam("status") Byte status,
            @Parameter(description = "审核状态") @RequestParam(value = "auditStatus", required = false) String auditStatus,
            @Parameter(description = "审核备注") @RequestParam(value = "auditRemark", required = false) String auditRemark) {
        openApiKeysApiService.updateOpenApiKeysStatus(id, status, auditStatus, auditRemark);
        return Result.success();
    }

    @Operation(summary = "统计API密钥数量", description = "统计租户下的API密钥总数")
    @GetMapping("/count/tenant/{tenantId}")
    public Result<Integer> countOpenApiKeysByTenantId(
            @Parameter(description = "租户ID", required = true) @PathVariable("tenantId") Long tenantId) {
        int count = openApiKeysApiService.countOpenApiKeysByTenantId(tenantId);
        return Result.success(count);
    }
}
