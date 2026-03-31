package com.duda.user.provider.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.duda.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 小程序PO
 *
 * 表名: mini_programs
 * 说明: 小程序信息表，包含小程序基本信息、配置、认证、备案等
 * 租户隔离: 是（通过tenant_id字段）
 *
 * @author Claude
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mini_programs")
public class MiniProgramPO extends BaseEntity {

    /**
     * 小程序ID（主键）
     */
    private Long id;

    /**
     * 租户ID（租户隔离字段）
     */
    private Long tenantId;

    /**
     * 小程序AppID
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
     * 所属公司ID
     */
    private Long companyId;

    /**
     * 小程序状态
     * active-激活, inactive-未激活, deleted-已删除
     */
    private String status;

    /**
     * 微信Token
     */
    private String wechatToken;

    /**
     * 微信消息加密密钥
     */
    private String wechatEncodingAesKey;

    /**
     * 所有者用户ID
     */
    private Long ownerId;

    /**
     * 开发者公司ID
     */
    private Long developerCompanyId;

    /**
     * 业务模式
     * development-代开发, self-develop-自开发
     */
    private String businessMode;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 一级分类
     */
    private String firstCategory;

    /**
     * 二级分类
     */
    private String secondCategory;

    /**
     * 二维码URL
     */
    private String qrcodeUrl;

    /**
     * 小程序介绍
     */
    private String intro;

    /**
     * 微信认证状态
     * 0-未认证, 1-已认证
     */
    private Byte wechatCertified;

    /**
     * 认证状态
     * none-未认证, pending-认证中, certified-已认证, expired-已过期
     */
    private String certificationStatus;

    /**
     * 认证时间
     */
    private Date certificationTime;

    /**
     * 备案状态
     * none-未备案, pending-备案中, filed-已备案
     */
    private String filingStatus;

    /**
     * 备案号
     */
    private String filingNo;

    /**
     * 备案时间
     */
    private Date filingTime;

    /**
     * 微信支付是否启用
     * 0-否, 1-是
     */
    private Byte wechatPayEnabled;

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
     * dev-开发中, review-审核中, online-已上线, offline-已下线
     */
    private String onlineStatus;

    /**
     * 授权时间
     */
    private Date authorizeTime;

    /**
     * 最后发布时间
     */
    private Date lastPublishTime;

    /**
     * 标签（JSON格式）
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object tags;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
