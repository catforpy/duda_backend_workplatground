package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantCommissionRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提成记录表Mapper（已废弃）
 *
 * @author Claude Code
 * @since 2026-03-30
 * @deprecated 使用TenantCommissionDetailMapper替代
 */
@Mapper
@Deprecated
public interface TenantCommissionRecordMapper extends BaseMapper<TenantCommissionRecord> {
}
