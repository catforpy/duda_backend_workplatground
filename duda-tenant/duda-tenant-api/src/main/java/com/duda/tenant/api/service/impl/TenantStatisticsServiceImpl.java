package com.duda.tenant.api.service.impl;

import com.duda.tenant.api.dto.TenantStatisticsDTO;
import com.duda.tenant.api.rpc.TenantStatisticsRpc;
import com.duda.tenant.api.service.TenantStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 租户统计Service实现类
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@Service
public class TenantStatisticsServiceImpl implements TenantStatisticsService {

    @DubboReference(group = "DUDA_TENANT_GROUP", version = "1.0.0")
    private TenantStatisticsRpc tenantStatisticsRpc;

    @Override
    public TenantStatisticsDTO getByTenantId(Long tenantId) {
        log.debug("查询租户统计: tenantId={}", tenantId);
        // 获取今天的统计数据
        return tenantStatisticsRpc.getStatisticsByDate(tenantId, LocalDate.now());
    }

    @Override
    public List<TenantStatisticsDTO> listByDateRange(Long tenantId, LocalDate startDate, LocalDate endDate) {
        log.debug("查询日期范围统计: tenantId={}, startDate={}, endDate={}",
                tenantId, startDate, endDate);
        return tenantStatisticsRpc.listStatisticsByDateRange(tenantId, startDate, endDate);
    }
}
