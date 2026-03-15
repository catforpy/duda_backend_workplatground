package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.common.dto.auth.*;
import com.duda.user.api.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * 认证控制器 V2 - 明确区分不同身份和登录方式
 *
 * 设计原则：
 * 1. 每种身份有独立的注册/登录接口
 * 2. 每种登录方式有独立的 DTO
 * 3. 支持同一人多身份叠加
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Tag(name = "用户认证V2", description = "区分身份和登录方式的认证接口")
@RestController
@RequestMapping("/api/auth/v2")
@CrossOrigin(originPatterns = "*", maxAge = 3600)  // 允许所有来源的跨域请求
public class AuthV2Controller {

    @Resource
    private IAuthService authService;

    // ==================== 都达网账户（账号密码方式）====================

    /**
     * 都达网账户 - 账号密码注册
     */
    @Operation(summary = "都达网账户注册（账号密码）", description = "使用用户名和密码注册为都达网账户")
    @PostMapping("/platform-account/register/password")
    public Result registerPlatformAccountByPassword(
            @Parameter(description = "账号密码注册参数", required = true)
            @Valid @RequestBody PasswordRegisterReqDTO request) {

        RegisterResponseDTO response = authService.registerPlatformAccountByPassword(request);
        return Result.success(response);
    }

    /**
     * 都达网账户 - 账号密码登录
     */
    @Operation(summary = "都达网账户登录（账号密码）", description = "使用用户名和密码登录都达网账户")
    @PostMapping("/platform-account/login/password")
    public Result loginPlatformAccountByPassword(
            @Parameter(description = "账号密码登录参数", required = true)
            @Valid @RequestBody PasswordLoginReqDTO request) {

        LoginResponseDTO response = authService.loginPlatformAccountByPassword(request);
        return Result.success(response);
    }

    /**
     * 都达网账户 - 手机验证码登录
     */
    @Operation(summary = "都达网账户登录（手机验证码）", description = "使用手机号和验证码登录都达网账户")
    @PostMapping("/platform-account/login/sms")
    public Result loginPlatformAccountBySms(
            @Parameter(description = "手机验证码登录参数", required = true)
            @Valid @RequestBody SmsCodeLoginReqDTO request) {

        LoginResponseDTO response = authService.loginPlatformAccountBySms(request);
        return Result.success(response);
    }

    // ==================== 服务商（账号密码方式）====================

    /**
     * 服务商 - 账号密码注册
     */
    @Operation(summary = "服务商注册（账号密码）", description = "使用用户名和密码注册为服务商")
    @PostMapping("/service-provider/register/password")
    public Result registerServiceProviderByPassword(
            @Parameter(description = "账号密码注册参数", required = true)
            @Valid @RequestBody PasswordRegisterReqDTO request) {

        RegisterResponseDTO response = authService.registerServiceProviderByPassword(request);
        return Result.success(response);
    }

    /**
     * 服务商 - 账号密码登录
     */
    @Operation(summary = "服务商登录（账号密码）", description = "使用用户名和密码登录服务商")
    @PostMapping("/service-provider/login/password")
    public Result loginServiceProviderByPassword(
            @Parameter(description = "账号密码登录参数", required = true)
            @Valid @RequestBody PasswordLoginReqDTO request) {

        LoginResponseDTO response = authService.loginServiceProviderByPassword(request);
        return Result.success(response);
    }

    /**
     * 服务商 - 手机验证码登录
     */
    @Operation(summary = "服务商登录（手机验证码）", description = "使用手机号和验证码登录服务商")
    @PostMapping("/service-provider/login/sms")
    public Result loginServiceProviderBySms(
            @Parameter(description = "手机验证码登录参数", required = true)
            @Valid @RequestBody SmsCodeLoginReqDTO request) {

        LoginResponseDTO response = authService.loginServiceProviderBySms(request);
        return Result.success(response);
    }

    // ==================== 平台管理员（账号密码方式）====================

