package com.duda.tenant.api.service;

import com.duda.tenant.api.dto.SupplyOrderDTO;

import java.util.List;

/**
 * 供应链订单Service接口（API层）
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface SupplyOrderService {

    /**
     * 根据ID查询订单
     *
     * @param id 订单ID
     * @return 订单DTO
     */
    SupplyOrderDTO getById(Long id);

    /**
     * 根据订单号查询
     *
     * @param distributorOrderNo 分销商订单号
     * @return 订单DTO
     */
    SupplyOrderDTO getByOrderNo(String distributorOrderNo);

    /**
     * 创建订单
     *
     * @param dto 订单DTO
     * @return 创建的订单
     */
    SupplyOrderDTO create(SupplyOrderDTO dto);

    /**
     * 更新订单状态
     *
     * @param id 订单ID
     * @param orderStatus 新订单状态
     * @return 是否成功
     */
    Boolean updateStatus(Long id, String orderStatus);

    /**
     * 支付订单
     *
     * @param id 订单ID
     * @param paymentMethod 支付方式
     * @param paymentNo 支付单号
     * @return 是否成功
     */
    Boolean pay(Long id, String paymentMethod, String paymentNo);

    /**
     * 供应商发货
     *
     * @param id 订单ID
     * @param logisticsCompany 物流公司
     * @param logisticsNo 物流单号
     * @return 是否成功
     */
    Boolean ship(Long id, String logisticsCompany, String logisticsNo);

    /**
     * 查询分销商的订单列表
     *
     * @param distributorTenantId 分销商租户ID
     * @return 订单列表
     */
    List<SupplyOrderDTO> listByDistributor(Long distributorTenantId);

    /**
     * 查询供应商的订单列表
     *
     * @param supplierTenantId 供应商租户ID
     * @return 订单列表
     */
    List<SupplyOrderDTO> listBySupplier(Long supplierTenantId);

    /**
     * 查询待结算订单
     *
     * @param distributorTenantId 分销商租户ID
     * @param supplierTenantId 供应商租户ID
     * @return 待结算订单列表
     */
    List<SupplyOrderDTO> listPendingSettlement(Long distributorTenantId, Long supplierTenantId);

    /**
     * 查询订单统计信息
     *
     * @param distributorTenantId 分销商租户ID
     * @return 统计信息
     */
    String getOrderStatistics(Long distributorTenantId);
}
