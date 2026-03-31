package com.duda.user.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商户VO
 *
 * 用于返回给前端的数据展示
 * 包含关联信息（如租户名称）
 * 只包含前端需要的展示字段
 *
 * @author Claude
 * @date 2026-03-27
 */
@Data
@Schema(description = "商户信息VO")
public class MerchantVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商户ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "商户编码")
    private String merchantCode;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户简称")
    private String merchantShortName;

    @Schema(description = "商户类型", example = "mini_program")
    private String merchantType;

    @Schema(description = "商户类型名称")
    private String merchantTypeName;

    @Schema(description = "商户分类", example = "retail")
    private String merchantCategory;

    @Schema(description = "商户分类名称")
    private String merchantCategoryName;

    @Schema(description = "联系人姓名")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "所属公司名称")
    private String companyName;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String district;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "完整地址（省市区+详细地址）")
    private String fullAddress;

    @Schema(description = "小程序AppID")
    private String miniAppAppid;

    @Schema(description = "小程序名称")
    private String miniAppName;

    @Schema(description = "小程序头像URL")
    private String miniAppAvatar;

    @Schema(description = "小程序二维码URL")
    private String miniAppQrcode;

    @Schema(description = "是否配置微信支付", example = "true")
    private Boolean hasWechatPay;

    @Schema(description = "是否配置支付宝", example = "true")
    private Boolean hasAlipay;

    @Schema(description = "是否启用分账", example = "false")
    private Boolean profitSharingEnabled;

    @Schema(description = "分账比例", example = "0.10")
    private BigDecimal profitSharingRate;

    @Schema(description = "结算周期", example = "T+1")
    private String settlementCycle;

    @Schema(description = "所属行业")
    private String industry;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "商户描述")
    private String description;

    @Schema(description = "商户状态", example = "active")
    private String status;

    @Schema(description = "商户状态名称")
    private String statusName;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "审核状态名称")
    private String auditStatusName;

    @Schema(description = "注册时间")
    private Date registerTime;

    @Schema(description = "激活时间")
    private Date activateTime;

    @Schema(description = "过期时间")
    private Date expireTime;

    @Schema(description = "是否已过期")
    private Boolean expired;

    @Schema(description = "总用户数", example = "0")
    private Integer totalUsers;

    @Schema(description = "总订单数", example = "0")
    private Integer totalOrders;

    @Schema(description = "总营收", example = "0.00")
    private BigDecimal totalRevenue;

    @Schema(description = "访问次数", example = "0")
    private Long visitCount;

    @Schema(description = "最后登录时间")
    private Date lastLoginTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建人名称")
    private String createByName;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新人名称")
    private String updateByName;
}
