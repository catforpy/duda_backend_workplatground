package com.duda.user.api.vo;

import com.duda.user.dto.merchant.MerchantDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商户详情VO
 *
 * 返回给前端的商户详细信息
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商户详情VO")
public class MerchantVO extends MerchantDTO {

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "审核状态文本")
    private String auditStatusText;

    @Schema(description = "商户类型文本")
    private String merchantTypeText;

    @Schema(description = "是否已认证")
    private Boolean certified;

    @Schema(description = "注册天数")
    private Integer registerDays;

    @Schema(description = "今日新增用户数")
    private Integer todayNewUsers;

    @Schema(description = "今日新增订单数")
    private Integer todayNewOrders;

    @Schema(description = "今日营收")
    private String todayRevenue;

    public MerchantVO() {
    }

    /**
     * 从MerchantDTO转换为MerchantVO
     * 可以在这里添加额外的计算字段
     */
    public static MerchantVO from(MerchantDTO dto) {
        MerchantVO vo = new MerchantVO();
        // 复制所有字段
        vo.setId(dto.getId());
        vo.setMerchantCode(dto.getMerchantCode());
        vo.setMerchantName(dto.getMerchantName());
        vo.setMerchantShortName(dto.getMerchantShortName());
        vo.setMerchantType(dto.getMerchantType());
        vo.setStatus(dto.getStatus());
        vo.setAuditStatus(dto.getAuditStatus());
        vo.setContactPerson(dto.getContactPerson());
        vo.setContactPhone(dto.getContactPhone());
        vo.setContactEmail(dto.getContactEmail());
        vo.setProvince(dto.getProvince());
        vo.setCity(dto.getCity());
        vo.setDistrict(dto.getDistrict());
        vo.setAddress(dto.getAddress());
        vo.setLogoUrl(dto.getLogoUrl());
        vo.setIndustry(dto.getIndustry());
        vo.setDescription(dto.getDescription());
        vo.setTotalUsers(dto.getTotalUsers());
        vo.setTotalOrders(dto.getTotalOrders());
        vo.setTotalRevenue(dto.getTotalRevenue());
        vo.setTenantId(dto.getTenantId());
        vo.setCreateTime(dto.getCreateTime());
        vo.setUpdateTime(dto.getUpdateTime());
        vo.setVersion(dto.getVersion());

        // 添加计算字段
        if (dto.getCreateTime() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(dto.getCreateTime().toLocalDate(), LocalDateTime.now().toLocalDate());
            vo.registerDays = (int) days;
        }

        // 设置状态文本
        if (dto.getStatus() != null) {
            switch (dto.getStatus()) {
                case "active" -> vo.statusText = "正常";
                case "pending" -> vo.statusText = "待审核";
                case "suspended" -> vo.statusText = "已暂停";
                case "deleted" -> vo.statusText = "已删除";
                default -> vo.statusText = dto.getStatus();
            }
        }

        return vo;
    }
}
