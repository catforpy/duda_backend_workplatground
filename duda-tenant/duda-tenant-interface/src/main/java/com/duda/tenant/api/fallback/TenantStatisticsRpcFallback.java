package com.duda.tenant.api.fallback;

import com.duda.tenant.api.dto.TenantStatisticsDTO;
import com.duda.tenant.api.rpc.TenantStatisticsRpc;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租户统计RPC降级实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
public class TenantStatisticsRpcFallback implements TenantStatisticsRpc {

    @Override
    public TenantStatisticsDTO getStatisticsByDate(Long tenantId, LocalDate statisticsDate) {
        log.error("TenantStatisticsRpc.getStatisticsByDate降级: tenantId={}, statisticsDate={}",
                tenantId, statisticsDate);
        return null;
    }

    @Override
    public List<TenantStatisticsDTO> listStatisticsByDateRange(Long tenantId, LocalDate startDate, LocalDate endDate) {
        log.error("TenantStatisticsRpc.listStatisticsByDateRange降级: tenantId={}, startDate={}, endDate={}",
                tenantId, startDate, endDate);
        return Collections.emptyList();
    }

    @Override
    public TenantStatisticsDTO generateDailyStatistics(Long tenantId, LocalDate statisticsDate) {
        log.error("TenantStatisticsRpc.generateDailyStatistics降级: tenantId={}, statisticsDate={}",
                tenantId, statisticsDate);
        return null;
    }

    @Override
    public Map<String, Object> getStatisticsOverview(Long tenantId, Integer days) {
        log.error("TenantStatisticsRpc.getStatisticsOverview降级: tenantId={}, days={}", tenantId, days);
        Map<String, Object> overview = new HashMap<>();
        overview.put("error", "租户统计服务不可用");
        return overview;
    }
}
