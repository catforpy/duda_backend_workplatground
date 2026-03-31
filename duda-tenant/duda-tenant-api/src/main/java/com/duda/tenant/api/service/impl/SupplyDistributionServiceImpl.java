package com.duda.tenant.api.service.impl;

import com.duda.tenant.api.dto.SupplyDistributionDTO;
import com.duda.tenant.api.rpc.SupplyDistributionRpc;
import com.duda.tenant.api.service.SupplyDistributionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应链分销Service实现（API层）
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@Service
public class SupplyDistributionServiceImpl implements SupplyDistributionService {

    @DubboReference(group = "DUDA_TENANT_GROUP", version = "1.0.0")
    private SupplyDistributionRpc supplyDistributionRpc;

    @Override
    public SupplyDistributionDTO getById(Long id) {
        log.debug("查询分销记录: id={}", id);
        return supplyDistributionRpc.getById(id);
    }

    @Override
    public SupplyDistributionDTO create(SupplyDistributionDTO dto) {
        log.info("创建分销记录(API层): supplyProductId={}, distributorTenantId={}, stockMode={}, salePrice={}",
                dto.getSupplyProductId(), dto.getDistributorTenantId(), dto.getStockMode(), dto.getSalePrice());
        return supplyDistributionRpc.create(dto);
    }

    @Override
    public Boolean updateSalePrice(Long id, BigDecimal salePrice) {
        log.info("更新销售价: id={}, salePrice={}", id, salePrice);
        return supplyDistributionRpc.updateSalePrice(id, salePrice);
    }

    @Override
    public Boolean pause(Long id) {
        log.info("暂停销售: id={}", id);
        return supplyDistributionRpc.pause(id);
    }

    @Override
    public Boolean activate(Long id) {
        log.info("恢复销售: id={}", id);
        return supplyDistributionRpc.activate(id);
    }

    @Override
    public Boolean terminate(Long id, String reason) {
        log.info("终止分销: id={}, reason={}", id, reason);
        return supplyDistributionRpc.terminate(id, reason);
    }

    @Override
    public List<SupplyDistributionDTO> listByDistributor(Long distributorTenantId) {
        log.debug("查询分销商的分销记录: distributorTenantId={}", distributorTenantId);
        return supplyDistributionRpc.listByDistributor(distributorTenantId);
    }

    @Override
    public List<SupplyDistributionDTO> listByProduct(Long supplyProductId) {
        log.debug("查询商品的分销记录: supplyProductId={}", supplyProductId);
        return supplyDistributionRpc.listByProduct(supplyProductId);
    }
}
