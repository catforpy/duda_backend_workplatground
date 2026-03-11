package com.duda.common.security;

import com.duda.common.dto.auth.LoginRequestDTO;
import com.duda.common.dto.auth.LoginResponseDTO;
import com.duda.common.dto.auth.RegisterRequestDTO;
import com.duda.common.dto.auth.RegisterResponseDTO;
import com.duda.common.enums.LoginType;
import com.duda.common.enums.UserType;
import com.duda.common.security.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证服务测试
 *
 * 测试4种身份 × 3种登录方式的注册和登录功能
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@SpringBootTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        // 测试前准备
    }

    // ==================== 注册测试 ====================

    @Test
    @DisplayName("测试手机号注册 - 普通用户")
    public void testRegisterByPhone_NormalUser() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType(UserType.NORMAL.getCode());
        request.setRegisterType("phone");
        request.setUsername("testuser001");
        request.setPassword("pass123456");
        request.setConfirmPassword("pass123456");
        request.setPhone("13800138001");
        request.setPhoneVerifyCode("123456");
        request.setClientIp("192.168.1.1");

        RegisterResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getUserId());
        assertEquals(UserType.NORMAL.getCode(), response.getUserType());
        assertEquals("普通用户", response.getUserTypeName());
        assertEquals("138****8001", response.getPhone());
        assertEquals("active", response.getStatus());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        System.out.println("✅ 手机号注册 - 普通用户测试通过");
    }

    @Test
    @DisplayName("测试手机号注册 - 商家")
    public void testRegisterByPhone_Merchant() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType(UserType.MERCHANT.getCode());
        request.setRegisterType("phone");
        request.setUsername("merchant001");
        request.setPassword("pass123456");
        request.setConfirmPassword("pass123456");
        request.setPhone("13800138002");
        request.setPhoneVerifyCode("123456");
        request.setRealName("测试商家");
        request.setClientIp("192.168.1.1");

        RegisterResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertEquals(UserType.MERCHANT.getCode(), response.getUserType());
        assertEquals("商家用户", response.getUserTypeName());

        System.out.println("✅ 手机号注册 - 商家测试通过");
    }

    @Test
    @DisplayName("测试邮箱注册 - 普通用户")
    public void testRegisterByEmail_NormalUser() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType(UserType.NORMAL.getCode());
        request.setRegisterType("email");
        request.setUsername("testuser002");
        request.setPassword("pass123456");
        request.setConfirmPassword("pass123456");
        request.setEmail("testuser002@example.com");
        request.setEmailVerifyCode("123456");
        request.setClientIp("192.168.1.1");

        RegisterResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertEquals(UserType.NORMAL.getCode(), response.getUserType());
        assertTrue(response.getEmail().contains("***"));

        System.out.println("✅ 邮箱注册 - 普通用户测试通过");
    }

    @Test
    @DisplayName("测试第三方注册 - 普通用户")
    public void testRegisterByThirdParty_NormalUser() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType(UserType.NORMAL.getCode());
        request.setRegisterType("third_party");
        request.setThirdPartyPlatform("wechat");
        request.setThirdPartyOpenId("oXXXXXXXXXXXXXXXX001");
        request.setThirdPartyNickname("微信用户001");
        request.setThirdPartyAvatar("https://example.com/avatar.jpg");
        request.setClientIp("192.168.1.1");

        RegisterResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertEquals(UserType.NORMAL.getCode(), response.getUserType());
        assertEquals("微信用户001", response.getRealName());

        System.out.println("✅ 第三方注册 - 普通用户测试通过");
    }

    // ==================== 登录测试 ====================

    @Test
    @DisplayName("测试手机号密码登录 - 普通用户")
    public void testLoginByPhonePassword_NormalUser() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType(LoginType.PHONE_PASSWORD.getCode());
        request.setUserType(UserType.NORMAL.getCode());
        request.setPhone("13800138001");
        request.setPassword("pass123456");
        request.setClientIp("192.168.1.1");

        LoginResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getUserId());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900L, response.getExpiresIn());
        assertEquals("active", response.getStatus());

        System.out.println("✅ 手机号密码登录 - 普通用户测试通过");
    }

    @Test
    @DisplayName("测试手机号密码登录 - 商家")
    public void testLoginByPhonePassword_Merchant() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType(LoginType.PHONE_PASSWORD.getCode());
        request.setUserType(UserType.MERCHANT.getCode());
        request.setPhone("13800138002");
        request.setPassword("pass123456");
        request.setClientIp("192.168.1.1");

        LoginResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals(UserType.MERCHANT.getCode(), response.getUserType());
        assertEquals("商家用户", response.getUserTypeName());

        System.out.println("✅ 手机号密码登录 - 商家测试通过");
    }

    @Test
    @DisplayName("测试邮箱密码登录 - 普通用户")
    public void testLoginByEmailPassword_NormalUser() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType(LoginType.EMAIL_PASSWORD.getCode());
        request.setUserType(UserType.NORMAL.getCode());
        request.setEmail("testuser002@example.com");
        request.setPassword("pass123456");
        request.setClientIp("192.168.1.1");

        LoginResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertTrue(response.getEmail().contains("***"));

        System.out.println("✅ 邮箱密码登录 - 普通用户测试通过");
    }

    @Test
    @DisplayName("测试第三方登录 - 普通用户")
    public void testLoginByThirdParty_NormalUser() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType(LoginType.THIRD_PARTY.getCode());
        request.setUserType(UserType.NORMAL.getCode());
        request.setThirdPartyPlatform("wechat");
        request.setThirdPartyAuthCode("auth_code_xxx");
        request.setThirdPartyOpenId("oXXXXXXXXXXXXXXXX001");
        request.setClientIp("192.168.1.1");

        LoginResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertTrue(response.getIsFirstLogin());
        assertTrue(response.getNeedCompleteInfo());

        System.out.println("✅ 第三方登录 - 普通用户测试通过");
    }

    @Test
    @DisplayName("测试Token刷新")
    public void testRefreshToken() {
        // 先登录获取Token
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setLoginType(LoginType.PHONE_PASSWORD.getCode());
        loginRequest.setUserType(UserType.NORMAL.getCode());
        loginRequest.setPhone("13800138001");
        loginRequest.setPassword("pass123456");
        loginRequest.setClientIp("192.168.1.1");

        LoginResponseDTO loginResponse = authService.login(loginRequest);
        assertNotNull(loginResponse.getRefreshToken());

        // 刷新Token
        LoginResponseDTO refreshResponse = authService.refreshToken(loginResponse.getRefreshToken());

        assertNotNull(refreshResponse);
        assertNotNull(refreshResponse.getAccessToken());
        assertNotNull(refreshResponse.getRefreshToken());
        assertEquals("Bearer", refreshResponse.getTokenType());

        System.out.println("✅ Token刷新测试通过");
    }

    @Test
    @DisplayName("测试Token验证")
    public void testValidateToken() {
        // 先登录获取Token
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setLoginType(LoginType.PHONE_PASSWORD.getCode());
        loginRequest.setUserType(UserType.NORMAL.getCode());
        loginRequest.setPhone("13800138001");
        loginRequest.setPassword("pass123456");
        loginRequest.setClientIp("192.168.1.1");

        LoginResponseDTO loginResponse = authService.login(loginRequest);
        String accessToken = loginResponse.getAccessToken();

        // 验证Token
        boolean valid = authService.validateToken(accessToken);
        assertTrue(valid);

        System.out.println("✅ Token验证测试通过");
    }

    @Test
    @DisplayName("测试从Token获取用户信息")
    public void testGetUserInfoFromToken() {
        // 先登录获取Token
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setLoginType(LoginType.PHONE_PASSWORD.getCode());
        loginRequest.setUserType(UserType.NORMAL.getCode());
        loginRequest.setPhone("13800138001");
        loginRequest.setPassword("pass123456");
        loginRequest.setClientIp("192.168.1.1");

        LoginResponseDTO loginResponse = authService.login(loginRequest);
        String accessToken = loginResponse.getAccessToken();

        // 从Token获取用户信息
        Long userId = authService.getUserIdFromToken(accessToken);
        String username = authService.getUsernameFromToken(accessToken);
        String userType = authService.getUserTypeFromToken(accessToken);

        assertNotNull(userId);
        assertNotNull(username);
        assertNotNull(userType);

        System.out.println("✅ 从Token获取用户信息测试通过");
    }

    // ==================== 综合场景测试 ====================

    @Test
    @DisplayName("测试完整的注册-登录-登出流程")
    public void testCompleteFlow() {
        // 1. 注册
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUserType(UserType.NORMAL.getCode());
        registerRequest.setRegisterType("phone");
        registerRequest.setUsername("flowtest001");
        registerRequest.setPassword("pass123456");
        registerRequest.setConfirmPassword("pass123456");
        registerRequest.setPhone("13900139001");
        registerRequest.setPhoneVerifyCode("123456");

        RegisterResponseDTO registerResponse = authService.register(registerRequest);
        assertNotNull(registerResponse.getUserId());
        System.out.println("注册成功，userId: " + registerResponse.getUserId());

        // 2. 登录
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setLoginType(LoginType.PHONE_PASSWORD.getCode());
        loginRequest.setPhone("13900139001");
        loginRequest.setPassword("pass123456");

        LoginResponseDTO loginResponse = authService.login(loginRequest);
        assertNotNull(loginResponse.getAccessToken());
        System.out.println("登录成功，accessToken: " + loginResponse.getAccessToken().substring(0, 20) + "...");

        // 3. 验证Token
        boolean valid = authService.validateToken(loginResponse.getAccessToken());
        assertTrue(valid);
        System.out.println("Token验证成功");

        // 4. 登出
        authService.logout(loginResponse.getAccessToken(), loginResponse.getUserId());
        System.out.println("登出成功");

        System.out.println("✅ 完整流程测试通过");
    }

    // ==================== 异常场景测试 ====================

    @Test
    @DisplayName("测试密码不一致")
    public void testPasswordMismatch() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUserType(UserType.NORMAL.getCode());
        request.setRegisterType("phone");
        request.setPassword("pass123456");
        request.setConfirmPassword("pass654321"); // 不一致
        request.setPhone("13900139002");
        request.setPhoneVerifyCode("123456");

        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        System.out.println("✅ 密码不一致测试通过");
    }

    @Test
    @DisplayName("测试无效的登录方式")
    public void testInvalidLoginType() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLoginType("invalid_type"); // 无效的登录方式
        request.setPhone("13900139003");
        request.setPassword("pass123456");

        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        System.out.println("✅ 无效登录方式测试通过");
    }
}
