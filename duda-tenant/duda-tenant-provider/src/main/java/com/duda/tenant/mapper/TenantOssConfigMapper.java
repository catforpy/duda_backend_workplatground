package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantOssConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * OSS配置表Mapper
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Mapper
public interface TenantOssConfigMapper extends BaseMapper<TenantOssConfig> {
}
