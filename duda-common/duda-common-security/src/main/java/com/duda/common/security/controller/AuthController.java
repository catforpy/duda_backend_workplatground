package com.duda.common.security.controller;

import com.duda.common.domain.Result;
import com.duda.common.dto.auth.LoginRequestDTO;
import com.duda.common.dto.auth.LoginResponseDTO;
import com.duda.common.dto.auth.RegisterRequestDTO;
import com.duda.common.dto.auth.RegisterResponseDTO;
import com.duda.common.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * 提供4种身份 × 3种方式的注册和登录API
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户注册、登录、登出等认证相关接口")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Resource
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "支持手机号、邮箱、第三方3种注册方式，每种方式支持4种用户身份")
    public Result<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        logger.info("收到注册请求，userType: {}, registerType: {}",
                request.getUserType(), request.getRegisterType());
        RegisterResponseDTO response = authService.register(request);
        return Result.success(response);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持手机号密码、邮箱密码、第三方3种登录方式，每种方式支持4种用户身份")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        logger.info("收到登录请求，loginType: {}, userType: {}",
                request.getLoginType(), request.getUserType());
        LoginResponseDTO response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录，失效Token")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization,
                               @RequestParam Long userId) {
        logger.info("收到登出请求，userId: {}", userId);
        // 移除 "Bearer " 前缀
        String accessToken = authorization.replace("Bearer ", "");
        authService.logout(accessToken, userId);
        return Result.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用RefreshToken获取新的AccessToken")
    public Result<LoginResponseDTO> refresh(@RequestParam String refreshToken) {
        logger.info("收到Token刷新请求");
        LoginResponseDTO response = authService.refreshToken(refreshToken);
        return Result.success(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "验证Token", description = "验证AccessToken是否有效")
    public Result<Boolean> validate(@RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.replace("Bearer ", "");
        boolean valid = authService.validateToken(accessToken);
        return Result.success(valid);
    }

    @GetMapping("/user/info")
    @Operation(summary = "获取当前用户信息", description = "从Token中解析用户信息")
    public Result<LoginResponseDTO> getUserInfo(@RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(accessToken);
        String username = authService.getUsernameFromToken(accessToken);
        String userType = authService.getUserTypeFromToken(accessToken);

        // TODO: 从数据库查询完整用户信息
        LoginResponseDTO response = LoginResponseDTO.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .build();

        return Result.success(response);
    }
}
