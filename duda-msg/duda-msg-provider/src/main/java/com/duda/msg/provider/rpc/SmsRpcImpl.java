package com.duda.msg.provider.rpc;

import com.duda.msg.dto.MsgCheckDTO;
import com.duda.msg.enums.MsgSendResultEnum;
import com.duda.msg.provider.service.ISmsService;
import com.duda.msg.rpc.ISmsRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 短信服务RPC实现
 *
 * 将短信服务通过Dubbo暴露给其他服务调用
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@DubboService(
        interfaceClass = ISmsRpc.class,
        version = "1.0.0",
        group = "MSG_GROUP"
)
public class SmsRpcImpl implements ISmsRpc {

    @Resource
    private ISmsService smsService;

    @Override
    public MsgSendResultEnum sendLoginCode(String phone) {
        return smsService.sendLoginCode(phone);
    }

    @Override
    public MsgCheckDTO checkLoginCode(String phone, Integer code) {
        return smsService.checkLoginCode(phone, code);
    }
}
