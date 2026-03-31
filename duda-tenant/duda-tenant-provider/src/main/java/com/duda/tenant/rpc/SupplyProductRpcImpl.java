package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.SupplyProductDTO;
import com.duda.tenant.api.rpc.SupplyProductRpc;
import com.duda.tenant.entity.SupplyProduct;
import com.duda.tenant.service.SupplyProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 供应链商品RPC实现
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class SupplyProductRpcImpl implements SupplyProductRpc {

    @Autowired
    private SupplyProductService supplyProductService;

    @Override
    public SupplyProductDTO getById(Long id) {
        log.info("RPC调用: getById, productId={}", id);
        return supplyProductService.getProductDTO(id);
    }

    @Override
    public SupplyProductDTO getByProductCode(String productCode) {
        log.info("RPC调用: getByProductCode, productCode={}", productCode);
        return supplyProductService.getByProductCode(productCode);
    }

    @Override
    public SupplyProductDTO create(SupplyProductDTO dto) {
        log.info("RPC调用: create, productName={}, productCode={}",
            dto.getProductName(), dto.getProductCode());
        SupplyProduct product = supplyProductService.createProduct(dto);
        return entityToDto(product);
    }

    @Override
    public SupplyProductDTO update(SupplyProductDTO dto) {
        log.info("RPC调用: update, productId={}", dto.getId());
        SupplyProduct product = supplyProductService.updateProduct(dto);
        return entityToDto(product);
    }

    @Override
    public Boolean delete(Long id) {
        log.info("RPC调用: delete, productId={}", id);
        // 软删除，更新状态为off_sale
        SupplyProductDTO dto = supplyProductService.getProductDTO(id);
        if (dto == null) {
            log.warn("商品不存在，无法删除: productId={}", id);
            return false;
        }
        dto.setStatus("off_sale");
        supplyProductService.updateProduct(dto);
        return true;
    }

    @Override
    public List<SupplyProductDTO> listBySupplier(Long supplierTenantId) {
        log.info("RPC调用: listBySupplier, supplierTenantId={}", supplierTenantId);
        return supplyProductService.listBySupplier(supplierTenantId);
    }

    @Override
    public Boolean updateStock(Long id, Integer quantity) {
        log.info("RPC调用: updateStock, productId={}, quantity={}", id, quantity);
        return supplyProductService.updateStock(id, quantity);
    }

    @Override
    public List<SupplyProductDTO> listMarketProducts() {
        log.info("RPC调用: listMarketProducts");
        return supplyProductService.listMarketProducts();
    }

    /**
     * Entity转DTO
     */
    private SupplyProductDTO entityToDto(SupplyProduct entity) {
        if (entity == null) {
            return null;
        }
        SupplyProductDTO dto = new SupplyProductDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
