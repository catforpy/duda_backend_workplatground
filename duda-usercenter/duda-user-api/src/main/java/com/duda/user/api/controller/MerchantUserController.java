package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.user.api.service.MerchantUserApiService;
import com.duda.user.dto.merchant.MerchantUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户用户Controller - API层
 *
 * 提供REST API，管理商户与用户的绑定关系
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Tag(name = "商户用户管理", description = "商户用户绑定关系相关接口")
@RestController
@RequestMapping("/merchant/user")
public class MerchantUserController {

    @Resource
    private MerchantUserApiService merchantUserApiService;

    @Operation(summary = "查询商户用户详情", description = "根据ID查询商户用户绑定关系")
    @GetMapping("/{id}")
    public Result<MerchantUserDTO> getMerchantUserById(
            @Parameter(description = "商户用户ID", required = true) @PathVariable("id") Long id) {
        MerchantUserDTO merchantUser = merchantUserApiService.getMerchantUserById(id);
        return Result.success(merchantUser);
    }

    @Operation(summary = "查询商户用户列表", description = "查询指定商户的所有用户")
    @GetMapping("/list/merchant/{merchantId}")
    public Result<List<MerchantUserDTO>> listUsersByMerchantId(
            @Parameter(description = "商户ID", required = true) @PathVariable("merchantId") Long merchantId) {
        List<MerchantUserDTO> list = merchantUserApiService.listUsersByMerchantId(merchantId);
        return Result.success(list);
    }

    @Operation(summary = "查询用户的商户列表", description = "查询指定用户的所有商户")
    @GetMapping("/list/platform-user/{platformUserId}")
    public Result<List<MerchantUserDTO>> listMerchantsByPlatformUserId(
            @Parameter(description = "平台用户ID", required = true) @PathVariable("platformUserId") Long platformUserId) {
        List<MerchantUserDTO> list = merchantUserApiService.listMerchantsByPlatformUserId(platformUserId);
        return Result.success(list);
    }

    @Operation(summary = "查询商户用户关系", description = "查询指定用户在指定商户的绑定关系")
    @GetMapping("/relation")
    public Result<MerchantUserDTO> getMerchantUser(
            @Parameter(description = "商户ID", required = true) @RequestParam("merchantId") Long merchantId,
            @Parameter(description = "平台用户ID", required = true) @RequestParam("platformUserId") Long platformUserId) {
        MerchantUserDTO merchantUser = merchantUserApiService.getMerchantUser(merchantId, platformUserId);
        return Result.success(merchantUser);
    }

    @Operation(summary = "根据OpenID查询用户", description = "根据微信OpenID查询商户用户")
    @GetMapping("/openid")
    public Result<MerchantUserDTO> getMerchantUserByOpenid(
            @Parameter(description = "商户ID", required = true) @RequestParam("merchantId") Long merchantId,
            @Parameter(description = "微信OpenID", required = true) @RequestParam("openid") String openid) {
        MerchantUserDTO merchantUser = merchantUserApiService.getMerchantUserByOpenid(merchantId, openid);
        return Result.success(merchantUser);
    }

    @Operation(summary = "绑定商户用户", description = "创建或更新用户与商户的绑定关系")
    @PostMapping("/bind")
    public Result<MerchantUserDTO> bindMerchantUser(
            @Valid @RequestBody MerchantUserDTO merchantUserDTO) {
        MerchantUserDTO result = merchantUserApiService.bindMerchantUser(merchantUserDTO);
        return Result.success(result);
    }

    @Operation(summary = "更新访问信息", description = "更新用户最后访问时间和次数")
    @PutMapping("/visit")
    public Result<Void> updateVisitInfo(
            @Parameter(description = "商户ID", required = true) @RequestParam("merchantId") Long merchantId,
            @Parameter(description = "平台用户ID", required = true) @RequestParam("platformUserId") Long platformUserId) {
        Boolean result = merchantUserApiService.updateVisitInfo(merchantId, platformUserId);
        return Result.success();
    }

    @Operation(summary = "解绑商户用户", description = "解除用户与商户的绑定关系")
    @DeleteMapping("/unbind")
    public Result<Void> unbindMerchantUser(
            @Parameter(description = "商户ID", required = true) @RequestParam("merchantId") Long merchantId,
            @Parameter(description = "平台用户ID", required = true) @RequestParam("platformUserId") Long platformUserId) {
        Boolean result = merchantUserApiService.unbindMerchantUser(merchantId, platformUserId);
        return Result.success();
    }

    @Operation(summary = "统计商户用户数量", description = "统计指定商户的总用户数")
    @GetMapping("/count/merchant/{merchantId}")
    public Result<Integer> countUsersByMerchantId(
            @Parameter(description = "商户ID", required = true) @PathVariable("merchantId") Long merchantId) {
        int count = merchantUserApiService.countUsersByMerchantId(merchantId);
        return Result.success(count);
    }

    @Operation(summary = "统计用户的商户数量", description = "统计指定用户的总商户数")
    @GetMapping("/count/platform-user/{platformUserId}")
    public Result<Integer> countMerchantsByPlatformUserId(
            @Parameter(description = "平台用户ID", required = true) @PathVariable("platformUserId") Long platformUserId) {
        int count = merchantUserApiService.countMerchantsByPlatformUserId(platformUserId);
        return Result.success(count);
    }
}
