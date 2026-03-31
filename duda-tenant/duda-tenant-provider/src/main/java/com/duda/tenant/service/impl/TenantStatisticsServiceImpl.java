package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.tenant.entity.Tenant;
import com.duda.tenant.entity.TenantStatistics;
import com.duda.tenant.mapper.TenantStatisticsMapper;
import com.duda.tenant.service.TenantService;
import com.duda.tenant.service.TenantStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 租户统计服务实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Service
public class TenantStatisticsServiceImpl extends ServiceImpl<TenantStatisticsMapper, TenantStatistics> implements TenantStatisticsService {

    @Autowired
    private TenantService tenantService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantStatistics createStatistics(TenantStatistics statistics) {
        statistics.setCreatedTime(LocalDateTime.now());
        statistics.setLastUpdateTime(LocalDateTime.now());
        save(statistics);
        log.info("创建统计记录成功: statisticsId={}, tenantId={}, statDate={}",
                statistics.getId(), statistics.getTenantId(), statistics.getStatDate());
        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantStatistics updateStatistics(TenantStatistics statistics) {
        statistics.setLastUpdateTime(LocalDateTime.now());
        updateById(statistics);
        log.info("更新统计记录成功: statisticsId={}, tenantId={}, statDate={}",
                statistics.getId(), statistics.getTenantId(), statistics.getStatDate());
        return statistics;
    }

    @Override
    public TenantStatistics getByTenantIdAndDate(Long tenantId, LocalDate statisticsDate) {
        LambdaQueryWrapper<TenantStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantStatistics::getTenantId, tenantId)
                .eq(TenantStatistics::getStatDate, statisticsDate);
        return getOne(wrapper);
    }

    @Override
    public List<TenantStatistics> listByTenantIdAndDateRange(Long tenantId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<TenantStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantStatistics::getTenantId, tenantId)
                .between(TenantStatistics::getStatDate, startDate, endDate)
                .orderByAsc(TenantStatistics::getStatDate);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantStatistics generateDailyStatistics(Long tenantId, LocalDate statisticsDate) {
        Tenant tenant = tenantService.getById(tenantId);
        if (tenant == null) {
            log.warn("生成统计数据失败，租户不存在: tenantId={}", tenantId);
            return null;
        }

        // 检查是否已存在
        TenantStatistics existing = getByTenantIdAndDate(tenantId, statisticsDate);
        if (existing != null) {
            log.info("统计数据已存在，跳过生成: tenantId={}, statDate={}", tenantId, statisticsDate);
            return existing;
        }

        TenantStatistics statistics = new TenantStatistics();
        statistics.setTenantId(tenantId);
        statistics.setStatDate(statisticsDate);
        statistics.setStatHour(null); // 全天统计
        statistics.setDataSource("auto");

        // TODO: 从实际数据源统计
        statistics.setUserCount(0);
        statistics.setMerchantCount(0);
        statistics.setMiniProgramCount(0);
        statistics.setStorageUsedSize(0L);
        statistics.setApiCallCount(0);
        statistics.setOrderCount(0);
        statistics.setOrderAmount(java.math.BigDecimal.ZERO);

        save(statistics);

        log.info("生成统计数据成功: tenantId={}, statDate={}", tenantId, statisticsDate);
        return statistics;
    }

    @Override
    public boolean checkAndUpdateApiCalls(Long tenantId) {
        // 获取今天的统计数据
        LocalDate today = LocalDate.now();
        TenantStatistics statistics = getByTenantIdAndDate(tenantId, today);

        if (statistics == null) {
            // 如果今天没有统计数据，创建一条
            statistics = generateDailyStatistics(tenantId, today);
            if (statistics == null) {
                return false;
            }
        }

        // 更新API调用次数
        statistics.setApiCallCount((statistics.getApiCallCount() != null ? statistics.getApiCallCount() : 0) + 1);
        statistics.setLastUpdateTime(LocalDateTime.now());
        updateById(statistics);

        // TODO: 检查是否超过配额
        // Tenant tenant = tenantService.getById(tenantId);
        // return statistics.getApiCallCount() <= tenant.getMaxApiCallsPerDay();

        return true;
    }

    @Override
    public Integer generateAllTenantsDailyStatistics(LocalDate statisticsDate) {
        // TODO: 批量生成所有租户统计
        log.info("批量生成统计数据: statisticsDate={}", statisticsDate);
        return 0;
    }

    @Override
    public Map<String, Object> getStatisticsOverview(Long tenantId, Integer days) {
        // TODO: 获取租户统计概览
        log.info("获取统计概览: tenantId={}, days={}", tenantId, days);
        return new java.util.HashMap<>();
    }
}
