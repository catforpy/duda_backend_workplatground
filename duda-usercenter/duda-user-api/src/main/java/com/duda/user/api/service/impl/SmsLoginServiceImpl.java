package com.duda.user.api.service.impl;

import com.duda.common.security.dto.TokenDTO;
import com.duda.common.security.service.TokenService;
import com.duda.user.dto.BindPhoneReqDTO;
import com.duda.user.dto.ReplacePrimaryPhoneReqDTO;
import com.duda.user.dto.SmsLoginReqDTO;
import com.duda.user.dto.SmsSendCodeReqDTO;
import com.duda.user.dto.SmsSendCodeRespDTO;
import com.duda.user.dto.UserDTO;
import com.duda.user.api.service.ISmsLoginService;
import com.duda.user.rpc.ISmsLoginRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 短信登录服务实现类
 *
 * 职责：
 * 1. 调用 Provider 的 RPC 服务
 * 2. 处理 Token 生成
 * 3. 组装返回数据
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Service
public class SmsLoginServiceImpl implements ISmsLoginService {

    private static final Logger logger = LoggerFactory.getLogger(SmsLoginServiceImpl.class);

    @Resource
    private TokenService tokenService;

    @DubboReference(
        version = "1.0.0",
        group = "USER_GROUP",
        check = false
    )
    private ISmsLoginRpc smsLoginRpc;

    @Override
    public SmsSendCodeRespDTO sendCode(SmsSendCodeReqDTO request) {
        logger.info("API层：发送短信验证码，手机号:{}, 场景:{}", request.getPhone(), request.getScene());

        // 直接调用 Provider 的 RPC 服务
        SmsSendCodeRespDTO response = smsLoginRpc.sendCode(request);

        logger.info("API层：验证码发送完成，手机号:{}, 成功:{}", request.getPhone(), response.getSuccess());
        return response;
    }

    @Override
    public UserDTO loginBySms(SmsLoginReqDTO request) {
        logger.info("API层：短信验证码登录，手机号:{}", request.getPhone());

        // 调用 Provider 的 RPC 服务进行登录
        UserDTO userDTO = smsLoginRpc.loginBySms(request);

        logger.info("API层：登录成功，用户ID:{}, 手机号:{}", userDTO.getId(), userDTO.getPhone());
        return userDTO;
    }

    @Override
    public String generateToken(UserDTO userDTO) {
        logger.info("生成访问令牌，用户ID:{}", userDTO.getId());

        // 生成 Token（使用 TokenService 的 generateTokens 方法）
        TokenDTO tokenDTO = tokenService.generateTokens(
                userDTO.getId(),
                userDTO.getUsername(),
                userDTO.getUserType()
        );

        logger.info("访问令牌生成成功，用户ID:{}", userDTO.getId());
        return tokenDTO.getAccessToken();
    }

    @Override
    public Boolean bindPhone(BindPhoneReqDTO request) {
        logger.info("API层：绑定手机号，用户ID:{}, 手机号:{}", request.getUserId(), request.getPhone());

        // 调用 Provider 的 RPC 服务
        Boolean result = smsLoginRpc.bindPhone(request);

        logger.info("API层：绑定手机号完成，用户ID:{}, 结果:{}", request.getUserId(), result);
        return result;
    }

    @Override
    public Boolean replacePrimaryPhone(ReplacePrimaryPhoneReqDTO request) {
        logger.info("API层：更换主手机号，用户ID:{}, 新手机号:{}", request.getUserId(), request.getNewPhone());

        // 调用 Provider 的 RPC 服务
        Boolean result = smsLoginRpc.replacePrimaryPhone(request);

        logger.info("API层：更换主手机号完成，用户ID:{}, 结果:{}", request.getUserId(), result);
        return result;
    }

    @Override
    public Boolean unbindPhone(Long userId, String phone) {
        logger.info("API层：解绑手机号，用户ID:{}, 手机号:{}", userId, phone);

        // 调用 Provider 的 RPC 服务
        Boolean result = smsLoginRpc.unbindPhone(userId, phone);

        logger.info("API层：解绑手机号完成，用户ID:{}, 结果:{}", userId, result);
        return result;
    }

    @Override
    public Boolean verifyCode(String phone, String code, String scene) {
        logger.debug("API层：验证短信验证码，手机号:{}, 场景:{}", phone, scene);

        // 调用 Provider 的 RPC 服务
        Boolean result = smsLoginRpc.verifyCode(phone, code, scene);

        logger.debug("API层：验证码验证完成，手机号:{}, 结果:{}", phone, result);
        return result;
    }
}
