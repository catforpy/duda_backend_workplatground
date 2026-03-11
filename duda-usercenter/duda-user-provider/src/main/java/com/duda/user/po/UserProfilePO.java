package com.duda.user.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户扩展资料PO
 *
 * 存储用户的扩展信息，不是每个用户都有
 * 与users表是一对一关系
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Data
@TableName("user_profiles")
public class UserProfilePO {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联users表）
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别：0-未知, 1-男, 2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * QQ号
     */
    private String qq;

    /**
     * 微信号
     */
    private String wechat;

    /**
     * 兴趣标签（逗号分隔）
     */
    private String tags;

    /**
     * 隐私级别：1-公开, 2-仅好友, 3-私密
     */
    private Integer privacyLevel;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
