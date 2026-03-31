package com.duda.user.provider.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duda.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 小程序代开发任务表PO
 *
 * 表名: mini_program_development_tasks
 * 说明: 小程序代开发任务信息表
 * 租户隔离: 是（通过tenant_id字段）
 *
 * @author Claude
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mini_program_development_tasks")
public class MiniProgramDevelopmentTasksPO extends BaseEntity {

    /**
     * 任务ID（主键）
     */
    private Long id;

    /**
     * 租户ID（租户隔离字段）
     */
    private Long tenantId;

    /**
     * 任务编号（全局唯一）
     */
    private String taskNo;

    /**
     * 小程序ID（关联mini_programs.id）
     */
    private Long miniProgramId;

    /**
     * 客户公司ID
     */
    private Long clientCompanyId;

    /**
     * 客户用户ID
     */
    private Long clientUserId;

    /**
     * 开发者公司ID
     */
    private Long developerCompanyId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务状态
     * pending-待处理, processing-处理中, completed-已完成, cancelled-已取消
     */
    private String taskStatus;

    /**
     * 当前步骤
     */
    private String currentStep;

    /**
     * 小程序AppID
     */
    private String wechatAppid;

    /**
     * 微信企业名称
     */
    private String wechatEnterpriseName;

    /**
     * 法人微信号
     */
    private String wechatLegalPersonWechat;

    /**
     * 小程序名称
     */
    private String miniProgramName;

    /**
     * 名称审核状态
     * pending-审核中, approved-已通过, rejected-已拒绝
     */
    private String miniProgramNameStatus;

    /**
     * 进度百分比（0-100）
     */
    private Integer progressPercent;

    /**
     * 预计完成时间
     */
    private Date estimatedFinishTime;

    /**
     * 总费用
     */
    private BigDecimal totalFee;

    /**
     * 已付费用
     */
    private BigDecimal paidFee;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 完成时间
     */
    private Date finishTime;

    /**
     * 删除标记
     * 0-正常, 1-已删除
     */
    private Byte deleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
