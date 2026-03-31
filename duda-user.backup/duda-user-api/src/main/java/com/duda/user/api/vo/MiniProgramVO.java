package com.duda.user.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 小程序VO
 *
 * @author Claude
 * @date 2026-03-27
 */
@Data
@Schema(description = "小程序信息VO")
public class MiniProgramVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "小程序ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "小程序AppID")
    private String appid;

    @Schema(description = "小程序名称")
    private String name;

    @Schema(description = "小程序描述")
    private String description;

    @Schema(description = "所属公司名称")
    private String companyName;

    @Schema(description = "小程序状态", example = "active")
    private String status;

    @Schema(description = "小程序状态名称")
    private String statusName;

    @Schema(description = "所有者用户名称")
    private String ownerName;

    @Schema(description = "开发者公司名称")
    private String developerCompanyName;

    @Schema(description = "业务模式", example = "development")
    private String businessMode;

    @Schema(description = "业务模式名称")
    private String businessModeName;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "一级分类")
    private String firstCategory;

    @Schema(description = "二级分类")
    private String secondCategory;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "二维码URL")
    private String qrcodeUrl;

    @Schema(description = "小程序介绍")
    private String intro;

    @Schema(description = "是否已微信认证", example = "true")
    private Boolean wechatCertified;

    @Schema(description = "认证状态", example = "certified")
    private String certificationStatus;

    @Schema(description = "认证状态名称")
    private String certificationStatusName;

    @Schema(description = "认证时间")
    private Date certificationTime;

    @Schema(description = "备案状态", example = "filed")
    private String filingStatus;

    @Schema(description = "备案状态名称")
    private String filingStatusName;

    @Schema(description = "备案号")
    private String filingNo;

    @Schema(description = "备案时间")
    private Date filingTime;

    @Schema(description = "是否启用微信支付", example = "false")
    private Boolean wechatPayEnabled;

    @Schema(description = "微信支付商户号")
    private String wechatPayMchId;

    @Schema(description = "服务器域名")
    private String serverDomain;

    @Schema(description = "上线状态", example = "online")
    private String onlineStatus;

    @Schema(description = "上线状态名称")
    private String onlineStatusName;

    @Schema(description = "授权时间")
    private Date authorizeTime;

    @Schema(description = "最后发布时间")
    private Date lastPublishTime;

    @Schema(description = "上线天数")
    private Long onlineDays;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
