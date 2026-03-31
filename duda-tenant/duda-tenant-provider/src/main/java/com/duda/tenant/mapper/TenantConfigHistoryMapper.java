package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantConfigHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户配置变更历史Mapper
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Mapper
public interface TenantConfigHistoryMapper extends BaseMapper<TenantConfigHistory> {
}
