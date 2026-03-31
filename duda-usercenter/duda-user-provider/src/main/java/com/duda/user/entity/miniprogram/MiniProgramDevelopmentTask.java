package com.duda.user.entity.miniprogram;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小程序代开发任务Entity
 *
 * 对应数据库表：mini_program_development_tasks
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
@TableName("mini_program_development_tasks")
public class MiniProgramDevelopmentTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 小程序ID
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
     * 企业名称
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
     */
    private String miniProgramNameStatus;

    /**
     * 进度百分比
     */
    private Integer progressPercent;

    /**
     * 预计完成时间
     */
    private LocalDateTime estimatedFinishTime;

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
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    /**
     * 乐观锁版本号
     */
    private Integer version;
    private LocalDateTime updateTime;
}
