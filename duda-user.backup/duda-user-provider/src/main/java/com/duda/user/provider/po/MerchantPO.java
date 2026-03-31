package com.duda.user.provider.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.duda.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商户PO
 *
 * 表名: merchants
 * 说明: 商户信息表，包含商户基本信息、联系方式、支付配置、微信配置等
 * 租户隔离: 是（通过tenant_id字段）
 * 乐观锁: 是（version字段）
 *
 * @author Claude
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchants")
public class MerchantPO extends BaseEntity {

    /**
     * 商户ID（主键）
     */
    private Long id;

    /**
     * 租户ID（租户隔离字段）
     */
    private Long tenantId;

    /**
     * 商户编码（租户内唯一）
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
     * mini_program-小程序, app-App应用, web-网站, h5-H5应用, desktop-桌面应用
     */
    private String merchantType;

    /**
     * 商户分类
     * retail-零售, catering-餐饮, service-服务, education-教育, medical-医疗, other-其他
     */
    private String merchantCategory;

    /**
     * 联系人姓名
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
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
     * 所属公司ID（关联user_nexus.companies表）
     */
    private Long companyId;

    /**
     * 所属公司名称（冗余字段）
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
     * 法人姓名
     */
    private String legalPerson;

    /**
     * 法人身份证号
     */
    private String legalPersonId;

    /**
     * 省份
     */
    private String province;

    /**
     * 省份编码（行政区划代码）
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

    /**
     * 小程序AppID
     */
    private String miniAppAppid;

    /**
     * 小程序AppSecret（AES加密）
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
     * 小程序标签（逗号分隔）
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
     * 微信支付子商户号
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
     * 微信支付回调通知URL
     */
    private String wechatPayNotifyUrl;

    /**
     * 微信支付退款回调URL
     */
    private String wechatPayRefundNotifyUrl;

    /**
     * 支付宝AppID
     */
    private String alipayAppId;

    /**
     * 支付宝私钥（AES加密）
     */
    private String alipayPrivateKey;

    /**
     * 支付宝公钥（AES加密）
     */
    private String alipayPublicKey;

    /**
     * 支付宝回调通知URL
     */
    private String alipayNotifyUrl;

    /**
     * 是否启用分账
     * 0-否, 1-是
     */
    private Integer profitSharingEnabled;

    /**
     * 分账比例
     * 范围: 0.00-1.00
     */
    private BigDecimal profitSharingRate;

    /**
     * 分账账号
     */
    private String profitSharingAccount;

    /**
     * 分账账号名称
     */
    private String profitSharingName;

    /**
     * 结算周期
     * T+0-实时, T+1-次日, T+7-周结, M+1-月结
     */
    private String settlementCycle;

    /**
     * 最小结算金额
     */
    private BigDecimal minSettlementAmount;

    /**
     * 经营范围
     */
    private String businessScope;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 关键词（逗号分隔，用于搜索）
     */
    private String keywords;

    /**
     * 商户描述
     */
    private String description;

    /**
     * 扩展配置（JSON格式）
     * 示例: {"custom_field1": "value1", "custom_field2": "value2"}
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object extConfig;

    /**
     * 营业时间（JSON格式）
     * 示例: {"monday": "09:00-18:00", "tuesday": "09:00-18:00"}
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object businessHours;

    /**
     * 服务配置（JSON格式）
     * 示例: {"auto_reply": true, "welcome_message": "欢迎光临"}
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object serviceConfig;

    /**
     * 商户状态
     * pending-待审核, active-已激活, suspended-已暂停, deleted-已删除
     */
    private String status;

    /**
     * 审核状态
     * pending-待审核, approved-已通过, rejected-已拒绝
     */
    private String auditStatus;

    /**
     * 审核时间
     */
    private Date auditTime;

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
    private Date registerTime;

    /**
     * 激活时间
     */
    private Date activateTime;

    /**
     * 过期时间（NULL表示永久有效）
     */
    private Date expireTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

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
     * 删除标记
     * 0-正常, 1-已删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 版本号（乐观锁）
     * 每次更新自动+1
     */
    @Version
    private Integer version;
}
