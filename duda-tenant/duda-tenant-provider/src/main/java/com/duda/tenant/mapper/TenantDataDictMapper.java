package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantDataDict;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户数据字典Mapper
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Mapper
public interface TenantDataDictMapper extends BaseMapper<TenantDataDict> {
}
