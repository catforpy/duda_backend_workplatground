package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.SupplyProductDTO;
import com.duda.tenant.entity.SupplyProduct;

import java.util.List;

/**
 * 供应链商品服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface SupplyProductService extends IService<SupplyProduct> {

    /**
     * 创建供应链商品
     *
     * @param dto 商品DTO
     * @return 创建的商品
     */
    SupplyProduct createProduct(SupplyProductDTO dto);

    /**
     * 更新供应链商品
     *
     * @param dto 商品DTO
     * @return 更新后的商品
     */
    SupplyProduct updateProduct(SupplyProductDTO dto);

    /**
     * 根据ID查询商品
     *
     * @param id 商品ID
     * @return 商品DTO
     */
    SupplyProductDTO getProductDTO(Long id);

    /**
     * 根据商品编码查询
     *
     * @param productCode 商品编码
     * @return 商品DTO
     */
    SupplyProductDTO getByProductCode(String productCode);

    /**
     * 查询供应商的商品列表
     *
     * @param supplierTenantId 供应商租户ID
     * @return 商品列表
     */
    List<SupplyProductDTO> listBySupplier(Long supplierTenantId);

    /**
     * 查询商品市场（所有审核通过的商品）
     *
     * @return 商品列表
     */
    List<SupplyProductDTO> listMarketProducts();

    /**
     * 更新库存数量
     *
     * @param id 商品ID
     * @param quantity 库存变化量（可为负数）
     * @return 是否成功
     */
    Boolean updateStock(Long id, Integer quantity);

    /**
     * 审核商品
     *
     * @param id 商品ID
     * @param approved 是否通过
     * @param reviewerId 审核员ID
     * @param reason 审核原因
     * @return 是否成功
     */
    Boolean review(Long id, Boolean approved, Long reviewerId, String reason);
}
