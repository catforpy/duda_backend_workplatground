package com.duda.user.entity.company;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公司资质文件Entity
 *
 * 对应数据库表：company_qualifications
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
@TableName("company_qualifications")
public class CompanyQualification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 资质类型
     */
    private String qualificationType;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 审核状态
     */
    private String auditStatus;

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
