package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.TenantStatisticsDTO;
import com.duda.tenant.api.rpc.TenantStatisticsRpc;
import com.duda.tenant.entity.TenantStatistics;
import com.duda.tenant.service.TenantStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户统计RPC实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantStatisticsRpcImpl implements TenantStatisticsRpc {

    @Autowired
    private TenantStatisticsService tenantStatisticsService;

    @Override
    public TenantStatisticsDTO getStatisticsByDate(Long tenantId, LocalDate statisticsDate) {
        log.info("RPC调用: getStatisticsByDate, tenantId={}, statisticsDate={}", tenantId, statisticsDate);
        TenantStatistics statistics = tenantStatisticsService.getByTenantIdAndDate(tenantId, statisticsDate);
        return entityToDto(statistics);
    }

    @Override
    public List<TenantStatisticsDTO> listStatisticsByDateRange(Long tenantId, LocalDate startDate, LocalDate endDate) {
        log.info("RPC调用: listStatisticsByDateRange, tenantId={}, startDate={}, endDate={}",
                tenantId, startDate, endDate);
        List<TenantStatistics> statisticsList = tenantStatisticsService.listByTenantIdAndDateRange(
                tenantId, startDate, endDate);
        return statisticsList.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TenantStatisticsDTO generateDailyStatistics(Long tenantId, LocalDate statisticsDate) {
        log.info("RPC调用: generateDailyStatistics, tenantId={}, statisticsDate={}", tenantId, statisticsDate);
        TenantStatistics statistics = tenantStatisticsService.generateDailyStatistics(tenantId, statisticsDate);
        return entityToDto(statistics);
    }

    @Override
    public Map<String, Object> getStatisticsOverview(Long tenantId, Integer days) {
        log.info("RPC调用: getStatisticsOverview, tenantId={}, days={}", tenantId, days);
        return tenantStatisticsService.getStatisticsOverview(tenantId, days);
    }

    /**
     * Entity转DTO
     */
    private TenantStatisticsDTO entityToDto(TenantStatistics entity) {
        if (entity == null) {
            return null;
        }
        TenantStatisticsDTO dto = new TenantStatisticsDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
