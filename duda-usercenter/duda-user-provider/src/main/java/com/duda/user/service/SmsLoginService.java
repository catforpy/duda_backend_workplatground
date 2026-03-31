package com.duda.user.service;

import com.duda.user.dto.BindPhoneReqDTO;
import com.duda.user.dto.ReplacePrimaryPhoneReqDTO;
import com.duda.user.dto.SmsLoginReqDTO;
import com.duda.user.dto.SmsSendCodeReqDTO;
import com.duda.user.dto.SmsSendCodeRespDTO;
import com.duda.user.dto.UserDTO;

/**
 * 短信登录服务接口
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
public interface SmsLoginService {

    /**
     * 发送短信验证码
     *
     * @param reqDTO 发送请求
     * @return 响应信息
     */
    SmsSendCodeRespDTO sendCode(SmsSendCodeReqDTO reqDTO);

    /**
     * 短信验证码登录
     * 如果手机号未注册，则自动注册
     *
     * @param reqDTO 登录请求
     * @return 用户信息
     */
    UserDTO loginBySms(SmsLoginReqDTO reqDTO);

    /**
     * 绑定手机号到已有账户
     *
     * @param reqDTO 绑定请求
     * @return 是否成功
     */
    Boolean bindPhone(BindPhoneReqDTO reqDTO);

    /**
     * 更换主手机号
     *
     * @param reqDTO 更换请求
     * @return 是否成功
     */
    Boolean replacePrimaryPhone(ReplacePrimaryPhoneReqDTO reqDTO);

    /**
     * 解绑手机号
     *
     * @param userId 用户ID
     * @param phone 手机号
     * @return 是否成功
     */
    Boolean unbindPhone(Long userId, String phone);

    /**
     * 验证短信验证码
     *
     * @param phone 手机号
     * @param code 验证码
     * @param scene 场景
     * @return 是否有效
     */
    Boolean verifyCode(String phone, String code, String scene);

    /**
     * 标记验证码已使用
     *
     * @param phone 手机号
     * @param code 验证码
     * @param scene 场景
     */
    void markCodeAsUsed(String phone, String code, String scene);
}
