package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.user.api.service.CompanyQualificationApiService;
import com.duda.user.dto.company.CompanyQualificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公司资质管理", description = "公司资质相关接口")
@RestController
@RequestMapping("/company/qualification")
public class CompanyQualificationController {

    @Resource
    private CompanyQualificationApiService companyQualificationApiService;

    @Operation(summary = "查询资质详情")
    @GetMapping("/{id}")
    public Result<CompanyQualificationDTO> getQualificationById(@PathVariable Long id) {
        return Result.success(companyQualificationApiService.getQualificationById(id));
    }

    @Operation(summary = "查询租户资质列表")
    @GetMapping("/list/tenant/{tenantId}")
    public Result<List<CompanyQualificationDTO>> listQualificationsByTenantId(@PathVariable Long tenantId) {
        return Result.success(companyQualificationApiService.listQualificationsByTenantId(tenantId));
    }

    @Operation(summary = "根据公司查询资质")
    @GetMapping("/list/company")
    public Result<List<CompanyQualificationDTO>> listQualificationsByCompany(
            @RequestParam Long tenantId, @RequestParam Long companyId) {
        return Result.success(companyQualificationApiService.listQualificationsByCompany(tenantId, companyId));
    }

    @Operation(summary = "根据类型查询资质")
    @GetMapping("/list/type")
    public Result<List<CompanyQualificationDTO>> listQualificationsByType(
            @RequestParam Long tenantId, @RequestParam String qualificationType) {
        return Result.success(companyQualificationApiService.listQualificationsByType(tenantId, qualificationType));
    }

    @Operation(summary = "创建资质信息")
    @PostMapping("/create")
    public Result<CompanyQualificationDTO> createQualification(@Valid @RequestBody CompanyQualificationDTO qualificationDTO) {
        return Result.success(companyQualificationApiService.createQualification(qualificationDTO));
    }

    @Operation(summary = "更新资质信息")
    @PutMapping("/update")
    public Result<Void> updateQualification(@Valid @RequestBody CompanyQualificationDTO qualificationDTO) {
        companyQualificationApiService.updateQualification(qualificationDTO);
        return Result.success();
    }

    @Operation(summary = "删除资质信息")
    @DeleteMapping("/{id}")
    public Result<Void> deleteQualification(@PathVariable Long id) {
        companyQualificationApiService.deleteQualification(id);
        return Result.success();
    }
}
