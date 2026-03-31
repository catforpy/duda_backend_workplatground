package com.duda.tenant.api.service.impl;

import com.duda.tenant.api.dto.SupplyOrderDTO;
import com.duda.tenant.api.rpc.SupplyOrderRpc;
import com.duda.tenant.api.service.SupplyOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 供应链订单Service实现（API层）
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@Service
public class SupplyOrderServiceImpl implements SupplyOrderService {

    @DubboReference(group = "DUDA_TENANT_GROUP", version = "1.0.0")
    private SupplyOrderRpc supplyOrderRpc;

    @Override
    public SupplyOrderDTO getById(Long id) {
        log.debug("查询订单: id={}", id);
        return supplyOrderRpc.getById(id);
    }

    @Override
    public SupplyOrderDTO getByOrderNo(String distributorOrderNo) {
        log.debug("根据订单号查询: orderNo={}", distributorOrderNo);
        return supplyOrderRpc.getByOrderNo(distributorOrderNo);
    }

    @Override
    public SupplyOrderDTO create(SupplyOrderDTO dto) {
        log.info("创建订单: distributorOrderNo={}, supplierTenantId={}, distributorTenantId={}",
                dto.getDistributorOrderNo(), dto.getSupplierTenantId(), dto.getDistributorTenantId());
        return supplyOrderRpc.create(dto);
    }

    @Override
    public Boolean updateStatus(Long id, String orderStatus) {
        log.info("更新订单状态: id={}, orderStatus={}", id, orderStatus);
        return supplyOrderRpc.updateStatus(id, orderStatus);
    }

    @Override
    public Boolean pay(Long id, String paymentMethod, String paymentNo) {
        log.info("支付订单: id={}, paymentMethod={}, paymentNo={}", id, paymentMethod, paymentNo);
        return supplyOrderRpc.pay(id, paymentMethod, paymentNo);
    }

    @Override
    public Boolean ship(Long id, String logisticsCompany, String logisticsNo) {
        log.info("供应商发货: id={}, logisticsCompany={}, logisticsNo={}",
                id, logisticsCompany, logisticsNo);
        return supplyOrderRpc.ship(id, logisticsCompany, logisticsNo);
    }

    @Override
    public List<SupplyOrderDTO> listByDistributor(Long distributorTenantId) {
        log.debug("查询分销商订单: distributorTenantId={}", distributorTenantId);
        return supplyOrderRpc.listByDistributor(distributorTenantId);
    }

    @Override
    public List<SupplyOrderDTO> listBySupplier(Long supplierTenantId) {
        log.debug("查询供应商订单: supplierTenantId={}", supplierTenantId);
        return supplyOrderRpc.listBySupplier(supplierTenantId);
    }

    @Override
    public List<SupplyOrderDTO> listPendingSettlement(Long distributorTenantId, Long supplierTenantId) {
        log.debug("查询待结算订单: distributorTenantId={}, supplierTenantId={}",
                distributorTenantId, supplierTenantId);
        return supplyOrderRpc.listPendingSettlement(distributorTenantId, supplierTenantId);
    }

    @Override
    public String getOrderStatistics(Long distributorTenantId) {
        log.debug("查询订单统计: distributorTenantId={}", distributorTenantId);
        // TODO: 实现订单统计逻辑
        return "订单统计功能待实现";
    }
}
