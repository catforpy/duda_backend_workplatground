package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.common.dto.auth.LoginRequestDTO;
import com.duda.common.dto.auth.LoginResponseDTO;
import com.duda.common.dto.auth.RegisterRequestDTO;
import com.duda.common.dto.auth.RegisterResponseDTO;
import com.duda.user.api.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * 提供4种身份 × 3种登录方式的认证API
 *
 * 架构说明：
 * Controller 层：只负责接收HTTP请求、参数验证、返回响应
 * Service 层：处理业务逻辑、调用RPC服务、组装数据
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Tag(name = "用户认证", description = "用户注册、登录、登出等认证接口（支持多身份多登录方式）")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private IAuthService authService;

    /**
     * 用户注册
     *
     * 支持4种身份 × 3种注册方式 = 12种组合
     */
    @Operation(
        summary = "用户注册",
        description = "支持手机号、邮箱、第三方3种注册方式，每种方式支持4种用户身份"
    )
    @PostMapping("/register")
    public Result register(
            @Parameter(description = "注册请求参数", required = true)
            @Valid @RequestBody RegisterRequestDTO request) {

        RegisterResponseDTO response = authService.register(request);
        return Result.success(response);
    }

    /**
     * 用户登录
     *
     * 支持4种身份 × 3种登录方式 = 12种组合
     */
    @Operation(
        summary = "用户登录",
        description = "支持手机号密码、邮箱密码、第三方3种登录方式，每种方式支持4种用户身份"
    )
    @PostMapping("/login")
    public Result login(
            @Parameter(description = "登录请求参数", required = true)
            @Valid @RequestBody LoginRequestDTO request) {

        LoginResponseDTO response = authService.login(request);
        return Result.success(response);
    }

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
     *
     * 用途：
     * - 注册：手机号验证码注册
     * - 登录：手机号验证码登录
     * - 找回密码：验证身份
     */
    @Operation(summary = "发送短信验证码", description = "发送登录/注册验证码到手机")
    @PostMapping("/sms/send")
    public Result sendSmsCode(
            @Parameter(description = "手机号", required = true)
            @RequestParam(value = "phone") String phone) {

        authService.sendSmsCode(phone);
        return Result.success("验证码发送成功，请查收短信");
    }

    // ==================== 快捷测试接口（用于Swagger测试）====================

    @Operation(summary = "【测试】都达网账号注册-普通用户", description = "快捷测试接口，预填数据")
    @PostMapping("/test/register/account/normal")
    public Result testRegisterAccountNormal() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType("normal");
        request.setRegisterType("account");
        request.setUsername("testuser_account");
        request.setPassword("pass123456");
        request.setConfirmPassword("pass123456");
        request.setRealName("测试用户");
        request.setPhone("13900139001");
        request.setClientIp("127.0.0.1");

        RegisterResponseDTO response = authService.register(request);
        return Result.success(response);
    }

    @Operation(summary = "【测试】手机号验证码注册-普通用户", description = "快捷测试接口，预填数据")
    @PostMapping("/test/register/phone-sms/normal")
    public Result testRegisterPhoneSmsNormal() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType("normal");
        request.setRegisterType("phone_sms");
        request.setPhone("13900139002");
        request.setPhoneVerifyCode("123456");
        request.setRealName("手机验证码用户");
        request.setClientIp("127.0.0.1");

        RegisterResponseDTO response = authService.register(request);
        return Result.success(response);
    }

    @Operation(summary = "【测试】都达网账号注册-商家", description = "快捷测试接口，预填数据")
    @PostMapping("/test/register/account/merchant")
    public Result testRegisterAccountMerchant() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType("merchant");
        request.setRegisterType("account");
        request.setUsername("testuser_merchant");
        request.setPassword("pass123456");
        request.setConfirmPassword("pass123456");
        request.setRealName("测试商家");
        request.setClientIp("127.0.0.1");

        RegisterResponseDTO response = authService.register(request);
        return Result.success(response);
    }

    @Operation(summary = "【测试】都达网账号登录-普通用户", description = "快捷测试接口，预填数据")
    @PostMapping("/test/login/account/normal")
    public Result testLoginAccountNormal() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType("account_password");
        request.setUserType("normal");
        request.setUsername("testuser_account");
        request.setPassword("pass123456");
        request.setClientIp("127.0.0.1");

        LoginResponseDTO response = authService.login(request);
        return Result.success(response);
    }

    @Operation(summary = "【测试】手机号验证码登录-普通用户", description = "快捷测试接口，预填数据")
    @PostMapping("/test/login/phone-sms/normal")
    public Result testLoginPhoneSmsNormal() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType("phone_sms");
        request.setUserType("normal");
        request.setPhone("13900139001");
        request.setPhoneVerifyCode("123456");
        request.setClientIp("127.0.0.1");

        LoginResponseDTO response = authService.login(request);
        return Result.success(response);
    }

    @Operation(summary = "【测试】都达网账号登录-商家", description = "快捷测试接口，预填数据")
    @PostMapping("/test/login/account/merchant")
    public Result testLoginAccountMerchant() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType("account_password");
        request.setUserType("merchant");
        request.setUsername("testuser_merchant");
        request.setPassword("pass123456");
        request.setClientIp("127.0.0.1");

        LoginResponseDTO response = authService.login(request);
        return Result.success(response);
    }
}
