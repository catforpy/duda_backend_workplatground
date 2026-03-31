package com.duda.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户手机号绑定实体
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Data
@TableName("user_phone_bindings")
@Schema(description = "用户手机号绑定实体")
public class UserPhoneBinding {

    /**
     * 绑定ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "绑定ID")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户分片ID
     */
    @Schema(description = "用户分片ID")
    private Integer userShard;

    /**
     * 绑定的手机号（全局唯一）
     */
    @Schema(description = "绑定的手机号")
    private String phone;

    /**
     * 绑定类型：sms_login=短信登录
     */
    @Schema(description = "绑定类型")
    private String bindType;

    /**
     * 绑定时间
     */
    @Schema(description = "绑定时间")
    private LocalDateTime bindTime;

    /**
     * 绑定时的验证码
     */
    @Schema(description = "绑定时的验证码")
    private String verifyCode;

    /**
     * 绑定时的IP
     */
    @Schema(description = "绑定时的IP")
    private String verifyIp;

    /**
     * 是否激活：1=激活 0=已更换
     */
    @Schema(description = "是否激活")
    private Integer isActive;

    /**
     * 更换时间
     */
    @Schema(description = "更换时间")
    private LocalDateTime replaceTime;

    /**
     * 更换原因
     */
    @Schema(description = "更换原因")
    private String replaceReason;

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
