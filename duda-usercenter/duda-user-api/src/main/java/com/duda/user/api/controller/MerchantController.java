package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.user.api.service.MerchantApiService;
import com.duda.user.dto.merchant.MerchantDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户Controller - API层
 *
 * 提供REST API，与前端对接
 * 内部调用Provider的RPC服务（通过MerchantApiService）
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Tag(name = "商户管理", description = "商户相关接口")
@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Resource
    private MerchantApiService merchantApiService;

    /**
     * 根据ID查询商户
     */
    @Operation(summary = "查询商户详情", description = "根据商户ID查询商户详细信息")
    @GetMapping("/{id}")
    public Result<MerchantDTO> getMerchantById(
            @Parameter(description = "商户ID", required = true)
            @PathVariable("id") Long id) {
        MerchantDTO merchant = merchantApiService.getMerchantById(id);
        return Result.success(merchant);
    }

    /**
     * 根据租户ID查询商户列表
     */
    @Operation(summary = "查询租户商户列表", description = "根据租户ID查询该租户下的所有商户")
    @GetMapping("/list/tenant/{tenantId}")
    public Result<List<MerchantDTO>> listMerchantsByTenantId(
            @Parameter(description = "租户ID", required = true)
            @PathVariable("tenantId") Long tenantId) {
        List<MerchantDTO> list = merchantApiService.listMerchantsByTenantId(tenantId);
        return Result.success(list);
    }

    /**
     * 根据商户编码查询
     */
    @Operation(summary = "根据编码查询商户", description = "根据商户编码查询商户信息")
    @GetMapping("/code")
    public Result<MerchantDTO> getMerchantByCode(
            @Parameter(description = "租户ID", required = true)
            @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "商户编码", required = true)
            @RequestParam("merchantCode") String merchantCode) {
        MerchantDTO merchant = merchantApiService.getMerchantByCode(tenantId, merchantCode);
        return Result.success(merchant);
    }

    /**
     * 根据状态查询商户列表
     */
    @Operation(summary = "根据状态查询商户", description = "根据状态查询商户列表")
    @GetMapping("/list/status")
    public Result<List<MerchantDTO>> listMerchantsByStatus(
            @Parameter(description = "租户ID", required = true)
            @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "状态", required = true)
            @RequestParam("status") String status) {
        List<MerchantDTO> list = merchantApiService.listMerchantsByStatus(tenantId, status);
        return Result.success(list);
    }

    /**
     * 分页查询商户列表
     */
    @Operation(summary = "分页查询商户", description = "分页查询商户列表")
    @GetMapping("/page")
    public Result<List<MerchantDTO>> listMerchantsPage(
            @Parameter(description = "租户ID", required = true)
            @RequestParam("tenantId") Long tenantId,
            @Parameter(description = "状态")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "页码", required = true)
            @RequestParam("pageNum") Integer pageNum,
            @Parameter(description = "页大小", required = true)
            @RequestParam("pageSize") Integer pageSize) {
        List<MerchantDTO> list = merchantApiService.listMerchantsPage(tenantId, status, pageNum, pageSize);
        return Result.success(list);
    }

    /**
     * 创建商户
     */
    @Operation(summary = "创建商户", description = "创建新的商户")
    @PostMapping("/create")
    public Result<MerchantDTO> createMerchant(
            @Valid @RequestBody MerchantDTO merchantDTO) {
        MerchantDTO created = merchantApiService.createMerchant(merchantDTO);
        return Result.success(created);
    }

    /**
     * 更新商户
     */
    @Operation(summary = "更新商户", description = "更新商户信息")
    @PutMapping("/update")
    public Result<Void> updateMerchant(
            @Valid @RequestBody MerchantDTO merchantDTO) {
        merchantApiService.updateMerchant(merchantDTO);
        return Result.success();
    }

    /**
     * 删除商户
     */
    @Operation(summary = "删除商户", description = "删除商户（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMerchant(
            @Parameter(description = "商户ID", required = true)
            @PathVariable("id") Long id) {
        merchantApiService.deleteMerchant(id);
        return Result.success();
    }

    /**
     * 更新商户状态
     */
    @Operation(summary = "更新商户状态", description = "更新商户状态和审核状态")
    @PutMapping("/status")
    public Result<Void> updateMerchantStatus(
            @Parameter(description = "商户ID", required = true)
            @RequestParam("id") Long id,
            @Parameter(description = "状态", required = true)
            @RequestParam("status") String status,
            @Parameter(description = "审核状态")
            @RequestParam(value = "auditStatus", required = false) String auditStatus,
            @Parameter(description = "审核备注")
            @RequestParam(value = "auditRemark", required = false) String auditRemark) {
        merchantApiService.updateMerchantStatus(id, status, auditStatus, auditRemark);
        return Result.success();
    }

    /**
     * 统计租户下的商户数量
     */
    @Operation(summary = "统计商户数量", description = "统计租户下的商户总数")
    @GetMapping("/count/tenant/{tenantId}")
    public Result<Integer> countMerchantsByTenantId(
            @Parameter(description = "租户ID", required = true)
            @PathVariable("tenantId") Long tenantId) {
        int count = merchantApiService.countMerchantsByTenantId(tenantId);
        return Result.success(count);
    }
}
