package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.common.dto.auth.LoginRequestDTO;
import com.duda.common.dto.auth.LoginResponseDTO;
import com.duda.common.dto.auth.RegisterRequestDTO;
import com.duda.common.dto.auth.RegisterResponseDTO;
import com.duda.common.enums.UserType;
import com.duda.common.security.service.AuthService;
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
 * 可通过Swagger UI直接测试
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Tag(name = "用户认证", description = "用户注册、登录、登出等认证接口（支持多身份多登录方式）")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 用户注册
     *
     * 支持4种身份 × 3种注册方式 = 12种组合
     *
     * 用户身份：
     * - normal: 普通用户
     * - merchant: 商家用户
     * - operator: 运营人员
     * - admin: 管理员
     *
     * 注册方式：
     * - phone: 手机号 + 密码 + 验证码
     * - email: 邮箱 + 密码 + 验证码
     * - third_party: 第三方账号绑定
     */
    @Operation(
        summary = "用户注册",
        description = "支持手机号、邮箱、第三方3种注册方式，每种方式支持4种用户身份"
    )
    @PostMapping("/register")
    public Result<RegisterResponseDTO> register(
            @Parameter(description = "注册请求参数", required = true)
            @Valid @RequestBody RegisterRequestDTO request) {

        RegisterResponseDTO response = authService.register(request);
        return Result.success(response);
    }

    /**
     * 用户登录
     *
     * 支持4种身份 × 3种登录方式 = 12种组合
     *
     * 登录方式：
     * - phone_password: 手机号 + 密码
     * - email_password: 邮箱 + 密码
     * - third_party: 第三方登录（微信/QQ/支付宝等）
     */
    @Operation(
        summary = "用户登录",
        description = "支持手机号密码、邮箱密码、第三方3种登录方式，每种方式支持4种用户身份"
    )
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(
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
    public Result<Void> logout(
            @Parameter(description = "访问令牌（格式：Bearer {token}）", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId) {

        // 移除 "Bearer " 前缀
        String accessToken = authorization.replace("Bearer ", "");
        authService.logout(accessToken, userId);
        return Result.success();
    }

    /**
     * 刷新Token
     * 使用RefreshToken获取新的AccessToken
     */
    @Operation(summary = "刷新Token", description = "使用RefreshToken获取新的AccessToken")
    @PostMapping("/refresh")
    public Result<LoginResponseDTO> refresh(
            @Parameter(description = "刷新令牌", required = true)
            @RequestParam String refreshToken) {

        LoginResponseDTO response = authService.refreshToken(refreshToken);
        return Result.success(response);
    }

    /**
     * 验证Token
     * 检查AccessToken是否有效
     */
    @Operation(summary = "验证Token", description = "验证AccessToken是否有效")
    @GetMapping("/validate")
    public Result<Boolean> validate(
            @Parameter(description = "访问令牌（格式：Bearer {token}）", required = true)
            @RequestHeader("Authorization") String authorization) {

        String accessToken = authorization.replace("Bearer ", "");
        boolean valid = authService.validateToken(accessToken);
        return Result.success(valid);
    }

    /**
     * 获取当前用户信息
     * 从Token中解析用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "从Token中解析用户信息")
    @GetMapping("/user/info")
    public Result<LoginResponseDTO> getUserInfo(
            @Parameter(description = "访问令牌（格式：Bearer {token}）", required = true)
            @RequestHeader("Authorization") String authorization) {

        String accessToken = authorization.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(accessToken);
        String username = authService.getUsernameFromToken(accessToken);
        String userType = authService.getUserTypeFromToken(accessToken);

        // TODO: 从数据库查询完整用户信息
        LoginResponseDTO response = LoginResponseDTO.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .userTypeName(UserType.fromCode(userType).getName())
                .build();

        return Result.success(response);
    }

    // ==================== 快捷测试接口（用于Swagger测试）====================

    /**
     * 快速测试：手机号注册（普通用户）
     *
     * 测试数据：
     * - 手机号: 13800138000
     * - 密码: pass123456
     * - 验证码: 123456（暂时不验证）
     */
    @Operation(summary = "【测试】手机号注册-普通用户", description = "快捷测试接口，预填数据")
    @PostMapping("/test/register/phone/normal")
    public Result<RegisterResponseDTO> testRegisterPhoneNormal() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType(UserType.NORMAL.getCode());
        request.setRegisterType("phone");
        request.setUsername("testuser_normal");
        request.setPassword("pass123456");
        request.setConfirmPassword("pass123456");
        request.setPhone("13800138000");
        request.setPhoneVerifyCode("123456");
        request.setClientIp("127.0.0.1");

        RegisterResponseDTO response = authService.register(request);
        return Result.success(response);
    }

    /**
     * 快速测试：手机号注册（商家）
     */
    @Operation(summary = "【测试】手机号注册-商家", description = "快捷测试接口，预填数据")
    @PostMapping("/test/register/phone/merchant")
    public Result<RegisterResponseDTO> testRegisterPhoneMerchant() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType(UserType.MERCHANT.getCode());
        request.setRegisterType("phone");
        request.setUsername("testuser_merchant");
        request.setPassword("pass123456");
        request.setConfirmPassword("pass123456");
        request.setPhone("13800138001");
        request.setPhoneVerifyCode("123456");
        request.setRealName("测试商家");
        request.setClientIp("127.0.0.1");

        RegisterResponseDTO response = authService.register(request);
        return Result.success(response);
    }

    /**
     * 快速测试：邮箱注册（普通用户）
     */
    @Operation(summary = "【测试】邮箱注册-普通用户", description = "快捷测试接口，预填数据")
    @PostMapping("/test/register/email/normal")
    public Result<RegisterResponseDTO> testRegisterEmailNormal() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType(UserType.NORMAL.getCode());
        request.setRegisterType("email");
        request.setUsername("testuser_email");
        request.setPassword("pass123456");
        request.setConfirmPassword("pass123456");
        request.setEmail("testuser@example.com");
        request.setEmailVerifyCode("123456");
        request.setClientIp("127.0.0.1");

        RegisterResponseDTO response = authService.register(request);
        return Result.success(response);
    }

    /**
     * 快速测试：第三方注册（微信）
     */
    @Operation(summary = "【测试】第三方注册-微信", description = "快捷测试接口，预填数据")
    @PostMapping("/test/register/third/wechat")
    public Result<RegisterResponseDTO> testRegisterThirdWechat() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType(UserType.NORMAL.getCode());
        request.setRegisterType("third_party");
        request.setThirdPartyPlatform("wechat");
        request.setThirdPartyOpenId("oTEST_OPEN_ID_" + System.currentTimeMillis());
        request.setThirdPartyNickname("微信测试用户");
        request.setThirdPartyAvatar("https://thirdparty.com/avatar.jpg");
        request.setClientIp("127.0.0.1");

        RegisterResponseDTO response = authService.register(request);
        return Result.success(response);
    }

    /**
     * 快速测试：手机号登录（普通用户）
     */
    @Operation(summary = "【测试】手机号登录-普通用户", description = "快捷测试接口，预填数据")
    @PostMapping("/test/login/phone/normal")
    public Result<LoginResponseDTO> testLoginPhoneNormal() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType("phone_password");
        request.setUserType(UserType.NORMAL.getCode());
        request.setPhone("13800138000");
        request.setPassword("pass123456");
        request.setClientIp("127.0.0.1");

        LoginResponseDTO response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 快速测试：手机号登录（商家）
     */
    @Operation(summary = "【测试】手机号登录-商家", description = "快捷测试接口，预填数据")
    @PostMapping("/test/login/phone/merchant")
    public Result<LoginResponseDTO> testLoginPhoneMerchant() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType("phone_password");
        request.setUserType(UserType.MERCHANT.getCode());
        request.setPhone("13800138001");
        request.setPassword("pass123456");
        request.setClientIp("127.0.0.1");

        LoginResponseDTO response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 快速测试：邮箱登录（普通用户）
     */
    @Operation(summary = "【测试】邮箱登录-普通用户", description = "快捷测试接口，预填数据")
    @PostMapping("/test/login/email/normal")
    public Result<LoginResponseDTO> testLoginEmailNormal() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType("email_password");
        request.setUserType(UserType.NORMAL.getCode());
        request.setEmail("testuser@example.com");
        request.setPassword("pass123456");
        request.setClientIp("127.0.0.1");

        LoginResponseDTO response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 快速测试：第三方登录（微信）
     */
    @Operation(summary = "【测试】第三方登录-微信", description = "快捷测试接口，预填数据")
    @PostMapping("/test/login/third/wechat")
    public Result<LoginResponseDTO> testLoginThirdWechat() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType("third_party");
        request.setUserType(UserType.NORMAL.getCode());
        request.setThirdPartyPlatform("wechat");
        request.setThirdPartyAuthCode("test_auth_code_" + System.currentTimeMillis());
        request.setThirdPartyOpenId("oTEST_OPEN_ID_" + System.currentTimeMillis());
        request.setClientIp("127.0.0.1");

        LoginResponseDTO response = authService.login(request);
        return Result.success(response);
    }
}
