package com.duda.user.entity.merchant;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户信息Entity
 *
 * 对应数据库表：merchants
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Data
@TableName("merchants")
public class Merchant implements Serializable {

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
     * 商户编码（全局唯一）
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
     * 商户类型
     */
    private String merchantType;

    /**
     * 商户分类
     */
    private String merchantCategory;

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
    private java.math.BigDecimal longitude;

    /**
     * 纬度
     */
    private java.math.BigDecimal latitude;

    /**
     * 微信小程序AppID
     */
    private String miniAppAppid;

    /**
     * 微信小程序AppSecret（AES加密）
     */
    private String miniAppSecret;

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

    /**
     * 微信支付商户号
     */
    private String wechatPayMchId;

    /**
     * 微信支付API密钥（AES加密）
     */
    private String wechatPayApiKey;

    /**
     * 微信支付证书路径
     */
    private String wechatPayCertPath;

    /**
     * 微信支付证书p12路径
     */
    private String wechatPayCertP12Path;

    /**
     * 微信支付子商户号（分账用）
     */
    private String wechatPaySubMchId;

    /**
     * 微信支付Key（AES加密）
     */
    private String wechatPayKey;

    /**
     * 微信支付证书序列号
     */
    private String wechatPaySerialNo;

    /**
     * 微信支付回调地址
     */
    private String wechatPayNotifyUrl;

    /**
     * 微信退款回调地址
     */
    private String wechatPayRefundNotifyUrl;

    /**
     * 支付宝应用ID
     */
    private String alipayAppId;

    /**
     * 支付宝应用私钥（AES加密）
     */
    private String alipayPrivateKey;

    /**
     * 支付宝公钥（AES加密）
     */
    private String alipayPublicKey;

    /**
     * 支付宝回调地址
     */
    private String alipayNotifyUrl;

    /**
     * 是否启用分账
     */
    private Integer profitSharingEnabled;

    /**
     * 平台分账比例
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
     * 结算周期
     */
    private String settlementCycle;

    /**
     * 最低结算金额
     */
    private BigDecimal minSettlementAmount;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 商户标签
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

    /**
     * 状态
     */
    private String status;

    /**
     * 审核状态
     */
    private String auditStatus;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    private Long auditBy;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 激活时间
     */
    private LocalDateTime activateTime;

    /**
     * 服务过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 总用户数
     */
    private Integer totalUsers;

    /**
     * 总订单数
     */
    private Integer totalOrders;

    /**
     * 总营收
     */
    private BigDecimal totalRevenue;

    /**
     * 访问次数
     */
    private Long visitCount;

    /**
     * 备注
     */
    private String remark;

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
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
