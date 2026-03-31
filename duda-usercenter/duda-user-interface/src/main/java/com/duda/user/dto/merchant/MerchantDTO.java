package com.duda.user.dto.merchant;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商户信息DTO
 *
 * 说明：商户基本信息表（不包含敏感配置信息如密钥等）
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Data
public class MerchantDTO implements Serializable {

    /**
     * 商户ID（雪花算法）
     */
    private Long id;

    /**
     * 租户ID（多租户隔离）
     */
    private Long tenantId;

    /**
     * 商户编码（全局唯一，如：M001）
     */
    private String merchantCode;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 商户简称
     */
    private String merchantShortName;

    /**
     * 商户类型：mini_program-小程序, app-移动应用, web-网站, h5-H5应用
     */
    private String merchantType;

    /**
     * 商户分类：retail-零售, catering-餐饮, service-服务, education-教育
     */
    private String merchantCategory;

    // ========== 联系信息 ==========

    /**
     * 联系人姓名
     */
    private String contactPerson;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 联系人微信号
     */
    private String contactWechat;

    /**
     * 联系人QQ号
     */
    private String contactQq;

    // ========== 公司信息 ==========

    /**
     * 关联公司ID
     */
    private Long companyId;

    /**
     * 公司名称（冗余字段）
     */
    private String companyName;

    /**
     * 营业执照号
     */
    private String licenseNo;

    /**
     * 营业执照URL
     */
    private String licenseUrl;

    /**
     * 法人代表姓名
     */
    private String legalPerson;

    /**
     * 法人代表身份证号
     */
    private String legalPersonId;

    // ========== 地址信息 ==========

    /**
     * 省份
     */
    private String province;

    /**
     * 省份编码
     */
    private String provinceCode;

    /**
     * 城市
     */
    private String city;

    /**
     * 城市编码
     */
    private String cityCode;

    /**
     * 区县
     */
    private String district;

    /**
     * 区县编码
     */
    private String districtCode;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    // ========== 微信小程序配置 ==========

    /**
     * 微信小程序AppID
     */
    private String miniAppAppid;

    /**
     * 小程序名称
     */
    private String miniAppName;

    /**
     * 小程序头像URL
     */
    private String miniAppAvatar;

    /**
     * 小程序二维码URL
     */
    private String miniAppQrcode;

    /**
     * 小程序介绍
     */
    private String miniAppIntro;

    /**
     * 小程序标签
     */
    private String miniAppTags;

    // ========== 分账配置 ==========

    /**
     * 是否启用分账：0-否 1-是
     */
    private Integer profitSharingEnabled;

    /**
     * 平台分账比例（0.10 = 10%）
     */
    private BigDecimal profitSharingRate;

    /**
     * 分账账户
     */
    private String profitSharingAccount;

    /**
     * 分账账户名称
     */
    private String profitSharingName;

    /**
     * 结算周期：T+0-实时, T+1-次日, T+7-周结, M+1-月结
     */
    private String settlementCycle;

    /**
     * 最低结算金额
     */
    private BigDecimal minSettlementAmount;

    // ========== 业务配置 ==========

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 商户标签（逗号分隔）
     */
    private String tags;

    /**
     * 经营范围
     */
    private String businessScope;

    /**
     * 关键词
     */
    private String keywords;

    /**
     * 商户详细描述
     */
    private String description;

    /**
     * 扩展配置（JSON格式）
     */
    private String extConfig;

    /**
     * 营业时间配置（JSON格式）
     */
    private String businessHours;

    /**
     * 服务配置（JSON格式）
     */
    private String serviceConfig;

    // ========== 状态管理 ==========

    /**
     * 状态：pending-待审核, active-已激活, suspended-暂停, deleted-已删除
     */
    private String status;

    /**
     * 审核状态：pending-待审核, approved-已通过, rejected-已拒绝
     */
    private String auditStatus;

    /**
     * 服务过期时间（NULL表示永久）
     */
    private LocalDateTime expireTime;

    // ========== 统计信息 ==========

    /**
     * 总用户数
     */
    private Integer totalUsers;

    /**
     * 总订单数
     */
    private Integer totalOrders;

    /**
     * 总营收（元）
     */
    private BigDecimal totalRevenue;

    /**
     * 访问次数
     */
    private Long visitCount;

    // ========== 时间信息 ==========

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 激活时间
     */
    private LocalDateTime activateTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 商户Logo URL
     */
    private String logoUrl;

    /**
     * 乐观锁版本号
     */
    private Integer version;
}
