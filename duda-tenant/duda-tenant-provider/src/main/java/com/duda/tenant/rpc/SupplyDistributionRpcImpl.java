package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.SupplyDistributionDTO;
import com.duda.tenant.api.rpc.SupplyDistributionRpc;
import com.duda.tenant.entity.SupplyDistribution;
import com.duda.tenant.service.SupplyDistributionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应链分销记录RPC实现
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class SupplyDistributionRpcImpl implements SupplyDistributionRpc {

    @Autowired
    private SupplyDistributionService supplyDistributionService;

    @Override
    public SupplyDistributionDTO getById(Long id) {
        log.info("RPC调用: getById, distributionId={}", id);
        return supplyDistributionService.getDistributionDTO(id);
    }

    @Override
    public SupplyDistributionDTO create(SupplyDistributionDTO dto) {
        log.info("RPC调用: create, supplyProductId={}, distributorTenantId={}",
            dto.getSupplyProductId(), dto.getDistributorTenantId());
        SupplyDistribution distribution = supplyDistributionService.createDistribution(dto);
        return entityToDto(distribution);
    }

    @Override
    public Boolean updateSalePrice(Long id, BigDecimal salePrice) {
        log.info("RPC调用: updateSalePrice, distributionId={}, salePrice={}", id, salePrice);
        return supplyDistributionService.updateSalePrice(id, salePrice);
    }

    @Override
    public Boolean pause(Long id) {
        log.info("RPC调用: pause, distributionId={}", id);
        return supplyDistributionService.pause(id);
    }

    @Override
    public Boolean activate(Long id) {
        log.info("RPC调用: activate, distributionId={}", id);
        return supplyDistributionService.activate(id);
    }

    @Override
    public Boolean terminate(Long id, String reason) {
        log.info("RPC调用: terminate, distributionId={}, reason={}", id, reason);
        return supplyDistributionService.terminate(id, reason);
    }

    @Override
    public List<SupplyDistributionDTO> listByDistributor(Long distributorTenantId) {
        log.info("RPC调用: listByDistributor, distributorTenantId={}", distributorTenantId);
        return supplyDistributionService.listByDistributor(distributorTenantId);
    }

    @Override
    public List<SupplyDistributionDTO> listByProduct(Long supplyProductId) {
        log.info("RPC调用: listByProduct, supplyProductId={}", supplyProductId);
        return supplyDistributionService.listByProduct(supplyProductId);
    }

    /**
     * Entity转DTO
     */
    private SupplyDistributionDTO entityToDto(SupplyDistribution entity) {
        if (entity == null) {
            return null;
        }
        SupplyDistributionDTO dto = new SupplyDistributionDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
