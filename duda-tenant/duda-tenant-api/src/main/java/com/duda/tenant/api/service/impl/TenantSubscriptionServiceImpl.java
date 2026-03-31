package com.duda.tenant.api.service.impl;

import com.duda.tenant.api.dto.TenantSubscriptionDTO;
import com.duda.tenant.api.dto.TenantSubscriptionLimitDTO;
import com.duda.tenant.api.rpc.TenantSubscriptionRpc;
import com.duda.tenant.api.service.TenantSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租户订阅Service实现（API层）
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@Service
public class TenantSubscriptionServiceImpl implements TenantSubscriptionService {

    @DubboReference(group = "DUDA_TENANT_GROUP", version = "1.0.0")
    private TenantSubscriptionRpc tenantSubscriptionRpc;

    @Override
    public TenantSubscriptionDTO getById(Long id) {
        log.debug("查询订阅: id={}", id);
        return tenantSubscriptionRpc.getSubscriptionById(id);
    }

    @Override
    public TenantSubscriptionDTO getBySubscriptionCode(String subscriptionCode) {
        log.debug("根据订阅编号查询: subscriptionCode={}", subscriptionCode);
        return tenantSubscriptionRpc.getSubscriptionByCode(subscriptionCode);
    }

    @Override
    public List<TenantSubscriptionDTO> listByTenantId(Long tenantId) {
        log.debug("查询租户的所有订阅: tenantId={}", tenantId);
        return tenantSubscriptionRpc.listSubscriptionsByTenantId(tenantId);
    }

    @Override
    public List<TenantSubscriptionDTO> listByUserId(Long userId) {
        log.debug("查询用户的所有订阅: userId={}", userId);
        return tenantSubscriptionRpc.listSubscriptionsByUserId(userId);
    }

    @Override
    public TenantSubscriptionDTO getActiveSubscription(Long tenantId, Long userId) {
        log.debug("查询生效中的订阅: tenantId={}, userId={}", tenantId, userId);
        return tenantSubscriptionRpc.getActiveSubscription(tenantId, userId);
    }

    @Override
    public TenantSubscriptionDTO create(TenantSubscriptionDTO subscriptionDTO) {
        log.info("创建订阅: subscriptionCode={}, tenantId={}, userId={}",
                subscriptionDTO.getSubscriptionCode(),
                subscriptionDTO.getTenantId(),
                subscriptionDTO.getUserId());
        return tenantSubscriptionRpc.createSubscription(subscriptionDTO);
    }

    @Override
    public TenantSubscriptionDTO update(TenantSubscriptionDTO subscriptionDTO) {
        log.info("更新订阅: id={}", subscriptionDTO.getId());
        return tenantSubscriptionRpc.updateSubscription(subscriptionDTO);
    }

    @Override
    public Boolean cancel(Long id, String cancelReason, Long cancelBy) {
        log.info("取消订阅: id={}, cancelBy={}", id, cancelBy);
        return tenantSubscriptionRpc.cancelSubscription(id, cancelReason, cancelBy);
    }

    @Override
    public Boolean suspend(Long id) {
        log.info("暂停订阅: id={}", id);
        return tenantSubscriptionRpc.suspendSubscription(id);
    }

    @Override
    public Boolean activate(Long id) {
        log.info("激活订阅: id={}", id);
        return tenantSubscriptionRpc.activateSubscription(id);
    }

    @Override
    public Boolean renew(Long id, Integer months) {
        log.info("续费订阅: id={}, months={}", id, months);
        return tenantSubscriptionRpc.renewSubscription(id, months);
    }

    @Override
    public List<TenantSubscriptionLimitDTO> listLimits(Long subscriptionId) {
        log.debug("查询订阅的所有限制条件: subscriptionId={}", subscriptionId);
        return tenantSubscriptionRpc.listSubscriptionLimits(subscriptionId);
    }

    @Override
    public List<TenantSubscriptionDTO> listExpiringSoon() {
        log.debug("查询即将到期的订阅（7天内）");
        // 这里可以在Provider层添加一个RPC方法，或者查询所有后再过滤
        // 暂时返回空列表，实际使用时需要补充
        return List.of();
    }
}
