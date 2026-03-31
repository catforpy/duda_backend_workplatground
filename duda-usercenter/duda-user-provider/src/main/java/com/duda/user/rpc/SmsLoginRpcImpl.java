package com.duda.user.rpc;

import com.duda.user.dto.BindPhoneReqDTO;
import com.duda.user.dto.ReplacePrimaryPhoneReqDTO;
import com.duda.user.dto.SmsLoginReqDTO;
import com.duda.user.dto.SmsSendCodeReqDTO;
import com.duda.user.dto.SmsSendCodeRespDTO;
import com.duda.user.dto.UserDTO;
import com.duda.user.service.SmsLoginService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 短信登录RPC实现类
 * 暴露Dubbo服务供其他服务调用
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@DubboService(
    version = "1.0.0",
    group = "USER_GROUP",
    timeout = 5000
)
public class SmsLoginRpcImpl implements ISmsLoginRpc {

    @Resource
    private SmsLoginService smsLoginService;

    @Override
    public SmsSendCodeRespDTO sendCode(SmsSendCodeReqDTO reqDTO) {
        return smsLoginService.sendCode(reqDTO);
    }

    @Override
    public UserDTO loginBySms(SmsLoginReqDTO reqDTO) {
        return smsLoginService.loginBySms(reqDTO);
    }

    @Override
    public Boolean bindPhone(BindPhoneReqDTO reqDTO) {
        return smsLoginService.bindPhone(reqDTO);
    }

    @Override
    public Boolean replacePrimaryPhone(ReplacePrimaryPhoneReqDTO reqDTO) {
        return smsLoginService.replacePrimaryPhone(reqDTO);
    }

    @Override
    public Boolean unbindPhone(Long userId, String phone) {
        return smsLoginService.unbindPhone(userId, phone);
    }

    @Override
    public Boolean verifyCode(String phone, String code, String scene) {
        return smsLoginService.verifyCode(phone, code, scene);
    }
}
