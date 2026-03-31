package com.duda.user.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商户列表项VO
 *
 * 用于商户列表页面的简化展示
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商户列表项VO")
public class MerchantListVO extends BaseListVO {

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户编码")
    private String merchantCode;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户简称")
    private String merchantShortName;

    @Schema(description = "商户类型")
    private String merchantType;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "总用户数")
    private Integer totalUsers;

    @Schema(description = "总订单数")
    private Integer totalOrders;

    @Schema(description = "总营收")
    private String totalRevenue;

    @Schema(description = "访问次数")
    private Long visitCount;
}
