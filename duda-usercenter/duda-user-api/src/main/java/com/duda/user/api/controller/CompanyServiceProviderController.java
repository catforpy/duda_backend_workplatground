package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.user.api.service.CompanyServiceProviderApiService;
import com.duda.user.dto.company.CompanyServiceProviderDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "服务商申请管理", description = "服务商申请相关接口")
@RestController
@RequestMapping("/company/service-provider")
public class CompanyServiceProviderController {

    @Resource
    private CompanyServiceProviderApiService companyServiceProviderApiService;

    @Operation(summary = "查询服务商申请详情")
    @GetMapping("/{id}")
    public Result<CompanyServiceProviderDTO> getServiceProviderById(@PathVariable Long id) {
        return Result.success(companyServiceProviderApiService.getServiceProviderById(id));
    }

    @Operation(summary = "查询租户服务商列表")
    @GetMapping("/list/tenant/{tenantId}")
    public Result<List<CompanyServiceProviderDTO>> listServiceProvidersByTenantId(@PathVariable Long tenantId) {
        return Result.success(companyServiceProviderApiService.listServiceProvidersByTenantId(tenantId));
    }

    @Operation(summary = "根据公司查询服务商")
    @GetMapping("/list/company")
    public Result<List<CompanyServiceProviderDTO>> listServiceProvidersByCompany(
            @RequestParam Long tenantId, @RequestParam Long companyId) {
        return Result.success(companyServiceProviderApiService.listServiceProvidersByCompany(tenantId, companyId));
    }

    @Operation(summary = "根据类型查询服务商")
    @GetMapping("/list/type")
    public Result<List<CompanyServiceProviderDTO>> listServiceProvidersByType(
            @RequestParam Long tenantId, @RequestParam String applyType) {
        return Result.success(companyServiceProviderApiService.listServiceProvidersByType(tenantId, applyType));
    }

    @Operation(summary = "根据状态查询服务商")
    @GetMapping("/list/status")
    public Result<List<CompanyServiceProviderDTO>> listServiceProvidersByStatus(
            @RequestParam Long tenantId, @RequestParam String status) {
        return Result.success(companyServiceProviderApiService.listServiceProvidersByStatus(tenantId, status));
    }

    @Operation(summary = "创建服务商申请")
    @PostMapping("/create")
    public Result<CompanyServiceProviderDTO> createServiceProvider(@Valid @RequestBody CompanyServiceProviderDTO serviceProviderDTO) {
        return Result.success(companyServiceProviderApiService.createServiceProvider(serviceProviderDTO));
    }

    @Operation(summary = "更新服务商申请")
    @PutMapping("/update")
    public Result<Void> updateServiceProvider(@Valid @RequestBody CompanyServiceProviderDTO serviceProviderDTO) {
        companyServiceProviderApiService.updateServiceProvider(serviceProviderDTO);
        return Result.success();
    }

    @Operation(summary = "删除服务商申请")
    @DeleteMapping("/{id}")
    public Result<Void> deleteServiceProvider(@PathVariable Long id) {
        companyServiceProviderApiService.deleteServiceProvider(id);
        return Result.success();
    }
}
