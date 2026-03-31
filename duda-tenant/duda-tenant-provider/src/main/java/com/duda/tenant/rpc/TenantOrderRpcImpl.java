package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.TenantOrderDTO;
import com.duda.tenant.api.rpc.TenantOrderRpc;
import com.duda.tenant.entity.TenantOrder;
import com.duda.tenant.service.TenantOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户订单RPC实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantOrderRpcImpl implements TenantOrderRpc {

    @Autowired
    private TenantOrderService tenantOrderService;

    @Override
    public TenantOrderDTO getOrderById(Long orderId) {
        log.info("RPC调用: getOrderById, orderId={}", orderId);
        TenantOrder order = tenantOrderService.getById(orderId);
        return entityToDto(order);
    }

    @Override
    public TenantOrderDTO getOrderByNo(String orderNo) {
        log.info("RPC调用: getOrderByNo, orderNo={}", orderNo);
        TenantOrder order = tenantOrderService.getByOrderNo(orderNo);
        return entityToDto(order);
    }

    @Override
    public List<TenantOrderDTO> listOrdersByTenantId(Long tenantId) {
        log.info("RPC调用: listOrdersByTenantId, tenantId={}", tenantId);
        List<TenantOrder> orders = tenantOrderService.listByTenantId(tenantId);
        return orders.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TenantOrderDTO createOrder(TenantOrderDTO orderDTO) {
        log.info("RPC调用: createOrder, tenantId={}", orderDTO.getTenantId());
        TenantOrder order = dtoToEntity(orderDTO);
        order = tenantOrderService.createOrder(order);
        return entityToDto(order);
    }

    @Override
    public Boolean payOrder(Long orderId, String paymentMethod, String paymentNo) {
        log.info("RPC调用: payOrder, orderId={}, paymentMethod={}", orderId, paymentMethod);
        return tenantOrderService.payOrder(orderId, paymentMethod, paymentNo);
    }

    /**
     * Entity转DTO
     */
    private TenantOrderDTO entityToDto(TenantOrder entity) {
        if (entity == null) {
            return null;
        }
        TenantOrderDTO dto = new TenantOrderDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * DTO转Entity
     */
    private TenantOrder dtoToEntity(TenantOrderDTO dto) {
        if (dto == null) {
            return null;
        }
        TenantOrder entity = new TenantOrder();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
