package com.duda.tenant.api.service.impl;

import com.duda.tenant.api.dto.TenantOrderDTO;
import com.duda.tenant.api.rpc.TenantOrderRpc;
import com.duda.tenant.api.service.TenantOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租户订单Service实现类
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@Service
public class TenantOrderServiceImpl implements TenantOrderService {

    @DubboReference(group = "DUDA_TENANT_GROUP", version = "1.0.0")
    private TenantOrderRpc tenantOrderRpc;

    @Override
    public List<TenantOrderDTO> listByTenantId(Long tenantId) {
        log.debug("查询租户订单列表: tenantId={}", tenantId);
        return tenantOrderRpc.listOrdersByTenantId(tenantId);
    }

    @Override
    public TenantOrderDTO getById(Long id) {
        log.debug("查询订单: id={}", id);
        return tenantOrderRpc.getOrderById(id);
    }
}
