package com.duda.tenant.api.controller;

import com.duda.tenant.api.dto.TenantStatisticsDTO;
import com.duda.tenant.api.service.TenantStatisticsService;
import com.duda.tenant.api.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 租户统计Controller
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
public class TenantStatisticsController {

    @Autowired
    private TenantStatisticsService tenantStatisticsService;

    /**
     * 根据租户ID查询统计数据
     *
     * @param tenantId 租户ID
     * @return 统计数据DTO
     */
    @GetMapping("/tenant/{tenantId}")
    public ResultVO<TenantStatisticsDTO> getByTenantId(@PathVariable Long tenantId) {
        log.info("查询租户统计: tenantId={}", tenantId);
        TenantStatisticsDTO statistics = tenantStatisticsService.getByTenantId(tenantId);
        return ResultVO.success(statistics);
    }

    /**
     * 根据日期范围查询统计数据
     *
     * @param tenantId 租户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    @GetMapping("/tenant/{tenantId}/range")
    public ResultVO<List<TenantStatisticsDTO>> listByDateRange(
            @PathVariable Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("查询日期范围统计: tenantId={}, startDate={}, endDate={}",
                tenantId, startDate, endDate);
        List<TenantStatisticsDTO> statistics = tenantStatisticsService.listByDateRange(tenantId, startDate, endDate);
        return ResultVO.success(statistics);
    }
}