    /**
     * 平台管理员 - 账号密码注册
     */
    @Operation(summary = "平台管理员注册（账号密码）", description = "使用用户名和密码注册为平台管理员")
    @PostMapping("/platform-admin/register/password")
    public Result registerPlatformAdminByPassword(
            @Parameter(description = "账号密码注册参数", required = true)
            @Valid @RequestBody PasswordRegisterReqDTO request) {

        RegisterResponseDTO response = authService.registerPlatformAdminByPassword(request);
        return Result.success(response);
    }

    /**
     * 平台管理员 - 账号密码登录
     */
    @Operation(summary = "平台管理员登录（账号密码）", description = "使用用户名和密码登录平台管理员")
    @PostMapping("/platform-admin/login/password")
    public Result loginPlatformAdminByPassword(
            @Parameter(description = "账号密码登录参数", required = true)
            @Valid @RequestBody PasswordLoginReqDTO request) {

        LoginResponseDTO response = authService.loginPlatformAdminByPassword(request);
        return Result.success(response);
    }

    // ==================== 后台管理员（账号密码方式）====================

    /**
     * 后台管理员 - 账号密码注册
     */
    @Operation(summary = "后台管理员注册（账号密码）", description = "使用用户名和密码注册为后台管理员")
    @PostMapping("/backend-admin/register/password")
    public Result registerBackendAdminByPassword(
            @Parameter(description = "账号密码注册参数", required = true)
            @Valid @RequestBody PasswordRegisterReqDTO request) {

        RegisterResponseDTO response = authService.registerBackendAdminByPassword(request);
        return Result.success(response);
    }

    /**
     * 后台管理员 - 账号密码登录
     */
    @Operation(summary = "后台管理员登录（账号密码）", description = "使用用户名和密码登录后台管理员")
    @PostMapping("/backend-admin/login/password")
    public Result loginBackendAdminByPassword(
            @Parameter(description = "账号密码登录参数", required = true)
            @Valid @RequestBody PasswordLoginReqDTO request) {

        LoginResponseDTO response = authService.loginBackendAdminByPassword(request);
        return Result.success(response);
    }

    // ==================== 通用接口（不区分身份）====================

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "退出登录，失效Token")
    @PostMapping("/logout")
    public Result logout(
            @Parameter(description = "访问令牌（格式：Bearer {token}）", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId) {

        authService.logout(authorization, userId);
        return Result.success();
    }

    /**
     * 刷新Token
     */
    @Operation(summary = "刷新Token", description = "使用RefreshToken获取新的AccessToken")
    @PostMapping("/refresh")
    public Result refresh(
            @Parameter(description = "刷新令牌", required = true)
            @RequestParam String refreshToken) {

        LoginResponseDTO response = authService.refreshToken(refreshToken);
        return Result.success(response);
    }

    /**
     * 验证Token
     */
    @Operation(summary = "验证Token", description = "验证AccessToken是否有效")
    @GetMapping("/validate")
    public Result validate(
            @Parameter(description = "访问令牌（格式：Bearer {token}）", required = true)
            @RequestHeader("Authorization") String authorization) {

        boolean valid = authService.validateToken(authorization);
        return Result.success(valid);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "从Token中解析用户信息")
    @GetMapping("/user/info")
    public Result getUserInfo(
            @Parameter(description = "访问令牌（格式：Bearer {token}）", required = true)
            @RequestHeader("Authorization") String authorization) {

        Object userInfo = authService.getUserInfo(authorization);
        return Result.success(userInfo);
    }

    /**
     * 发送短信验证码
     */
    @Operation(summary = "发送短信验证码", description = "发送登录/注册验证码到手机")
    @PostMapping("/sms/send")
    public Result sendSmsCode(
            @Parameter(description = "手机号", required = true)
            @RequestParam(value = "phone") String phone) {

        authService.sendSmsCode(phone);
        return Result.success("验证码发送成功，请查收短信");
    }
}
