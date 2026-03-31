package com.duda.user.api.service;

import com.duda.user.dto.BindPhoneReqDTO;
import com.duda.user.dto.ReplacePrimaryPhoneReqDTO;
import com.duda.user.dto.SmsLoginReqDTO;
import com.duda.user.dto.SmsSendCodeReqDTO;
import com.duda.user.dto.SmsSendCodeRespDTO;
import com.duda.user.dto.UserDTO;

/**
 * 短信登录服务接口
 *
 * 提供短信验证码登录、绑定手机号、更换手机号等功能
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
public interface ISmsLoginService {

    /**
     * 发送短信验证码
     *
     * @param request 发送请求
     * @return 响应信息
     */
    SmsSendCodeRespDTO sendCode(SmsSendCodeReqDTO request);

    /**
     * 短信验证码登录
     * 如果手机号未注册，则自动注册
     *
     * @param request 登录请求
     * @return 用户信息
     */
    UserDTO loginBySms(SmsLoginReqDTO request);

    /**
     * 生成访问令牌
     *
     * @param userDTO 用户信息
     * @return 访问令牌
     */
    String generateToken(UserDTO userDTO);

    /**
     * 绑定手机号到已有账户
     *
     * @param request 绑定请求
     * @return 是否成功
     */
    Boolean bindPhone(BindPhoneReqDTO request);

    /**
     * 更换主手机号
     *
     * @param request 更换请求
     * @return 是否成功
     */
    Boolean replacePrimaryPhone(ReplacePrimaryPhoneReqDTO request);

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
}
