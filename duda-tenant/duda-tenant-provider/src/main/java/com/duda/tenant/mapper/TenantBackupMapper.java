package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantBackup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户备份Mapper
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Mapper
public interface TenantBackupMapper extends BaseMapper<TenantBackup> {
}
