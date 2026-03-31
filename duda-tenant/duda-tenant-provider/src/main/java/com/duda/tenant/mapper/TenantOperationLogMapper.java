package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户操作日志Mapper
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Mapper
public interface TenantOperationLogMapper extends BaseMapper<TenantOperationLog> {
}
