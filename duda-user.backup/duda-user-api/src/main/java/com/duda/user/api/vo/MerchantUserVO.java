package com.duda.user.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商户用户VO
 *
 * @author Claude
 * @date 2026-03-27
 */
@Data
@Schema(description = "商户用户信息VO")
public class MerchantUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户用户ID")
    private String merchantUserId;

    @Schema(description = "平台用户ID")
    private Long platformUserId;

    @Schema(description = "平台用户账号")
    private String platformUserAccount;

    @Schema(description = "平台用户昵称")
    private String platformUserNickname;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别", example = "1")
    private Byte gender;

    @Schema(description = "性别名称")
    private String genderName;

    @Schema(description = "生日")
    private Date birthday;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "来源类型", example = "mini_program")
    private String sourceType;

    @Schema(description = "来源类型名称")
    private String sourceTypeName;

    @Schema(description = "来源渠道")
    private String sourceChannel;

    @Schema(description = "用户标签")
    private String userTags;

    @Schema(description = "用户备注")
    private String userRemark;

    @Schema(description = "用户等级", example = "normal")
    private String userLevel;

    @Schema(description = "用户等级名称")
    private String userLevelName;

    @Schema(description = "用户分组")
    private String userGroup;

    @Schema(description = "用户积分", example = "0")
    private Integer userScore;

    @Schema(description = "用户余额", example = "0.00")
    private BigDecimal userBalance;

    @Schema(description = "状态", example = "1")
    private Byte status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "关注状态", example = "0")
    private Byte followStatus;

    @Schema(description = "关注状态名称")
    private String followStatusName;

    @Schema(description = "订阅状态", example = "0")
    private Byte subscribeStatus;

    @Schema(description = "订阅状态名称")
    private String subscribeStatusName;

    @Schema(description = "访问次数", example = "0")
    private Integer visitCount;

    @Schema(description = "订单数量", example = "0")
    private Integer orderCount;

    @Schema(description = "消费金额", example = "0.00")
    private BigDecimal consumptionAmount;

    @Schema(description = "最后访问时间")
    private Date lastVisitTime;

    @Schema(description = "首次访问时间")
    private Date firstVisitTime;

    @Schema(description = "绑定时间")
    private Date bindTime;

    @Schema(description = "绑定天数")
    private Long bindDays;

    @Schema(description = "最后下单时间")
    private Date lastOrderTime;

    @Schema(description = "最后支付时间")
    private Date lastPayTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
