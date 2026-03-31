package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.SupplyOrderDTO;
import com.duda.tenant.api.rpc.SupplyOrderRpc;
import com.duda.tenant.entity.SupplyOrder;
import com.duda.tenant.service.SupplyOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 供应链订单RPC实现
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class SupplyOrderRpcImpl implements SupplyOrderRpc {

    @Autowired
    private SupplyOrderService supplyOrderService;

    @Override
    public SupplyOrderDTO getById(Long id) {
        log.info("RPC调用: getById, orderId={}", id);
        return supplyOrderService.getOrderDTO(id);
    }

    @Override
    public SupplyOrderDTO getByOrderNo(String distributorOrderNo) {
        log.info("RPC调用: getByOrderNo, distributorOrderNo={}", distributorOrderNo);
        return supplyOrderService.getByOrderNo(distributorOrderNo);
    }

    @Override
    public SupplyOrderDTO create(SupplyOrderDTO dto) {
        log.info("RPC调用: create, supplyProductId={}, distributorTenantId={}, orderNo={}",
            dto.getSupplyProductId(), dto.getDistributorTenantId(), dto.getDistributorOrderNo());
        SupplyOrder order = supplyOrderService.createOrder(dto);
        return entityToDto(order);
    }

    @Override
    public Boolean updateStatus(Long id, String orderStatus) {
        log.info("RPC调用: updateStatus, orderId={}, orderStatus={}", id, orderStatus);
        return supplyOrderService.updateStatus(id, orderStatus);
    }

    @Override
    public Boolean pay(Long id, String paymentMethod, String paymentNo) {
        log.info("RPC调用: pay, orderId={}, paymentMethod={}, paymentNo={}",
            id, paymentMethod, paymentNo);
        return supplyOrderService.pay(id, paymentMethod, paymentNo);
    }

    @Override
    public Boolean ship(Long id, String logisticsCompany, String logisticsNo) {
        log.info("RPC调用: ship, orderId={}, logisticsCompany={}, logisticsNo={}",
            id, logisticsCompany, logisticsNo);
        return supplyOrderService.ship(id, logisticsCompany, logisticsNo);
    }

    @Override
    public List<SupplyOrderDTO> listByDistributor(Long distributorTenantId) {
        log.info("RPC调用: listByDistributor, distributorTenantId={}", distributorTenantId);
        return supplyOrderService.listByDistributor(distributorTenantId);
    }

    @Override
    public List<SupplyOrderDTO> listBySupplier(Long supplierTenantId) {
        log.info("RPC调用: listBySupplier, supplierTenantId={}", supplierTenantId);
        return supplyOrderService.listBySupplier(supplierTenantId);
    }

    @Override
    public List<SupplyOrderDTO> listPendingSettlement(Long distributorTenantId, Long supplierTenantId) {
        log.info("RPC调用: listPendingSettlement, distributorTenantId={}, supplierTenantId={}",
            distributorTenantId, supplierTenantId);
        return supplyOrderService.listPendingSettlement(distributorTenantId, supplierTenantId);
    }

    /**
     * Entity转DTO
     */
    private SupplyOrderDTO entityToDto(SupplyOrder entity) {
        if (entity == null) {
            return null;
        }
        SupplyOrderDTO dto = new SupplyOrderDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
