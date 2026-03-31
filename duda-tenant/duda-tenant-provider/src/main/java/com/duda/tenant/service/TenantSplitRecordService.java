package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantSplitRecordDTO;
import com.duda.tenant.entity.TenantSplitRecord;

import java.util.List;

/**
 * 分账记录表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantSplitRecordService extends IService<TenantSplitRecord> {

    /**
     * 创建分账记录
     */
    TenantSplitRecord createRecord(TenantSplitRecordDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantSplitRecordDTO getRecordDTO(Long id);

    /**
     * 查询订单的分账记录
     */
    List<TenantSplitRecordDTO> listByOrderId(Long orderId);

    /**
     * 查询合作的分账记录
     */
    List<TenantSplitRecordDTO> listByCooperation(Long cooperationId);
}
