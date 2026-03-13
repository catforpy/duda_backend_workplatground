package com.duda.msg.provider.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 短信服务配置类
 *
 * 支持开发环境模拟验证码和生产环境真实发送的插拔式设计
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Configuration
@ConfigurationProperties(prefix = "duda.sms")
@Getter
@Setter
public class SmsConfig {

    /**
     * 是否开启真实发送短信
     * - false: 开发环境，验证码输出到日志，不真实发送（默认）
     * - true: 生产环境，调用真实的短信服务商API
     */
    private boolean realSend = false;

    /**
     * 短信服务器IP
     */
    private String serverIp;

    /**
     * 短信服务器端口
     */
    private Integer port;

    /**
     * 账号ID
     */
    private String accountId;

    /**
     * 账号令牌
     */
    private String accountToken;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 测试手机号
     * 开发测试阶段使用的手机号，所有测试短信都会发送到这个号码
     */
    private String testPhone;

    /**
     * 验证码有效期（秒）
     * 默认60秒
     */
    private int codeExpireSeconds = 60;

    /**
     * 验证码长度
     * 默认4位
     */
    private int codeLength = 4;
}
