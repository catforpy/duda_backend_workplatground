package com.duda.user.entity.merchant;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 跨平台账户绑定Entity
 *
 * 对应数据库表：cross_platform_bindings
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Data
@TableName("cross_platform_bindings")
public class CrossPlatformBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 手机号（全局唯一）
     */
    private String phone;

    /**
     * 邮箱（全局唯一）
     */
    private String email;

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

    /**
     * PHP后端用户ID
     */
    private Long phpUserId;

    /**
     * PHP后端用户分片ID
     */
    private Integer phpUserShard;

    /**
     * PHP后端用户名（冗余字段）
     */
    private String phpUsername;

    /**
     * Python后端用户ID（预留）
     */
    private Long pythonUserId;

    /**
     * Go后端用户ID（预留）
     */
    private Long goUserId;

    /**
     * 绑定类型
     */
    private String bindType;

    /**
     * 绑定来源
     */
    private String bindSource;

    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;

    /**
     * 验证码
     */
    private String verifyCode;

    /**
     * 验证IP
     */
    private String verifyIp;

    /**
     * 验证方式
     */
    private String verifyMethod;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作备注
     */
    private String operationRemark;

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

    /**
     * 解绑操作人ID
     */
    private Long unbindOperatorId;

    /**
     * 同步状态
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

    /**
     * 扩展信息（JSON格式）
     */
    private String extInfo;

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
