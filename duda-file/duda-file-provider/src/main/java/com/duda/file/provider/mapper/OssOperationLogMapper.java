package com.duda.file.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.file.provider.entity.OssOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * OSS操作日志Mapper
 *
 * @author duda
 * @date 2025-03-14
 */
@Mapper
public interface OssOperationLogMapper extends BaseMapper<OssOperationLog> {
}
