package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.SupplyProductDTO;

import java.util.List;

/**
 * 供应链商品RPC接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface SupplyProductRpc {

    /**
     * 根据ID查询供应链商品
     *
     * @param id 商品ID
     * @return 商品DTO
     */
    SupplyProductDTO getById(Long id);

    /**
     * 根据商品编码查询
     *
     * @param productCode 商品编码
     * @return 商品DTO
     */
    SupplyProductDTO getByProductCode(String productCode);

    /**
     * 创建供应链商品
     *
     * @param dto 商品DTO
     * @return 创建的商品DTO
     */
    SupplyProductDTO create(SupplyProductDTO dto);

    /**
     * 更新供应链商品
     *
     * @param dto 商品DTO
     * @return 更新后的商品DTO
     */
    SupplyProductDTO update(SupplyProductDTO dto);

    /**
     * 删除供应链商品
     *
     * @param id 商品ID
     * @return 是否成功
     */
    Boolean delete(Long id);

    /**
     * 查询供应商的商品列表
     *
     * @param supplierTenantId 供应商租户ID
     * @return 商品列表
     */
    List<SupplyProductDTO> listBySupplier(Long supplierTenantId);

    /**
     * 更新库存数量
     *
     * @param id 商品ID
     * @param quantity 库存变化量（可为负数）
     * @return 是否成功
     */
    Boolean updateStock(Long id, Integer quantity);

    /**
     * 查询商品市场（所有审核通过的商品）
     *
     * @return 商品列表
     */
    List<SupplyProductDTO> listMarketProducts();
}
