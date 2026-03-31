package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantSubscriptionLimit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户订阅限制条件Mapper
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Mapper
public interface TenantSubscriptionLimitMapper extends BaseMapper<TenantSubscriptionLimit> {
}
