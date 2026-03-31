package com.duda.tenant.api.service;

import com.duda.tenant.api.dto.SupplyDistributionDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应链分销Service接口（API层）
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface SupplyDistributionService {

    /**
     * 根据ID查询分销记录
     *
     * @param id 分销记录ID
     * @return 分销记录DTO
     */
    SupplyDistributionDTO getById(Long id);

    /**
     * 创建分销记录（一键上架）
     *
     * @param dto 分销记录DTO
     * @return 创建的分销记录DTO
     */
    SupplyDistributionDTO create(SupplyDistributionDTO dto);

    /**
     * 更新销售价
     *
     * @param id 分销记录ID
     * @param salePrice 新销售价
     * @return 是否成功
     */
    Boolean updateSalePrice(Long id, BigDecimal salePrice);

    /**
     * 暂停销售
     *
     * @param id 分销记录ID
     * @return 是否成功
     */
    Boolean pause(Long id);

    /**
     * 恢复销售
     *
     * @param id 分销记录ID
     * @return 是否成功
     */
    Boolean activate(Long id);

    /**
     * 终止分销
     *
     * @param id 分销记录ID
     * @param reason 终止原因
     * @return 是否成功
     */
    Boolean terminate(Long id, String reason);

    /**
     * 查询分销商的分销记录列表
     *
     * @param distributorTenantId 分销商租户ID
     * @return 分销记录列表
     */
    List<SupplyDistributionDTO> listByDistributor(Long distributorTenantId);

    /**
     * 查询商品的分销记录列表
     *
     * @param supplyProductId 供应链商品ID
     * @return 分销记录列表
     */
    List<SupplyDistributionDTO> listByProduct(Long supplyProductId);
}
