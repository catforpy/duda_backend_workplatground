package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantUserRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户用户关系Mapper
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Mapper
public interface TenantUserRelationMapper extends BaseMapper<TenantUserRelation> {
}
