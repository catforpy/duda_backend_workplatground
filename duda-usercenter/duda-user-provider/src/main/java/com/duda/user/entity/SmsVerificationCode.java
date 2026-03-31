package com.duda.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 短信验证码实体
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Data
@TableName("sms_verification_codes")
@Schema(description = "短信验证码实体")
public class SmsVerificationCode {

    /**
     * 验证码ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "验证码ID")
    private Long id;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 验证码
     */
    @Schema(description = "验证码")
    private String code;

    /**
     * 场景：login/register/reset_pwd/bind_phone/verify_bind
     */
    @Schema(description = "验证场景")
    private String scene;

    /**
     * 关联用户ID（登录/注册时自动填充）
     */
    @Schema(description = "关联用户ID")
    private Long userId;

    /**
     * 用户分片ID（0-99）
     */
    @Schema(description = "用户分片ID")
    private Integer userShard;

    /**
     * 设备唯一标识
     */
    @Schema(description = "设备唯一标识")
    private String deviceId;

    /**
     * 设备类型：ios/android/web/miniapp
     */
    @Schema(description = "设备类型")
    private String deviceType;

    /**
     * 请求IP
     */
    @Schema(description = "请求IP")
    private String ip;

    /**
     * 同一IP/设备今日发送次数
     */
    @Schema(description = "发送次数")
    private Integer sendCount;

    /**
     * 状态：0=未使用 1=已使用 2=已过期
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /**
     * 使用时间
     */
    @Schema(description = "使用时间")
    private LocalDateTime useTime;

    /**
     * 验证结果：success/failed/expired
     */
    @Schema(description = "验证结果")
    private String verifyResult;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因")
    private String failReason;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
