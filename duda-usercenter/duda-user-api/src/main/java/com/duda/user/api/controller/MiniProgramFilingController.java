package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.user.api.service.MiniProgramFilingApiService;
import com.duda.user.dto.miniprogram.MiniProgramFilingDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "小程序备案管理", description = "小程序备案相关接口")
@RestController
@RequestMapping("/miniprogram/filing")
public class MiniProgramFilingController {

    @Resource
    private MiniProgramFilingApiService miniProgramFilingApiService;

    @Operation(summary = "查询备案详情")
    @GetMapping("/{id}")
    public Result<MiniProgramFilingDTO> getFilingById(@PathVariable Long id) {
        return Result.success(miniProgramFilingApiService.getFilingById(id));
    }

    @Operation(summary = "查询租户备案列表")
    @GetMapping("/list/tenant/{tenantId}")
    public Result<List<MiniProgramFilingDTO>> listFilingsByTenantId(@PathVariable Long tenantId) {
        return Result.success(miniProgramFilingApiService.listFilingsByTenantId(tenantId));
    }

    @Operation(summary = "根据小程序ID查询备案")
    @GetMapping("/miniprogram/{miniProgramId}")
    public Result<MiniProgramFilingDTO> getFilingByMiniProgramId(@PathVariable Long miniProgramId) {
        return Result.success(miniProgramFilingApiService.getFilingByMiniProgramId(miniProgramId));
    }

    @Operation(summary = "创建备案信息")
    @PostMapping("/create")
    public Result<MiniProgramFilingDTO> createFiling(@Valid @RequestBody MiniProgramFilingDTO filingDTO) {
        return Result.success(miniProgramFilingApiService.createFiling(filingDTO));
    }

    @Operation(summary = "更新备案信息")
    @PutMapping("/update")
    public Result<Void> updateFiling(@Valid @RequestBody MiniProgramFilingDTO filingDTO) {
        miniProgramFilingApiService.updateFiling(filingDTO);
        return Result.success();
    }

    @Operation(summary = "删除备案信息")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFiling(@PathVariable Long id) {
        miniProgramFilingApiService.deleteFiling(id);
        return Result.success();
    }
}
