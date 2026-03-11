package com.duda.common.security.service;

import com.duda.common.dto.auth.LoginRequestDTO;
import com.duda.common.dto.auth.LoginResponseDTO;
import com.duda.common.dto.auth.RegisterRequestDTO;
import com.duda.common.dto.auth.RegisterResponseDTO;

/**
 * 认证服务接口
 *
 * 提供统一的认证功能：
 * 1. 注册（支持3种方式 × 4种身份 = 12种组合）
 * 2. 登录（支持3种方式 × 4种身份 = 12种组合）
 * 3. 登出
 * 4. 刷新Token
 * 5. 验证Token
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * 支持3种注册方式：
     * 1. 手机号+密码+验证码
     * 2. 邮箱+密码+验证码
     * 3. 第三方账号绑定
     *
     * 支持4种用户身份：
     * 1. 普通用户（normal）
     * 2. 商家（merchant）
     * 3. 运营（operator）
     * 4. 管理员（admin）
     *
     * @param request 注册请求
     * @return 注册响应
     */
    RegisterResponseDTO register(RegisterRequestDTO request);

    /**
     * 用户登录
     *
     * 支持3种登录方式：
     * 1. 手机号+密码
     * 2. 邮箱+密码
     * 3. 第三方登录
     *
     * 支持4种用户身份：
     * 1. 普通用户（normal）
     * 2. 商家（merchant）
     * 3. 运营（operator）
     * 4. 管理员（admin）
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponseDTO login(LoginRequestDTO request);

    /**
     * 用户登出
     *
     * @param accessToken 访问令牌
     * @param userId 用户ID
     */
    void logout(String accessToken, Long userId);

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录响应
     */
    LoginResponseDTO refreshToken(String refreshToken);

    /**
     * 验证Token
     *
     * @param accessToken 访问令牌
     * @return 是否有效
     */
    boolean validateToken(String accessToken);

    /**
     * 从Token中获取用户ID
     *
     * @param token 令牌
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 从Token中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    String getUsernameFromToken(String token);

    /**
     * 从Token中获取用户类型
     *
     * @param token 令牌
     * @return 用户类型
     */
    String getUserTypeFromToken(String token);
}
