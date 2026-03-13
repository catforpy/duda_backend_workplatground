package com.duda.common.mq.message;

import com.duda.common.mq.BaseMqMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户缓存变更消息
 *
 * 用于：
 * - 通知其他服务清除用户相关缓存
 * - 同步用户信息变更
 * - 保持数据一致性
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserCacheMsg extends BaseMqMsg {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 操作类型（update/delete）
     */
    private String operation;

    /**
     * 变更时间
     */
    private String changeTime;

    /**
     * 变更字段（JSON格式，可选）
     * 例如：{"phone":"13900139000","email":"new@example.com"}
     */
    private String changedFields;

    /**
     * 变更原因（register/update/profile_update/password_change）
     */
    private String reason;
}
