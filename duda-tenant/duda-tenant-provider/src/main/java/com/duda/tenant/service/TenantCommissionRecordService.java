package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantCommissionRecordDTO;
import com.duda.tenant.entity.TenantCommissionRecord;

/**
 * 提成记录表服务接口（已废弃）
 *
 * @author Claude Code
 * @since 2026-03-30
 * @deprecated 使用TenantCommissionDetailService替代
 */
public interface TenantCommissionRecordService extends IService<TenantCommissionRecord> {

    /**
     * 创建提成记录
     */
    TenantCommissionRecord create(TenantCommissionRecordDTO dto);
}
