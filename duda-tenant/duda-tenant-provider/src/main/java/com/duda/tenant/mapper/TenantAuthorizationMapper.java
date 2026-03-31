package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantAuthorization;
import org.apache.ibatis.annotations.Mapper;

/**
 * 授权管理表Mapper
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Mapper
public interface TenantAuthorizationMapper extends BaseMapper<TenantAuthorization> {
}
