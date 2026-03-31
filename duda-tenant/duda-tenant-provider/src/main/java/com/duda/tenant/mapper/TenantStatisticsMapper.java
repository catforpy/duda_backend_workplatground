package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户统计Mapper
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Mapper
public interface TenantStatisticsMapper extends BaseMapper<TenantStatistics> {
}
