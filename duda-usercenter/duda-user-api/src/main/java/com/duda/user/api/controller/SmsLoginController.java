package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.user.dto.BindPhoneReqDTO;
import com.duda.user.dto.ReplacePrimaryPhoneReqDTO;
import com.duda.user.dto.SmsLoginReqDTO;
import com.duda.user.dto.SmsSendCodeReqDTO;
import com.duda.user.dto.SmsSendCodeRespDTO;
import com.duda.user.dto.UserDTO;
import com.duda.user.api.service.ISmsLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;

/**
 * 短信登录控制器
 *
 * 提供短信验证码登录、绑定手机号、更换手机号等功能
 *
 * 架构说明：
 * Controller 层：只负责接收HTTP请求、参数验证、返回响应
 * Service 层：处理业务逻辑、调用RPC服务、组装数据
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Tag(name = "短信登录", description = "短信验证码登录、绑定手机号、更换手机号等接口")
@RestController
@RequestMapping("/api/sms-login")
public class SmsLoginController {

    @Resource
    private ISmsLoginService smsLoginService;

    /**
     * 发送短信验证码
     *
     * 支持多种场景：login、register、reset_pwd、bind_phone、verify_bind
     */
    @Operation(
        summary = "发送短信验证码",
        description = "发送短信验证码，支持登录、注册、重置密码、绑定手机号等场景"
    )
    @PostMapping("/send-code")
    public Result sendCode(
            @Parameter(description = "发送验证码请求参数", required = true)
            @Valid @RequestBody SmsSendCodeReqDTO request) {

        SmsSendCodeRespDTO response = smsLoginService.sendCode(request);
        return Result.success(response);
    }

    /**
     * 短信验证码登录
     *
     * 如果手机号未注册，则自动注册
     */
    @Operation(
        summary = "短信验证码登录",
        description = "使用手机号和验证码登录，如果手机号未注册则自动注册"
    )
    @PostMapping("/login")
    public Result loginBySms(
            @Parameter(description = "短信登录请求参数", required = true)
            @Valid @RequestBody SmsLoginReqDTO request) {

        UserDTO userDTO = smsLoginService.loginBySms(request);

        // 生成访问令牌
        String token = smsLoginService.generateToken(userDTO);

        // 构建返回数据（包含用户信息和token）
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("user", userDTO);
        data.put("accessToken", token);

        return Result.success(data);
    }

    /**
     * 绑定手机号到已有账户
     */
    @Operation(
        summary = "绑定手机号",
        description = "将手机号绑定到已有账户"
    )
    @PostMapping("/bind-phone")
    public Result bindPhone(
            @Parameter(description = "绑定手机号请求参数", required = true)
            @Valid @RequestBody BindPhoneReqDTO request) {

        Boolean result = smsLoginService.bindPhone(request);
        return Result.success(result);
    }

    /**
     * 更换主手机号
     */
    @Operation(
        summary = "更换主手机号",
        description = "更换用户的主手机号，需要验证新旧手机号"
    )
    @PostMapping("/replace-phone")
    public Result replacePrimaryPhone(
            @Parameter(description = "更换手机号请求参数", required = true)
            @Valid @RequestBody ReplacePrimaryPhoneReqDTO request) {

        Boolean result = smsLoginService.replacePrimaryPhone(request);
        return Result.success(result);
    }

    /**
     * 解绑手机号
     */
    @Operation(
        summary = "解绑手机号",
        description = "解除绑定的手机号"
    )
    @PostMapping("/unbind-phone")
    public Result unbindPhone(
            @Parameter(description = "用户ID", required = true)
            @RequestParam("userId") @NotNull(message = "用户ID不能为空") Long userId,

            @Parameter(description = "手机号", required = true)
            @RequestParam("phone") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone) {

        Boolean result = smsLoginService.unbindPhone(userId, phone);
        return Result.success(result);
    }

    /**
     * 验证短信验证码
     */
    @Operation(
        summary = "验证短信验证码",
        description = "验证短信验证码是否正确"
    )
    @PostMapping("/verify-code")
    public Result verifyCode(
            @Parameter(description = "手机号", required = true)
            @RequestParam("phone") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,

            @Parameter(description = "验证码", required = true)
            @RequestParam("code") @Pattern(regexp = "^\\d{4,6}$", message = "验证码格式不正确") String code,

            @Parameter(description = "场景", required = true)
            @RequestParam("scene") @Pattern(regexp = "^(login|register|reset_pwd|bind_phone|verify_bind)$", message = "场景不合法") String scene) {

        Boolean result = smsLoginService.verifyCode(phone, code, scene);
        return Result.success(result);
    }
}
