package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.entity.TenantStatistics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 租户统计服务接口
 *
 * @author Claude Code
 * @since 2026-03-28
 */
public interface TenantStatisticsService extends IService<TenantStatistics> {

    /**
     * 创建统计记录
     *
     * @param statistics 统计信息
     * @return 创建的统计记录
     */
    TenantStatistics createStatistics(TenantStatistics statistics);

    /**
     * 更新统计记录
     *
     * @param statistics 统计信息
     * @return 更新后的统计记录
     */
    TenantStatistics updateStatistics(TenantStatistics statistics);

    /**
     * 根据租户ID和统计日期查询
     *
     * @param tenantId 租户ID
     * @param statisticsDate 统计日期
     * @return 统计信息
     */
    TenantStatistics getByTenantIdAndDate(Long tenantId, LocalDate statisticsDate);

    /**
     * 根据租户ID查询统计数据列表
     *
     * @param tenantId 租户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计列表
     */
    List<TenantStatistics> listByTenantIdAndDateRange(Long tenantId, LocalDate startDate, LocalDate endDate);

    /**
     * 生成当日统计数据
     *
     * @param tenantId 租户ID
     * @param statisticsDate 统计日期
     * @return 统计信息
     */
    TenantStatistics generateDailyStatistics(Long tenantId, LocalDate statisticsDate);

    /**
     * 批量生成所有租户的当日统计数据
     *
     * @param statisticsDate 统计日期
     * @return 生成数量
     */
    Integer generateAllTenantsDailyStatistics(LocalDate statisticsDate);

    /**
     * 获取租户统计概览
     *
     * @param tenantId 租户ID
     * @param days 统计天数
     * @return 统计概览数据
     */
    Map<String, Object> getStatisticsOverview(Long tenantId, Integer days);

    /**
     * 检查并更新API调用次数
     *
     * @param tenantId 租户ID
     * @return 是否超限
     */
    boolean checkAndUpdateApiCalls(Long tenantId);
}
