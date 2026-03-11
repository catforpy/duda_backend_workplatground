package com.duda.common.security.service.impl;

import com.duda.common.dto.auth.LoginRequestDTO;
import com.duda.common.dto.auth.LoginResponseDTO;
import com.duda.common.dto.auth.RegisterRequestDTO;
import com.duda.common.dto.auth.RegisterResponseDTO;
import com.duda.common.enums.LoginType;
import com.duda.common.enums.ThirdPartyPlatform;
import com.duda.common.enums.UserType;
import com.duda.common.redis.idempotent.IdempotentHelper;
import com.duda.common.security.properties.JwtProperties;
import com.duda.common.security.service.TokenService;
import com.duda.common.security.util.JwtTokenProvider;
import com.duda.common.web.exception.BizException;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * 认证服务实现
 *
 * 实现4种身份 × 3种登录方式的注册和登录功能
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Service
public class AuthServiceImpl implements com.duda.common.security.service.AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private TokenService tokenService;

    @Resource
    private IdempotentHelper idempotentHelper;

    // TODO: 注入用户服务（需要从实际的 User Service 中注入）
    // @Autowired
    // private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        logger.info("用户注册开始，userType: {}, registerType: {}",
                request.getUserType(), request.getRegisterType());

        // 1. 验证请求参数
        request.validate();

        // 2. 防重复提交检查
        String idempotentKey = buildIdempotentKey("register", request);
        if (!idempotentHelper.checkAndSet(idempotentKey, 300)) {
            throw new BizException("请勿重复提交注册请求");
        }

        // 3. 检查用户身份是否合法
        UserType userType = UserType.fromCode(request.getUserType());

        // 4. 根据注册方式处理
        try {
            RegisterResponseDTO response;

            if ("phone".equals(request.getRegisterType())) {
                // 手机号注册
                response = registerByPhone(request, userType);
            } else if ("email".equals(request.getRegisterType())) {
                // 邮箱注册
                response = registerByEmail(request, userType);
            } else if ("third_party".equals(request.getRegisterType())) {
                // 第三方注册
                response = registerByThirdParty(request, userType);
            } else {
                throw new BizException("不支持的注册方式: " + request.getRegisterType());
            }

            logger.info("用户注册成功，userId: {}, userType: {}", response.getUserId(), response.getUserType());
            return response;

        } catch (Exception e) {
            // 注册失败，删除幂等key
            idempotentHelper.delete(idempotentKey);
            logger.error("用户注册失败，userType: {}, registerType: {}",
                    request.getUserType(), request.getRegisterType(), e);
            throw new BizException("注册失败: " + e.getMessage());
        }
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        logger.info("用户登录开始，loginType: {}, userType: {}",
                request.getLoginType(), request.getUserType());

        // 1. 验证请求参数
        request.validate();

        // 2. 防止暴力破解（限流）
        String rateLimitKey = buildRateLimitKey("login", request);
        // TODO: 实现限流逻辑
        // if (!rateLimiter.tryAcquire(rateLimitKey, 5, 60)) {
        //     throw new BizException("登录尝试次数过多，请稍后再试");
        // }

        // 3. 根据登录方式处理
        LoginResponseDTO response;

        try {
            LoginType loginType = LoginType.fromCode(request.getLoginType());

            if (loginType == LoginType.PHONE_PASSWORD) {
                // 手机号+密码登录
                response = loginByPhone(request);
            } else if (loginType == LoginType.EMAIL_PASSWORD) {
                // 邮箱+密码登录
                response = loginByEmail(request);
            } else if (loginType == LoginType.THIRD_PARTY) {
                // 第三方登录
                response = loginByThirdParty(request);
            } else {
                throw new BizException("不支持的登录方式: " + loginType);
            }

            logger.info("用户登录成功，userId: {}, userType: {}",
                    response.getUserId(), response.getUserType());
            return response;

        } catch (Exception e) {
            logger.error("用户登录失败，loginType: {}, userType: {}",
                    request.getLoginType(), request.getUserType(), e);
            throw new BizException("登录失败: " + e.getMessage());
        }
    }

    @Override
    public void logout(String accessToken, Long userId) {
        logger.info("用户登出，userId: {}", userId);
        tokenService.logout(accessToken, userId);
    }

    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {
        logger.info("刷新Token");

        // 使用 TokenService 刷新
        var tokenDTO = tokenService.refreshToken(
                new com.duda.common.security.dto.RefreshTokenReqDTO(refreshToken)
        );

        // TODO: 从数据库查询用户完整信息
        return LoginResponseDTO.builder()
                .accessToken(tokenDTO.getAccessToken())
                .refreshToken(tokenDTO.getRefreshToken())
                .tokenType(tokenDTO.getTokenType())
                .expiresIn(tokenDTO.getExpiresIn())
                .build();
    }

    @Override
    public boolean validateToken(String accessToken) {
        return tokenService.validateAccessToken(accessToken);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return tokenService.getUserIdFromToken(token);
    }

    @Override
    public String getUsernameFromToken(String token) {
        return tokenService.getUsernameFromToken(token);
    }

    @Override
    public String getUserTypeFromToken(String token) {
        return tokenService.getUserTypeFromToken(token);
    }

    // ==================== 私有方法 ====================

    /**
     * 手机号注册
     */
    private RegisterResponseDTO registerByPhone(RegisterRequestDTO request, UserType userType) {
        logger.info("手机号注册，phone: {}, userType: {}", request.getPhone(), userType);

        // 1. 验证手机验证码
        // TODO: 验证手机验证码
        // if (!verifyCodeService.verifyPhoneCode(request.getPhone(), request.getPhoneVerifyCode())) {
        //     throw new BizException("手机验证码错误或已过期");
        // }

        // 2. 检查手机号是否已注册
        // TODO: 查询数据库
        // if (userService.existsByPhone(request.getPhone())) {
        //     throw new BizException("该手机号已注册");
        // }

        // 3. 创建用户
        // UserPO user = new UserPO();
        // user.setPhone(request.getPhone());
        // user.setPassword(BCrypt.hashpw(request.getPassword()));
        // user.setUserType(userType.getCode());
        // user.setStatus("active");
        // userService.save(user);

        // TODO: 临时返回模拟数据
        Long userId = 10001L;

        // 4. 自动登录（可选）
        boolean autoLogin = true; // 可以根据配置决定
        String accessToken = null;
        String refreshToken = null;

        if (autoLogin) {
            var tokenDTO = tokenService.generateTokens(
                    userId,
                    request.getUsername() != null ? request.getUsername() : request.getPhone(),
                    userType.getCode()
            );
            accessToken = tokenDTO.getAccessToken();
            refreshToken = tokenDTO.getRefreshToken();
        }

        return RegisterResponseDTO.builder()
                .userId(userId)
                .username(request.getUsername())
                .userType(userType.getCode())
                .userTypeName(userType.getName())
                .phone(maskPhone(request.getPhone()))
                .status("active")
                .statusDesc("激活成功")
                .registerTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .needLogin(autoLogin)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .welcomeMessage("欢迎加入都达云台！")
                .build();
    }

    /**
     * 邮箱注册
     */
    private RegisterResponseDTO registerByEmail(RegisterRequestDTO request, UserType userType) {
        logger.info("邮箱注册，email: {}, userType: {}", request.getEmail(), userType);

        // 1. 验证邮箱验证码
        // TODO: 验证邮箱验证码

        // 2. 检查邮箱是否已注册
        // TODO: 查询数据库

        // 3. 创建用户
        // UserPO user = new UserPO();
        // user.setEmail(request.getEmail());
        // user.setPassword(BCrypt.hashpw(request.getPassword()));
        // user.setUserType(userType.getCode());
        // user.setStatus("active");
        // userService.save(user);

        // TODO: 临时返回模拟数据
        Long userId = 10002L;

        boolean autoLogin = true;
        String accessToken = null;
        String refreshToken = null;

        if (autoLogin) {
            var tokenDTO = tokenService.generateTokens(
                    userId,
                    request.getUsername() != null ? request.getUsername() : request.getEmail(),
                    userType.getCode()
            );
            accessToken = tokenDTO.getAccessToken();
            refreshToken = tokenDTO.getRefreshToken();
        }

        return RegisterResponseDTO.builder()
                .userId(userId)
                .username(request.getUsername())
                .userType(userType.getCode())
                .userTypeName(userType.getName())
                .email(maskEmail(request.getEmail()))
                .status("active")
                .statusDesc("激活成功")
                .registerTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .needLogin(autoLogin)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .welcomeMessage("欢迎加入都达云台！")
                .build();
    }

    /**
     * 第三方注册
     */
    private RegisterResponseDTO registerByThirdParty(RegisterRequestDTO request, UserType userType) {
        logger.info("第三方注册，platform: {}, openId: {}, userType: {}",
                request.getThirdPartyPlatform(), request.getThirdPartyOpenId(), userType);

        // 1. 验证第三方授权
        // TODO: 验证第三方授权码

        // 2. 检查是否已绑定
        // TODO: 查询数据库

        // 3. 创建用户并绑定第三方账号
        // UserPO user = new UserPO();
        // user.setUsername(request.getUsername());
        // user.setRealName(request.getThirdPartyNickname());
        // user.setAvatar(request.getThirdPartyAvatar());
        // user.setUserType(userType.getCode());
        // user.setStatus("active");
        // userService.save(user);

        // // 创建第三方绑定关系
        // UserThirdPartyPO thirdParty = new UserThirdPartyPO();
        // thirdParty.setUserId(user.getId());
        // thirdParty.setPlatform(request.getThirdPartyPlatform());
        // thirdParty.setOpenId(request.getThirdPartyOpenId());
        // thirdParty.setUnionId(request.getThirdPartyUnionId());
        // userThirdPartyService.save(thirdParty);

        // TODO: 临时返回模拟数据
        Long userId = 10003L;

        boolean autoLogin = true;
        String accessToken = null;
        String refreshToken = null;

        if (autoLogin) {
            var tokenDTO = tokenService.generateTokens(
                    userId,
                    request.getUsername() != null ? request.getUsername() : request.getThirdPartyNickname(),
                    userType.getCode()
            );
            accessToken = tokenDTO.getAccessToken();
            refreshToken = tokenDTO.getRefreshToken();
        }

        return RegisterResponseDTO.builder()
                .userId(userId)
                .username(request.getUsername())
                .userType(userType.getCode())
                .userTypeName(userType.getName())
                .realName(request.getThirdPartyNickname())
                .avatar(request.getThirdPartyAvatar())
                .status("active")
                .statusDesc("激活成功")
                .registerTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .needLogin(autoLogin)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .welcomeMessage("欢迎加入都达云台！")
                .build();
    }

    /**
     * 手机号+密码登录
     */
    private LoginResponseDTO loginByPhone(LoginRequestDTO request) {
        logger.info("手机号+密码登录，phone: {}", request.getPhone());

        // 1. 查询用户
        // UserPO user = userService.getByPhone(request.getPhone());
        // if (user == null) {
        //     throw new BizException("手机号或密码错误");
        // }

        // 2. 验证密码
        // if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
        //     throw new BizException("手机号或密码错误");
        // }

        // 3. 检查用户状态
        // if (!"active".equals(user.getStatus())) {
        //     throw new BizException("账号已被" + getStatusDesc(user.getStatus()));
        // }

        // TODO: 临时返回模拟数据
        Long userId = 10001L;
        String username = "user_" + request.getPhone().substring(7);
        String userTypeCode = request.getUserType() != null ? request.getUserType() : UserType.NORMAL.getCode();
        UserType userType = UserType.fromCode(userTypeCode);

        // 4. 生成Token
        var tokenDTO = tokenService.generateTokens(userId, username, userTypeCode);

        return LoginResponseDTO.builder()
                .accessToken(tokenDTO.getAccessToken())
                .refreshToken(tokenDTO.getRefreshToken())
                .tokenType(tokenDTO.getTokenType())
                .expiresIn(tokenDTO.getExpiresIn())
                .userId(userId)
                .username(username)
                .userType(userTypeCode)
                .userTypeName(userType.getName())
                .phone(maskPhone(request.getPhone()))
                .status("active")
                .statusDesc("正常")
                .isFirstLogin(false)
                .needCompleteInfo(false)
                .loginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    /**
     * 邮箱+密码登录
     */
    private LoginResponseDTO loginByEmail(LoginRequestDTO request) {
        logger.info("邮箱+密码登录，email: {}", request.getEmail());

        // TODO: 实现邮箱登录逻辑
        // 类似手机号登录

        // TODO: 临时返回模拟数据
        Long userId = 10002L;
        String username = "user_" + request.getEmail().split("@")[0];
        String userTypeCode = request.getUserType() != null ? request.getUserType() : UserType.NORMAL.getCode();
        UserType userType = UserType.fromCode(userTypeCode);

        var tokenDTO = tokenService.generateTokens(userId, username, userTypeCode);

        return LoginResponseDTO.builder()
                .accessToken(tokenDTO.getAccessToken())
                .refreshToken(tokenDTO.getRefreshToken())
                .tokenType(tokenDTO.getTokenType())
                .expiresIn(tokenDTO.getExpiresIn())
                .userId(userId)
                .username(username)
                .userType(userTypeCode)
                .userTypeName(userType.getName())
                .email(maskEmail(request.getEmail()))
                .status("active")
                .statusDesc("正常")
                .isFirstLogin(false)
                .needCompleteInfo(false)
                .loginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    /**
     * 第三方登录
     */
    private LoginResponseDTO loginByThirdParty(LoginRequestDTO request) {
        logger.info("第三方登录，platform: {}, openId: {}",
                request.getThirdPartyPlatform(), request.getThirdPartyOpenId());

        // 1. 验证第三方授权
        ThirdPartyPlatform platform = ThirdPartyPlatform.fromCode(request.getThirdPartyPlatform());

        // 2. 查询是否已绑定账号
        // UserThirdPartyPO thirdParty = userThirdPartyService.getByPlatformAndOpenId(
        //         platform.getCode(), request.getThirdPartyOpenId()
        // );

        // 3. 如果已绑定，直接登录
        // if (thirdParty != null) {
        //     UserPO user = userService.getById(thirdParty.getUserId());
        //     // 生成Token...
        // }

        // 4. 如果未绑定，创建新账号
        // ...

        // TODO: 临时返回模拟数据
        Long userId = 10003L;
        String username = "wechat_user_" + System.currentTimeMillis() % 10000;
        String userTypeCode = request.getUserType() != null ? request.getUserType() : UserType.NORMAL.getCode();
        UserType userType = UserType.fromCode(userTypeCode);

        var tokenDTO = tokenService.generateTokens(userId, username, userTypeCode);

        return LoginResponseDTO.builder()
                .accessToken(tokenDTO.getAccessToken())
                .refreshToken(tokenDTO.getRefreshToken())
                .tokenType(tokenDTO.getTokenType())
                .expiresIn(tokenDTO.getExpiresIn())
                .userId(userId)
                .username(username)
                .userType(userTypeCode)
                .userTypeName(userType.getName())
                .status("active")
                .statusDesc("正常")
                .isFirstLogin(true)
                .needCompleteInfo(true)
                .loginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    // ==================== 工具方法 ====================

    /**
     * 构建幂等key
     */
    private String buildIdempotentKey(String operation, RegisterRequestDTO request) {
        if ("phone".equals(request.getRegisterType())) {
            return operation + ":phone:" + request.getPhone();
        } else if ("email".equals(request.getRegisterType())) {
            return operation + ":email:" + request.getEmail();
        } else {
            return operation + ":third:" + request.getThirdPartyPlatform() + ":" + request.getThirdPartyOpenId();
        }
    }

    /**
     * 构建限流key
     */
    private String buildRateLimitKey(String operation, LoginRequestDTO request) {
        LoginType loginType = LoginType.fromCode(request.getLoginType());
        if (loginType == LoginType.PHONE_PASSWORD) {
            return operation + ":phone:" + request.getPhone();
        } else if (loginType == LoginType.EMAIL_PASSWORD) {
            return operation + ":email:" + request.getEmail();
        } else {
            return operation + ":ip:" + request.getClientIp();
        }
    }

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
     * 获取状态描述
     */
    private String getStatusDesc(String status) {
        switch (status) {
            case "active":
                return "正常";
            case "inactive":
                return "未激活";
            case "suspended":
                return "已暂停";
            case "deleted":
                return "已删除";
            default:
                return "未知";
        }
    }
}
