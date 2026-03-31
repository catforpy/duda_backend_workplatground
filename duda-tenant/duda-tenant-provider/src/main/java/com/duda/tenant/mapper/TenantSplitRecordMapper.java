package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantSplitRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分账记录表Mapper
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Mapper
public interface TenantSplitRecordMapper extends BaseMapper<TenantSplitRecord> {
}
