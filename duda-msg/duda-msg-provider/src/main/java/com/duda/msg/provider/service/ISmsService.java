package com.duda.msg.provider.service;

import com.duda.msg.dto.MsgCheckDTO;
import com.duda.msg.enums.MsgSendResultEnum;

/**
 * 短信服务接口
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
public interface ISmsService {

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @return 发送结果
     */
    MsgSendResultEnum sendLoginCode(String phone);

    /**
     * 校验登录验证码
     *
     * @param phone 手机号
     * @param code 验证码
     * @return 校验结果
     */
    MsgCheckDTO checkLoginCode(String phone, Integer code);
}
