package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户配置Mapper
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Mapper
public interface TenantConfigMapper extends BaseMapper<TenantConfig> {
}
