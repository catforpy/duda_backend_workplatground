package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.TenantSubscriptionDTO;
import com.duda.tenant.api.dto.TenantSubscriptionLimitDTO;
import com.duda.tenant.api.rpc.TenantSubscriptionRpc;
import com.duda.tenant.entity.TenantSubscription;
import com.duda.tenant.service.TenantSubscriptionLimitService;
import com.duda.tenant.service.TenantSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 租户订阅RPC实现
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantSubscriptionRpcImpl implements TenantSubscriptionRpc {

    @Autowired
    private TenantSubscriptionService subscriptionService;

    @Autowired
    private TenantSubscriptionLimitService limitService;

    @Override
    public TenantSubscriptionDTO getSubscriptionById(Long subscriptionId) {
        log.info("RPC调用: getSubscriptionById, subscriptionId={}", subscriptionId);
        return subscriptionService.getSubscriptionDTO(subscriptionId);
    }

    @Override
    public TenantSubscriptionDTO getSubscriptionByCode(String subscriptionCode) {
        log.info("RPC调用: getSubscriptionByCode, subscriptionCode={}", subscriptionCode);
        return subscriptionService.getBySubscriptionCode(subscriptionCode);
    }

    @Override
    public List<TenantSubscriptionDTO> listSubscriptionsByTenantId(Long tenantId) {
        log.info("RPC调用: listSubscriptionsByTenantId, tenantId={}", tenantId);
        return subscriptionService.listByTenantId(tenantId);
    }

    @Override
    public List<TenantSubscriptionDTO> listSubscriptionsByUserId(Long userId) {
        log.info("RPC调用: listSubscriptionsByUserId, userId={}", userId);
        return subscriptionService.listByUserId(userId);
    }

    @Override
    public List<TenantSubscriptionDTO> listSubscriptionsByTenantAndUser(Long tenantId, Long userId) {
        log.info("RPC调用: listSubscriptionsByTenantAndUser, tenantId={}, userId={}", tenantId, userId);
        return subscriptionService.listByTenantAndUser(tenantId, userId);
    }

    @Override
    public TenantSubscriptionDTO getActiveSubscription(Long tenantId, Long userId) {
        log.info("RPC调用: getActiveSubscription, tenantId={}, userId={}", tenantId, userId);
        return subscriptionService.getActiveSubscription(tenantId, userId);
    }

    @Override
    public TenantSubscriptionDTO createSubscription(TenantSubscriptionDTO subscriptionDTO) {
        log.info("RPC调用: createSubscription, tenantId={}, userId={}",
                subscriptionDTO.getTenantId(), subscriptionDTO.getUserId());
        TenantSubscription entity = dtoToEntity(subscriptionDTO);
        TenantSubscription created = subscriptionService.createSubscription(entity);
        // 转换为DTO返回
        return subscriptionService.getSubscriptionDTO(created.getId());
    }

    @Override
    public TenantSubscriptionDTO updateSubscription(TenantSubscriptionDTO subscriptionDTO) {
        log.info("RPC调用: updateSubscription, id={}", subscriptionDTO.getId());
        TenantSubscription entity = dtoToEntity(subscriptionDTO);
        subscriptionService.updateSubscription(entity);
        // 更新后重新查询返回DTO
        return subscriptionService.getSubscriptionDTO(entity.getId());
    }

    @Override
    public Boolean cancelSubscription(Long subscriptionId, String cancelReason, Long cancelBy) {
        log.info("RPC调用: cancelSubscription, subscriptionId={}, cancelBy={}", subscriptionId, cancelBy);
        return subscriptionService.cancelSubscription(subscriptionId, cancelReason, cancelBy);
    }

    @Override
    public Boolean suspendSubscription(Long subscriptionId) {
        log.info("RPC调用: suspendSubscription, subscriptionId={}", subscriptionId);
        return subscriptionService.suspendSubscription(subscriptionId);
    }

    @Override
    public Boolean activateSubscription(Long subscriptionId) {
        log.info("RPC调用: activateSubscription, subscriptionId={}", subscriptionId);
        return subscriptionService.activateSubscription(subscriptionId);
    }

    @Override
    public Boolean renewSubscription(Long subscriptionId, Integer months) {
        log.info("RPC调用: renewSubscription, subscriptionId={}, months={}", subscriptionId, months);
        return subscriptionService.renewSubscription(subscriptionId, months);
    }

    @Override
    public List<TenantSubscriptionLimitDTO> listSubscriptionLimits(Long subscriptionId) {
        log.info("RPC调用: listSubscriptionLimits, subscriptionId={}", subscriptionId);
        return limitService.listBySubscriptionId(subscriptionId);
    }

    @Override
    public TenantSubscriptionLimitDTO getSubscriptionLimit(Long subscriptionId, String limitKey) {
        log.info("RPC调用: getSubscriptionLimit, subscriptionId={}, limitKey={}", subscriptionId, limitKey);
        return limitService.getBySubscriptionAndKey(subscriptionId, limitKey);
    }

    @Override
    public Boolean increaseUsage(Long limitId, Long increment) {
        log.info("RPC调用: increaseUsage, limitId={}, increment={}", limitId, increment);
        return limitService.increaseUsage(limitId, increment);
    }

    @Override
    public Boolean checkLimitExceeded(Long subscriptionId, String limitKey, Long requiredValue) {
        log.info("RPC调用: checkLimitExceeded, subscriptionId={}, limitKey={}, requiredValue={}",
                subscriptionId, limitKey, requiredValue);
        return limitService.checkLimitExceeded(subscriptionId, limitKey, requiredValue);
    }

    /**
     * Entity转DTO
     */
    private TenantSubscriptionDTO entityToDto(TenantSubscription entity) {
        if (entity == null) {
            return null;
        }
        TenantSubscriptionDTO dto = new TenantSubscriptionDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * DTO转Entity
     */
    private TenantSubscription dtoToEntity(TenantSubscriptionDTO dto) {
        if (dto == null) {
            return null;
        }
        TenantSubscription entity = new TenantSubscription();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
