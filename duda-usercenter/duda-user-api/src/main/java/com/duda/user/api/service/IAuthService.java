package com.duda.user.api.service;

import com.duda.common.dto.auth.*;

/**
 * 认证服务接口
 *
 * 提供用户注册、登录、登出等功能
 * V2：明确区分不同身份和不同登录方式
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
public interface IAuthService {

    /**
     * 用户注册
     *
     * 支持4种身份 × 3种注册方式 = 12种组合
     *
     * @param request 注册请求参数
     * @return 注册响应结果
     */
    RegisterResponseDTO register(RegisterRequestDTO request);

    /**
     * 用户登录
     *
     * 支持4种身份 × 3种登录方式 = 12种组合
     *
     * @param request 登录请求参数
     * @return 登录响应结果
     */
    LoginResponseDTO login(LoginRequestDTO request);

    /**
     * 用户登出
     *
     * @param authorization Authorization头（格式：Bearer {token}）
     * @param userId 用户ID
     */
    void logout(String authorization, Long userId);

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录响应结果
     */
    LoginResponseDTO refreshToken(String refreshToken);

    /**
     * 验证Token
     *
     * @param authorization Authorization头（格式：Bearer {token}）
     * @return 是否有效
     */
    boolean validateToken(String authorization);

    /**
     * 获取当前用户信息
     *
     * @param authorization Authorization头（格式：Bearer {token}）
     * @return 用户信息
     */
    Object getUserInfo(String authorization);

    /**
     * 发送短信验证码
     *
     * 功能：
     * - 生成随机验证码
     * - 存储到Redis（60秒过期）
     * - 防重发检查
     * - 支持开发环境打印日志、生产环境真实发送
     *
     * @param phone 手机号
     */
    void sendSmsCode(String phone);

    // ==================== V2：明确区分身份和登录方式 ====================

    /**
     * 都达网账户 - 账号密码注册
     *
     * @param request 账号密码注册请求
     * @return 注册响应
     */
    RegisterResponseDTO registerPlatformAccountByPassword(PasswordRegisterReqDTO request);

    /**
     * 都达网账户 - 账号密码登录
     *
     * @param request 账号密码登录请求
     * @return 登录响应
     */
    LoginResponseDTO loginPlatformAccountByPassword(PasswordLoginReqDTO request);

    /**
     * 都达网账户 - 手机验证码登录
     *
     * @param request 手机验证码登录请求
     * @return 登录响应
     */
    LoginResponseDTO loginPlatformAccountBySms(SmsCodeLoginReqDTO request);

    /**
     * 服务商 - 账号密码注册
     *
     * @param request 账号密码注册请求
     * @return 注册响应
     */
    RegisterResponseDTO registerServiceProviderByPassword(PasswordRegisterReqDTO request);

    /**
     * 服务商 - 账号密码登录
     *
     * @param request 账号密码登录请求
     * @return 登录响应
     */
    LoginResponseDTO loginServiceProviderByPassword(PasswordLoginReqDTO request);

    /**
     * 服务商 - 手机验证码登录
     *
     * @param request 手机验证码登录请求
     * @return 登录响应
     */
    LoginResponseDTO loginServiceProviderBySms(SmsCodeLoginReqDTO request);

    /**
     * 平台管理员 - 账号密码注册
     *
     * @param request 账号密码注册请求
     * @return 注册响应
     */
    RegisterResponseDTO registerPlatformAdminByPassword(PasswordRegisterReqDTO request);

    /**
     * 平台管理员 - 账号密码登录
     *
     * @param request 账号密码登录请求
     * @return 登录响应
     */
    LoginResponseDTO loginPlatformAdminByPassword(PasswordLoginReqDTO request);

    /**
     * 后台管理员 - 账号密码注册
     *
     * @param request 账号密码注册请求
     * @return 注册响应
     */
    RegisterResponseDTO registerBackendAdminByPassword(PasswordRegisterReqDTO request);

    /**
     * 后台管理员 - 账号密码登录
     *
     * @param request 账号密码登录请求
     * @return 登录响应
     */
    LoginResponseDTO loginBackendAdminByPassword(PasswordLoginReqDTO request);
}
