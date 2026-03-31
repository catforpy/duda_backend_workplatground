package com.duda.user.dto.merchant;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 跨平台账户绑定DTO
 *
 * 说明：用于Java后端和PHP后端的账户绑定
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Data
public class CrossPlatformBindingDTO implements Serializable {

    /**
     * 主键ID（雪花算法）
     */
    private Long id;

    // ========== 基本信息 ==========

    /**
     * 手机号（全局唯一）
     */
    private String phone;

    /**
     * 邮箱（全局唯一）
     */
    private String email;

    // ========== Java后端用户信息 ==========

    /**
     * Java后端用户ID
     */
    private Long javaUserId;

    /**
     * Java后端用户分片ID（0-99）
     */
    private Integer javaUserShard;

    /**
     * Java后端用户名（冗余字段）
     */
    private String javaUsername;

    // ========== PHP后端用户信息 ==========

    /**
     * PHP后端用户ID
     */
    private Long phpUserId;

    /**
     * PHP后端用户分片ID（如果有）
     */
    private Integer phpUserShard;

    /**
     * PHP后端用户名（冗余字段）
     */
    private String phpUsername;

    // ========== 其他平台用户信息（预留）==========

    /**
     * Python后端用户ID（预留）
     */
    private Long pythonUserId;

    /**
     * Go后端用户ID（预留）
     */
    private Long goUserId;

    // ========== 绑定信息 ==========

    /**
     * 绑定类型：auto-自动绑定, manual-手动绑定, system-系统绑定
     */
    private String bindType;

    /**
     * 绑定来源：java-Java后端发起, php-PHP后端发起, admin-管理员操作
     */
    private String bindSource;

    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;

    /**
     * 验证IP
     */
    private String verifyIp;

    // ========== 状态信息 ==========

    /**
     * 状态：1-已绑定 2-已解绑 3-待验证
     */
    private Integer status;

    /**
     * 解绑时间
     */
    private LocalDateTime unbindTime;

    /**
     * 解绑原因
     */
    private String unbindReason;

    // ========== 同步状态 ==========

    /**
     * 同步状态：synced-已同步, pending-待同步, failed-同步失败
     */
    private String syncStatus;

    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;

    /**
     * 同步错误信息
     */
    private String syncErrorMessage;

    // ========== 其他信息 ==========

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
