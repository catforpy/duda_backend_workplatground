package com.duda.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 所有实体类的父类，包含公共字段
 *
 * 使用方法：
 * <pre>
 * &#64;Data
 * &#64;TableName("t_user")
 * public class User extends BaseEntity {
 *     private String username;
 *     private String password;
 * }
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法生成）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志（0=未删除，1=已删除）
     */
    @TableLogic
    private Integer deleted;

    /**
     * 版本号（用于乐观锁）
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}
