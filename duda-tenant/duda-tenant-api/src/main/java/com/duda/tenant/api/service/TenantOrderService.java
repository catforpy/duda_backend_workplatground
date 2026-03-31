package com.duda.tenant.api.service;

import com.duda.tenant.api.dto.TenantOrderDTO;

import java.util.List;

/**
 * 租户订单Service接口
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface TenantOrderService {

    /**
     * 根据租户ID查询订单列表
     *
     * @param tenantId 租户ID
     * @return 订单列表
     */
    List<TenantOrderDTO> listByTenantId(Long tenantId);

    /**
     * 根据ID查询订单
     *
     * @param id 订单ID
     * @return 订单DTO
     */
    TenantOrderDTO getById(Long id);
}
