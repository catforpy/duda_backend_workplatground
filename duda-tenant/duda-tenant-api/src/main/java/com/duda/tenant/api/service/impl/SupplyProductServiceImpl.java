package com.duda.tenant.api.service.impl;

import com.duda.tenant.api.dto.SupplyProductDTO;
import com.duda.tenant.api.rpc.SupplyProductRpc;
import com.duda.tenant.api.service.SupplyProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 供应链商品Service实现（API层）
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@Service
public class SupplyProductServiceImpl implements SupplyProductService {

    @DubboReference(group = "DUDA_TENANT_GROUP", version = "1.0.0")
    private SupplyProductRpc supplyProductRpc;

    @Override
    public SupplyProductDTO getById(Long id) {
        log.debug("查询商品: id={}", id);
        return supplyProductRpc.getById(id);
    }

    @Override
    public SupplyProductDTO getByProductCode(String productCode) {
        log.debug("根据商品编码查询: productCode={}", productCode);
        return supplyProductRpc.getByProductCode(productCode);
    }

    @Override
    public SupplyProductDTO create(SupplyProductDTO dto) {
        log.info("创建商品: productCode={}, productName={}, supplierTenantId={}",
                dto.getProductCode(), dto.getProductName(), dto.getSupplierTenantId());
        return supplyProductRpc.create(dto);
    }

    @Override
    public SupplyProductDTO update(SupplyProductDTO dto) {
        log.info("更新商品: id={}, productCode={}", dto.getId(), dto.getProductCode());
        return supplyProductRpc.update(dto);
    }

    @Override
    public Boolean delete(Long id) {
        log.info("删除商品: id={}", id);
        return supplyProductRpc.delete(id);
    }

    @Override
    public List<SupplyProductDTO> listBySupplier(Long supplierTenantId) {
        log.debug("查询供应商商品列表: supplierTenantId={}", supplierTenantId);
        return supplyProductRpc.listBySupplier(supplierTenantId);
    }

    @Override
    public Boolean updateStock(Long id, Integer quantity) {
        log.info("更新库存: id={}, quantity={}", id, quantity);
        return supplyProductRpc.updateStock(id, quantity);
    }

    @Override
    public List<SupplyProductDTO> listMarketProducts() {
        log.debug("查询商品市场");
        return supplyProductRpc.listMarketProducts();
    }
}
