package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户订单Mapper
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Mapper
public interface TenantOrderMapper extends BaseMapper<TenantOrder> {
}
