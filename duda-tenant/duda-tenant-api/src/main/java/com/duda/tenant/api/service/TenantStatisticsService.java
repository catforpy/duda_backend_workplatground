package com.duda.tenant.api.service;

import com.duda.tenant.api.dto.TenantStatisticsDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 租户统计Service接口
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface TenantStatisticsService {

    /**
     * 根据租户ID查询统计数据
     *
     * @param tenantId 租户ID
     * @return 统计数据DTO
     */
    TenantStatisticsDTO getByTenantId(Long tenantId);

    /**
     * 根据日期范围查询统计数据
     *
     * @param tenantId 租户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    List<TenantStatisticsDTO> listByDateRange(Long tenantId, LocalDate startDate, LocalDate endDate);
}
