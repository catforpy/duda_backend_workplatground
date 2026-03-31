package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.TenantStatisticsDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 租户统计RPC接口
 *
 * @author Claude Code
 * @since 2026-03-28
 */
public interface TenantStatisticsRpc {

    /**
     * 根据租户ID和统计日期查询统计
     *
     * @param tenantId 租户ID
     * @param statisticsDate 统计日期
     * @return 统计DTO
     */
    TenantStatisticsDTO getStatisticsByDate(Long tenantId, LocalDate statisticsDate);

    /**
     * 根据租户ID和日期范围查询统计列表
     *
     * @param tenantId 租户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计列表
     */
    List<TenantStatisticsDTO> listStatisticsByDateRange(Long tenantId, LocalDate startDate, LocalDate endDate);

    /**
     * 生成当日统计数据
     *
     * @param tenantId 租户ID
     * @param statisticsDate 统计日期
     * @return 统计DTO
     */
    TenantStatisticsDTO generateDailyStatistics(Long tenantId, LocalDate statisticsDate);

    /**
     * 获取租户统计概览
     *
     * @param tenantId 租户ID
     * @param days 统计天数
     * @return 统计概览
     */
    Map<String, Object> getStatisticsOverview(Long tenantId, Integer days);
}
