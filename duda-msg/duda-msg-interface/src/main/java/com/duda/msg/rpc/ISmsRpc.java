package com.duda.msg.rpc;

import com.duda.msg.dto.MsgCheckDTO;
import com.duda.msg.enums.MsgSendResultEnum;

/**
 * 短信服务RPC接口
 *
 * 提供短信验证码的发送和验证功能
 * 支持开发环境模拟验证码和生产环境真实发送
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
public interface ISmsRpc {

    /**
     * 发送短信登录验证码
     *
     * 功能说明：
     * - 生成4位随机验证码
     * - 存储到Redis（60秒过期）
     * - 防重发检查（60秒内不能重复发送）
     * - 支持环境切换（开发环境打印日志，生产环境真实发送）
     *
     * @param phone 手机号
     * @return 发送结果
     */
    MsgSendResultEnum sendLoginCode(String phone);

    /**
     * 校验登录验证码
     *
     * 功能说明：
     * - 从Redis获取存储的验证码
     * - 验证成功后自动删除（防止重复使用）
     *
     * @param phone 手机号
     * @param code 验证码
     * @return 校验结果
     */
    MsgCheckDTO checkLoginCode(String phone, Integer code);
}
