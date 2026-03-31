package com.duda.user.api.service.impl;

import com.duda.common.dto.auth.LoginRequestDTO;
import com.duda.common.dto.auth.LoginResponseDTO;
import com.duda.common.dto.auth.RegisterRequestDTO;
import com.duda.common.dto.auth.RegisterResponseDTO;
import com.duda.common.mq.MqTopicConstants;
import com.duda.common.mq.message.UserLoginMsg;
import com.duda.common.mq.message.UserRegisterMsg;
import com.duda.common.rocketmq.RocketMQUtils;
import com.duda.common.security.dto.TokenDTO;
import com.duda.common.security.service.TokenService;
import com.duda.msg.enums.MsgSendResultEnum;
import com.duda.msg.rpc.ISmsRpc;
import com.duda.user.api.service.IAuthService;
import com.duda.user.dto.UserDTO;
import com.duda.user.rpc.IUserRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Service
public class AuthServiceImpl implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Resource
    private TokenService tokenService;

    @Resource
    private RocketMQUtils rocketMQUtils;

    @DubboReference(
            version = "1.0.0",
            group = "USER_GROUP",
            check = false
    )
    private IUserRpc userRpc;

    @DubboReference(
            version = "1.0.0",
            group = "MSG_GROUP",
            registry = "msgRegistry",
            check = false
    )
    private ISmsRpc smsRpc;

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        // 1. 验证请求参数
        request.validate();

        // 2. 调用RPC服务注册
        Long userId = userRpc.register(convertToOldRegisterDTO(request));

        // 3. 查询用户信息
        UserDTO userDTO = userRpc.getUserById(userId);

        // 4. 组装响应（不自动生成Token，需要用户重新登录）
        RegisterResponseDTO response = RegisterResponseDTO.builder()
                .userId(userDTO.getId())
                .username(userDTO.getUsername())
                .userType(userDTO.getUserType())
                .userTypeName(userDTO.getUserType()) // TODO: 从枚举获取名称
                .realName(userDTO.getRealName())
                .phone(maskPhone(userDTO.getPhone()))
                .email(maskEmail(userDTO.getEmail()))
                .avatar(userDTO.getAvatar())
                .status(userDTO.getStatus())
                .statusDesc("注册成功，请登录")
                .needLogin(true)
                .welcomeMessage("注册成功！请使用您的账号和密码登录。")
                .build();

        // 6. 发送注册 MQ 消息（异步处理：欢迎邮件、初始化数据等）
        UserRegisterMsg registerMsg = new UserRegisterMsg();
        registerMsg.setUserId(userDTO.getId());
        registerMsg.setUsername(userDTO.getUsername());
        registerMsg.setUserType(userDTO.getUserType());
        registerMsg.setRealName(userDTO.getRealName());
        registerMsg.setPhone(request.getPhone());
        registerMsg.setEmail(userDTO.getEmail());
        registerMsg.setRegisterTime(java.time.LocalDateTime.now().toString());
        registerMsg.setRegisterIp(request.getClientIp());
        registerMsg.setRegisterType(request.getRegisterType());
        registerMsg.setInviteCode(request.getInviteCode());

        rocketMQUtils.asyncSendWithKey(
            MqTopicConstants.USER_REGISTER,
            registerMsg,
            RocketMQUtils.buildMessageKey("user-register", userDTO.getId())
        );

        logger.info("用户注册成功，userId:{}, 已发送注册MQ消息", userDTO.getId());
        return response;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. 验证请求参数
        request.validate();

        // 2. 如果是手机验证码登录，先验证验证码
        if ("phone_sms".equals(request.getLoginType())) {
            logger.info("【登录】手机验证码登录，phone={}", request.getPhone());

            // 调用 MSG Provider 验证码校验
            com.duda.msg.dto.MsgCheckDTO checkResult = smsRpc.checkLoginCode(
                    request.getPhone(),
                    Integer.parseInt(request.getPhoneVerifyCode())
            );

            if (!checkResult.isCheckStatus()) {
                logger.warn("【登录】验证码校验失败，phone={}, reason={}",
                        request.getPhone(), checkResult.getDesc());
                throw new RuntimeException("验证码校验失败：" + checkResult.getDesc());
            }

            logger.info("【登录】验证码校验成功，phone={}", request.getPhone());
        }

        // 3. 调用RPC服务登录
        UserDTO userDTO = userRpc.login(convertToOldLoginDTO(request));

        // 3.1 ⭐ 校验用户类型（防止权限越界）
        if (request.getUserType() != null && !request.getUserType().equals(userDTO.getUserType())) {
            logger.error("【登录】用户类型不匹配！请求类型={}，实际类型={}，username={}",
                    request.getUserType(), userDTO.getUserType(), userDTO.getUsername());
            throw new IllegalArgumentException("账号类型不匹配，请使用正确的登录入口");
        }

        // 4. 生成Token
        TokenDTO tokenDTO = tokenService.generateTokens(
                userDTO.getId(),
                userDTO.getUsername(),
                userDTO.getUserType()
        );

        // 5. 组装响应
        LoginResponseDTO response = LoginResponseDTO.builder()
                .accessToken(tokenDTO.getAccessToken())
                .refreshToken(tokenDTO.getRefreshToken())
                .tokenType(tokenDTO.getTokenType())
                .expiresIn(tokenDTO.getExpiresIn() != null ? tokenDTO.getExpiresIn().longValue() : null)
                .userId(userDTO.getId())
                .username(userDTO.getUsername())
                .userType(userDTO.getUserType())
                .userTypeName(userDTO.getUserType()) // TODO: 从枚举获取名称
                .realName(userDTO.getRealName())
                .avatar(userDTO.getAvatar())
                .phone(maskPhone(userDTO.getPhone()))
                .email(maskEmail(userDTO.getEmail()))
                .status(userDTO.getStatus())
                .statusDesc("正常")
                .isFirstLogin(userDTO.getLastLoginTime() == null)
                .needCompleteInfo(false)
                .loginTime(userDTO.getLastLoginTime() != null ?
                    userDTO.getLastLoginTime().toString() : null)
                .build();

        // 6. 发送登录 MQ 消息（异步记录日志、风控检测等）
        UserLoginMsg loginMsg = new UserLoginMsg();
        loginMsg.setUserId(userDTO.getId());
        loginMsg.setUsername(userDTO.getUsername());
        loginMsg.setUserType(userDTO.getUserType());
        loginMsg.setLoginTime(java.time.LocalDateTime.now().toString());
        loginMsg.setLoginIp(request.getClientIp());
        loginMsg.setClientType(request.getClientType());
        loginMsg.setDeviceId(request.getDeviceId());
        loginMsg.setIsFirstLogin(userDTO.getLastLoginTime() == null);
        loginMsg.setLoginType(request.getLoginType());

        rocketMQUtils.asyncSendWithKey(
            MqTopicConstants.USER_LOGIN,
            loginMsg,
            RocketMQUtils.buildMessageKey("user-login", userDTO.getId())
        );

        logger.info("用户登录成功，userId:{}, username:{}, 已发送登录MQ消息",
            userDTO.getId(), userDTO.getUsername());
        return response;
    }

    @Override
    public void logout(String authorization, Long userId) {
        // 移除 "Bearer " 前缀
        String accessToken = authorization.replace("Bearer ", "");
        tokenService.logout(accessToken, userId);
    }

    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {
        var refreshReq = new com.duda.common.security.dto.RefreshTokenReqDTO();
        refreshReq.setRefreshToken(refreshToken);
        var tokenDTO = tokenService.refreshToken(refreshReq);

        return LoginResponseDTO.builder()
                .accessToken(tokenDTO.getAccessToken())
                .refreshToken(tokenDTO.getRefreshToken())
                .tokenType(tokenDTO.getTokenType())
                .expiresIn(tokenDTO.getExpiresIn() != null ? tokenDTO.getExpiresIn().longValue() : null)
                .build();
    }

    @Override
    public boolean validateToken(String authorization) {
        String accessToken = authorization.replace("Bearer ", "");
        return tokenService.validateAccessToken(accessToken);
    }

    @Override
    public Object getUserInfo(String authorization) {
        String accessToken = authorization.replace("Bearer ", "");
        Long userId = tokenService.getUserIdFromToken(accessToken);
        return userRpc.getUserById(userId);
    }

    // ==================== 私有方法 ====================

    /**
     * 脱敏手机号
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 脱敏邮箱
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        if (username.length() <= 3) {
            return username.charAt(0) + "***@" + parts[1];
        }
        return username.substring(0, 1) + "***" + username.substring(username.length() - 1) + "@" + parts[1];
    }

    /**
     * 转换为旧的注册DTO（兼容现有RPC接口）
     *
     * 新旧映射关系：
     * - account (都达网账号) → username + password + phone + realName
     * - phone_sms (手机号验证码) → username(从phone生成) + password(随机生成) + phone + realName
     * - wechat_scan (微信扫码) → username(从openId生成) + password(随机生成) + realName(昵称)
     */
    private com.duda.user.dto.UserRegisterReqDTO convertToOldRegisterDTO(RegisterRequestDTO request) {
        com.duda.user.dto.UserRegisterReqDTO oldDTO = new com.duda.user.dto.UserRegisterReqDTO();

        // 设置用户类型
        oldDTO.setUserType(request.getUserType() != null ? request.getUserType() : "normal");

        String registerType = request.getRegisterType();

        if ("account".equals(registerType)) {
            // 方式1：都达网账号注册
            oldDTO.setUsername(request.getUsername());
            oldDTO.setPassword(request.getPassword());
            oldDTO.setPhone(request.getPhone()); // 可选
            oldDTO.setRealName(request.getRealName());

        } else if ("phone_sms".equals(registerType)) {
            // 方式2：手机号验证码注册
            // 旧接口需要username和password，从手机号生成
            oldDTO.setUsername(request.getPhone()); // 使用手机号作为username
            oldDTO.setPassword(generateRandomPassword()); // 生成随机密码
            oldDTO.setPhone(request.getPhone());
            oldDTO.setRealName(request.getRealName());

        } else if ("wechat_scan".equals(registerType)) {
            // 方式3：微信扫码注册
            // 旧接口需要username和password，从openId生成
            oldDTO.setUsername("wx_" + request.getThirdPartyOpenId()); // 使用wx_前缀+openId作为username
            oldDTO.setPassword(generateRandomPassword()); // 生成随机密码
            oldDTO.setRealName(request.getThirdPartyNickname());
            // 微信用户可能没有手机号，先不设置

        } else {
            throw new IllegalArgumentException("不支持的注册方式: " + registerType);
        }

        return oldDTO;
    }

    /**
     * 转换为旧的登录DTO（兼容现有RPC接口）
     *
     * 新旧映射关系：
     * - account_password (都达网账号) → username + password ✓ 支持
     * - phone_sms (手机号验证码) → phone作为username + 验证码作为password ✓ 兼容
     * - wechat_scan (微信扫码) → wx_openId作为username ✓ 兼容
     */
    private com.duda.user.dto.UserLoginReqDTO convertToOldLoginDTO(LoginRequestDTO request) {
        com.duda.user.dto.UserLoginReqDTO oldDTO = new com.duda.user.dto.UserLoginReqDTO();

        String loginType = request.getLoginType();

        if ("account_password".equals(loginType)) {
            // 方式1：都达网账号登录
            oldDTO.setUsername(request.getUsername());
            oldDTO.setPassword(request.getPassword());

        } else if ("phone_sms".equals(loginType)) {
            // 方式2：手机号验证码登录
            // 注册时使用手机号作为username，这里也用手机号
            // 注意：这里password字段存储的是验证码，需要在RPC层特殊处理
            oldDTO.setUsername(request.getPhone());
            oldDTO.setPassword(request.getPhoneVerifyCode()); // 验证码临时作为password传递

        } else if ("wechat_scan".equals(loginType)) {
            // 方式3：微信扫码登录
            // 注册时使用 wx_openId 作为username
            oldDTO.setUsername("wx_" + request.getThirdPartyOpenId());
            oldDTO.setPassword(""); // 微信登录不需要密码

        } else {
            throw new IllegalArgumentException("不支持的登录方式: " + loginType);
        }

        return oldDTO;
    }

    /**
     * 生成随机密码（用于手机号验证码注册和微信扫码注册）
     */
    private String generateRandomPassword() {
        // 生成16位随机密码（包含大小写字母和数字）
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }

    @Override
    public void sendSmsCode(String phone) {
        // 调用 MSG Provider RPC 发送验证码
        logger.info("【发送验证码】调用 MSG Provider，phone={}", phone);

        com.duda.msg.enums.MsgSendResultEnum result = smsRpc.sendLoginCode(phone);

        if (result != com.duda.msg.enums.MsgSendResultEnum.SEND_SUCCESS) {
            throw new RuntimeException("验证码发送失败：" + result.getDesc());
        }

        logger.info("【发送验证码】成功，phone={}", phone);
    }

    // ==================== V2：明确区分身份和登录方式的实现 ====================

    /**
     * 都达网账户 - 账号密码注册
     */
    @Override
    public RegisterResponseDTO registerPlatformAccountByPassword(
            com.duda.common.dto.auth.PasswordRegisterReqDTO request) {

        // 1. 验证密码一致性
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }

        // 2. 调用通用注册方法，指定身份类型
        RegisterRequestDTO genericRequest = new RegisterRequestDTO();
        genericRequest.setUserType("platform_account");
        genericRequest.setRegisterType("account");
        genericRequest.setUsername(request.getUsername());
        genericRequest.setPassword(request.getPassword());
        genericRequest.setConfirmPassword(request.getConfirmPassword());  // ⚠️ 重要：设置确认密码
        genericRequest.setRealName(request.getRealName());
        genericRequest.setPhone(request.getPhone());
        genericRequest.setInviteCode(request.getInviteCode());

        return register(genericRequest);
    }

    /**
     * 都达网账户 - 账号密码登录
     */
    @Override
    public LoginResponseDTO loginPlatformAccountByPassword(
            com.duda.common.dto.auth.PasswordLoginReqDTO request) {

        // 调用通用登录方法
        LoginRequestDTO genericRequest = new LoginRequestDTO();
        genericRequest.setLoginType("account_password");
        genericRequest.setUserType("platform_account");
        genericRequest.setUsername(request.getUsername());
        genericRequest.setPassword(request.getPassword());
        genericRequest.setClientIp(request.getClientIp());
        genericRequest.setClientType(request.getClientType());
        genericRequest.setDeviceId(request.getDeviceId());

        return login(genericRequest);
    }

    /**
     * 都达网账户 - 手机验证码登录
     */
    @Override
    public LoginResponseDTO loginPlatformAccountBySms(
            com.duda.common.dto.auth.SmsCodeLoginReqDTO request) {

        // 调用通用登录方法
        LoginRequestDTO genericRequest = new LoginRequestDTO();
        genericRequest.setLoginType("phone_sms");
        genericRequest.setUserType("platform_account");
        genericRequest.setPhone(request.getPhone());
        genericRequest.setPhoneVerifyCode(request.getSmsCode());
        genericRequest.setClientIp(request.getClientIp());
        genericRequest.setClientType(request.getClientType());
        genericRequest.setDeviceId(request.getDeviceId());

        return login(genericRequest);
    }

    /**
     * 服务商 - 账号密码注册
     */
    @Override
    public RegisterResponseDTO registerServiceProviderByPassword(
            com.duda.common.dto.auth.PasswordRegisterReqDTO request) {

        // 1. 验证密码一致性
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }

        // 2. 调用通用注册方法
        RegisterRequestDTO genericRequest = new RegisterRequestDTO();
        genericRequest.setUserType("service_provider");
        genericRequest.setRegisterType("account");
        genericRequest.setUsername(request.getUsername());
        genericRequest.setPassword(request.getPassword());
        genericRequest.setConfirmPassword(request.getConfirmPassword());
        genericRequest.setRealName(request.getRealName());
        genericRequest.setPhone(request.getPhone());
        genericRequest.setInviteCode(request.getInviteCode());

        return register(genericRequest);
    }

    /**
     * 服务商 - 账号密码登录
     */
    @Override
    public LoginResponseDTO loginServiceProviderByPassword(
            com.duda.common.dto.auth.PasswordLoginReqDTO request) {

        LoginRequestDTO genericRequest = new LoginRequestDTO();
        genericRequest.setLoginType("account_password");
        genericRequest.setUserType("service_provider");
        genericRequest.setUsername(request.getUsername());
        genericRequest.setPassword(request.getPassword());  // ⭐ 添加密码
        genericRequest.setClientIp(request.getClientIp());
        genericRequest.setClientType(request.getClientType());
        genericRequest.setDeviceId(request.getDeviceId());

        return login(genericRequest);
    }

    /**
     * 服务商 - 手机验证码登录
     */
    @Override
    public LoginResponseDTO loginServiceProviderBySms(
            com.duda.common.dto.auth.SmsCodeLoginReqDTO request) {

        LoginRequestDTO genericRequest = new LoginRequestDTO();
        genericRequest.setLoginType("phone_sms");
        genericRequest.setUserType("service_provider");
        genericRequest.setPhone(request.getPhone());
        genericRequest.setPhoneVerifyCode(request.getSmsCode());
        genericRequest.setClientIp(request.getClientIp());
        genericRequest.setClientType(request.getClientType());
        genericRequest.setDeviceId(request.getDeviceId());

        return login(genericRequest);
    }

    /**
     * 平台管理员 - 账号密码注册
     */
    @Override
    public RegisterResponseDTO registerPlatformAdminByPassword(
            com.duda.common.dto.auth.PasswordRegisterReqDTO request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }

        RegisterRequestDTO genericRequest = new RegisterRequestDTO();
        genericRequest.setUserType("platform_admin");
        genericRequest.setRegisterType("account");
        genericRequest.setUsername(request.getUsername());
        genericRequest.setPassword(request.getPassword());
        genericRequest.setConfirmPassword(request.getConfirmPassword());
        genericRequest.setRealName(request.getRealName());
        genericRequest.setPhone(request.getPhone());

        return register(genericRequest);
    }

    /**
     * 平台管理员 - 账号密码登录
     */
    @Override
    public LoginResponseDTO loginPlatformAdminByPassword(
            com.duda.common.dto.auth.PasswordLoginReqDTO request) {

        LoginRequestDTO genericRequest = new LoginRequestDTO();
        genericRequest.setLoginType("account_password");
        genericRequest.setUserType("platform_admin");
        genericRequest.setUsername(request.getUsername());
        genericRequest.setPassword(request.getPassword());  // ⭐ 添加密码
        genericRequest.setClientIp(request.getClientIp());
        genericRequest.setClientType(request.getClientType());
        genericRequest.setDeviceId(request.getDeviceId());

        return login(genericRequest);
    }

    /**
     * 后台管理员 - 账号密码注册
     */
    @Override
    public RegisterResponseDTO registerBackendAdminByPassword(
            com.duda.common.dto.auth.PasswordRegisterReqDTO request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }

        RegisterRequestDTO genericRequest = new RegisterRequestDTO();
        genericRequest.setUserType("backend_admin");
        genericRequest.setRegisterType("account");
        genericRequest.setUsername(request.getUsername());
        genericRequest.setPassword(request.getPassword());
        genericRequest.setConfirmPassword(request.getConfirmPassword());
        genericRequest.setRealName(request.getRealName());
        genericRequest.setPhone(request.getPhone());

        return register(genericRequest);
    }

    /**
     * 后台管理员 - 账号密码登录
     */
    @Override
    public LoginResponseDTO loginBackendAdminByPassword(
            com.duda.common.dto.auth.PasswordLoginReqDTO request) {

        LoginRequestDTO genericRequest = new LoginRequestDTO();
        genericRequest.setLoginType("account_password");
        genericRequest.setUserType("backend_admin");
        genericRequest.setUsername(request.getUsername());
        genericRequest.setPassword(request.getPassword());  // ⭐ 添加密码
        genericRequest.setClientIp(request.getClientIp());
        genericRequest.setClientType(request.getClientType());
        genericRequest.setDeviceId(request.getDeviceId());

        return login(genericRequest);
    }
}
