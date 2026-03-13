package com.duda.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户身份关联实体
 *
 * 支持同一用户拥有多个身份
 * 每个身份有独立的状态和审核流程
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Data
@TableName("user_type_roles")
@Schema(description = "用户身份关联实体")
public class UserTypeRole {

    /**
     * 主键ID（雪花算法）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户身份类型
     * - platform_admin: 平台管理员
     * - service_provider: 服务商
     * - platform_account: 都达账户
     * - backend_admin: 后台管理员
     */
    @Schema(description = "用户身份类型")
    private String userType;

    /**
     * 身份状态
     * - active: 激活
     * - inactive: 未激活
     * - suspended: 暂停
     * - deleted: 已删除
     */
    @Schema(description = "身份状态")
    private String status;

    /**
     * 所属公司ID（仅服务商身份需要）
     */
    @Schema(description = "所属公司ID")
    private Long companyId;

    /**
     * 部门
     */
    @Schema(description = "部门")
    private String department;

    /**
     * 职位
     */
    @Schema(description = "职位")
    private String position;

    /**
     * 审核状态
     * - pending: 待审核
     * - approved: 已通过
     * - rejected: 已拒绝
     */
    @Schema(description = "审核状态")
    private String auditStatus;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    private Long auditBy;

    /**
     * 审核备注
     */
    @Schema(description = "审核备注")
    private String auditRemark;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 逻辑删除：0-未删除, 1-已删除
     */
    @Schema(description = "逻辑删除")
    private Integer deleted;

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

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;
}
