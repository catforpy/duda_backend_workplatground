package com.duda.user.entity.miniprogram;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小程序备案Entity
 *
 * 对应数据库表：mini_program_filing
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
@TableName("mini_program_filing")
public class MiniProgramFiling implements Serializable {

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
     * 小程序ID
     */
    private Long miniProgramId;

    /**
     * 备案状态
     */
    private String filingStatus;

    /**
     * 备案号
     */
    private String filingNo;

    /**
     * 备案时间
     */
    private LocalDateTime filingTime;

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
