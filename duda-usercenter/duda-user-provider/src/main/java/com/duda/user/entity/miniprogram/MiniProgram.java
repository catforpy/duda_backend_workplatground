package com.duda.user.entity.miniprogram;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小程序Entity
 *
 * 对应数据库表：mini_programs
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
@TableName("mini_programs")
public class MiniProgram implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 租户ID（多租户隔离）
     */
    private Long tenantId;

    /**
     * 微信小程序AppID
     */
    private String appid;

    /**
     * 小程序名称
     */
    private String name;

    /**
     * 小程序描述
     */
    private String description;

    /**
     * 关联公司ID
     */
    private Long companyId;

    /**
     * 状态
     */
    private String status;

    /**
     * 微信Token
     */
    private String wechatToken;

    /**
     * 微信EncodingAESKey
     */
    private String wechatEncodingAesKey;

    /**
     * 所有者用户ID
     */
    private Long ownerUserId;

    /**
     * 开发公司ID
     */
    private Long developerCompanyId;

    /**
     * 业务模式
     */
    private String businessMode;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 一级类目
     */
    private String firstCategory;

    /**
     * 二级类目
     */
    private String secondCategory;

    /**
     * 小程序二维码URL
     */
    private String qrcodeUrl;

    /**
     * 小程序介绍
     */
    private String intro;

    /**
     * 微信认证状态
     */
    private Integer wechatCertified;

    /**
     * 认证状态
     */
    private String certificationStatus;

    /**
     * 认证时间
     */
    private LocalDateTime certificationTime;

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
     * 微信支付启用状态
     */
    private Integer wechatPayEnabled;

    /**
     * 微信支付商户号
     */
    private String wechatPayMchId;

    /**
     * 服务器域名
     */
    private String serverDomain;

    /**
     * Socket服务器域名
     */
    private String socketServerDomain;

    /**
     * 上传域名
     */
    private String uploadDomain;

    /**
     * 下载域名
     */
    private String downloadDomain;

    /**
     * 业务域名
     */
    private String businessDomain;

    /**
     * 上线状态
     */
    private String onlineStatus;

    /**
     * 授权时间
     */
    private LocalDateTime authorizeTime;

    /**
     * 最后发布时间
     */
    private LocalDateTime lastPublishTime;

    /**
     * 标签（JSON格式）
     */
    private String tags;

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
    @com.baomidou.mybatisplus.annotation.Version
    private Integer version;
    private LocalDateTime updateTime;
}
