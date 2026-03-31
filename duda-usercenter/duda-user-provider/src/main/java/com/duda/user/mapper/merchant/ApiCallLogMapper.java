package com.duda.user.mapper.merchant;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.merchant.ApiCallLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * API调用日志Mapper
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Mapper
public interface ApiCallLogMapper extends BaseMapper<ApiCallLog> {

}
