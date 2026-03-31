package com.duda.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantSubscriptionDTO;
import com.duda.tenant.entity.TenantSubscription;
import com.duda.tenant.mapper.TenantSubscriptionMapper;
import com.duda.tenant.service.TenantSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户订阅服务实现
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@Service
public class TenantSubscriptionServiceImpl
        extends ServiceImpl<TenantSubscriptionMapper, TenantSubscription>
        implements TenantSubscriptionService {

    @Autowired
    private TenantSubscriptionMapper subscriptionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSubscription createSubscription(TenantSubscription subscription) {
        log.info("开始创建订阅: subscriptionCode={}, tenantId={}, userId={}",
                subscription.getSubscriptionCode(), subscription.getTenantId(), subscription.getUserId());

        // 1. 参数校验
        validateCreateParams(subscription);

        // 2. 业务规则校验
        validateBusinessRules(subscription);

        // 3. 检查订阅编号是否已存在(幂等性)
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getSubscriptionCode, subscription.getSubscriptionCode());
        if (count(wrapper) > 0) {
            log.warn("订阅编号已存在: subscriptionCode={}", subscription.getSubscriptionCode());
            throw new BizException(409, "订阅编号已存在");
        }

        // 4. 设置默认值
        subscription.setSubscriptionStatus(subscription.getSubscriptionStatus() != null
                ? subscription.getSubscriptionStatus() : "ACTIVE");
        subscription.setAutoRenew(subscription.getAutoRenew() != null ? subscription.getAutoRenew() : 0);
        subscription.setRenewCount(subscription.getRenewCount() != null ? subscription.getRenewCount() : 0);
        subscription.setDeleted(0);
        subscription.setCreateTime(LocalDateTime.now());
        subscription.setUpdateTime(LocalDateTime.now());

        // 5. 保存到数据库
        save(subscription);

        log.info("创建订阅成功: id={}, subscriptionCode={}",
                subscription.getId(), subscription.getSubscriptionCode());

        return subscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSubscription updateSubscription(TenantSubscription subscription) {
        log.info("开始更新订阅: id={}", subscription.getId());

        // 1. 参数校验
        if (subscription.getId() == null) {
            throw new BizException(400, "订阅ID不能为空");
        }

        // 2. 查询现有订阅
        TenantSubscription existing = getById(subscription.getId());
        if (existing == null) {
            log.warn("订阅不存在: id={}", subscription.getId());
            throw new BizException(10001, "订阅不存在");
        }

        // 3. 更新时间
        subscription.setUpdateTime(LocalDateTime.now());

        // 4. 保存更新
        updateById(subscription);

        log.info("更新订阅成功: id={}", subscription.getId());

        return subscription;
    }

    @Override
    public TenantSubscriptionDTO getSubscriptionDTO(Long id) {
        log.debug("查询订阅: id={}", id);
        TenantSubscription subscription = getById(id);
        return entityToDto(subscription);
    }

    @Override
    public TenantSubscriptionDTO getBySubscriptionCode(String subscriptionCode) {
        log.debug("根据订阅编号查询: subscriptionCode={}", subscriptionCode);
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getSubscriptionCode, subscriptionCode);
        TenantSubscription subscription = getOne(wrapper);
        return entityToDto(subscription);
    }

    @Override
    public List<TenantSubscriptionDTO> listByTenantId(Long tenantId) {
        log.debug("查询租户的所有订阅: tenantId={}", tenantId);
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getTenantId, tenantId);
        wrapper.orderByDesc(TenantSubscription::getCreateTime);
        List<TenantSubscription> subscriptions = list(wrapper);
        return subscriptions.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantSubscriptionDTO> listByUserId(Long userId) {
        log.debug("查询用户的所有订阅: userId={}", userId);
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getUserId, userId);
        wrapper.orderByDesc(TenantSubscription::getCreateTime);
        List<TenantSubscription> subscriptions = list(wrapper);
        return subscriptions.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantSubscriptionDTO> listByTenantAndUser(Long tenantId, Long userId) {
        log.debug("查询租户用户的订阅: tenantId={}, userId={}", tenantId, userId);
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getTenantId, tenantId);
        wrapper.eq(TenantSubscription::getUserId, userId);
        wrapper.orderByDesc(TenantSubscription::getCreateTime);
        List<TenantSubscription> subscriptions = list(wrapper);
        return subscriptions.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TenantSubscriptionDTO getActiveSubscription(Long tenantId, Long userId) {
        log.debug("查询生效中的订阅: tenantId={}, userId={}", tenantId, userId);
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getTenantId, tenantId);
        wrapper.eq(TenantSubscription::getUserId, userId);
        wrapper.eq(TenantSubscription::getSubscriptionStatus, "ACTIVE");
        wrapper.ge(TenantSubscription::getEndTime, LocalDateTime.now());
        wrapper.orderByDesc(TenantSubscription::getCreateTime);
        wrapper.last("LIMIT 1");
        TenantSubscription subscription = getOne(wrapper);
        return entityToDto(subscription);
    }

    @Override
    public List<TenantSubscriptionDTO> listExpiringSoon() {
        log.debug("查询即将到期的订阅（7天内）");
        LocalDateTime sevenDaysLater = LocalDateTime.now().plusDays(7);
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getSubscriptionStatus, "ACTIVE");
        wrapper.between(TenantSubscription::getEndTime, LocalDateTime.now(), sevenDaysLater);
        List<TenantSubscription> subscriptions = list(wrapper);
        return subscriptions.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantSubscriptionDTO> listExpired() {
        log.debug("查询已过期的订阅");
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getSubscriptionStatus, "ACTIVE");
        wrapper.lt(TenantSubscription::getEndTime, LocalDateTime.now());
        List<TenantSubscription> subscriptions = list(wrapper);
        return subscriptions.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelSubscription(Long id, String cancelReason, Long cancelBy) {
        log.info("取消订阅: id={}, cancelReason={}, cancelBy={}", id, cancelReason, cancelBy);

        TenantSubscription subscription = getById(id);
        if (subscription == null) {
            log.warn("订阅不存在: id={}", id);
            throw new BizException(10001, "订阅不存在");
        }

        subscription.setSubscriptionStatus("CANCELLED");
        subscription.setCancelReason(cancelReason);
        subscription.setCancelTime(LocalDateTime.now());
        subscription.setCancelBy(cancelBy);
        subscription.setUpdateTime(LocalDateTime.now());

        updateById(subscription);

        log.info("取消订阅成功: id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean suspendSubscription(Long id) {
        log.info("暂停订阅: id={}", id);

        TenantSubscription subscription = getById(id);
        if (subscription == null) {
            log.warn("订阅不存在: id={}", id);
            throw new BizException(10001, "订阅不存在");
        }

        subscription.setSubscriptionStatus("SUSPENDED");
        subscription.setUpdateTime(LocalDateTime.now());
        updateById(subscription);

        log.info("暂停订阅成功: id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean activateSubscription(Long id) {
        log.info("激活订阅: id={}", id);

        TenantSubscription subscription = getById(id);
        if (subscription == null) {
            log.warn("订阅不存在: id={}", id);
            throw new BizException(10001, "订阅不存在");
        }

        subscription.setSubscriptionStatus("ACTIVE");
        subscription.setUpdateTime(LocalDateTime.now());
        updateById(subscription);

        log.info("激活订阅成功: id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean renewSubscription(Long id, Integer months) {
        log.info("续费订阅: id={}, months={}", id, months);

        TenantSubscription subscription = getById(id);
        if (subscription == null) {
            log.warn("订阅不存在: id={}", id);
            throw new BizException(10001, "订阅不存在");
        }

        // 计算新的到期时间
        LocalDateTime newEndTime;
        if (subscription.getEndTime() != null && subscription.getEndTime().isAfter(LocalDateTime.now())) {
            // 如果还有效，在原到期时间基础上延长
            newEndTime = subscription.getEndTime().plusMonths(months);
        } else {
            // 如果已过期，从现在开始计算
            newEndTime = LocalDateTime.now().plusMonths(months);
        }

        subscription.setEndTime(newEndTime);
        subscription.setRenewCount(subscription.getRenewCount() + 1);
        subscription.setLastRenewTime(LocalDateTime.now());
        subscription.setSubscriptionStatus("ACTIVE");
        subscription.setUpdateTime(LocalDateTime.now());

        updateById(subscription);

        log.info("续费订阅成功: id={}, newEndTime={}", id, newEndTime);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer checkAndUpdateExpired() {
        log.info("检查并更新过期订阅状态");

        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getSubscriptionStatus, "ACTIVE");
        wrapper.lt(TenantSubscription::getEndTime, LocalDateTime.now());

        List<TenantSubscription> expiredList = list(wrapper);
        int count = 0;

        for (TenantSubscription subscription : expiredList) {
            subscription.setSubscriptionStatus("EXPIRED");
            subscription.setUpdateTime(LocalDateTime.now());
            updateById(subscription);
            count++;
        }

        log.info("更新过期订阅状态成功: count={}", count);
        return count;
    }

    /**
     * 参数校验
     */
    private void validateCreateParams(TenantSubscription subscription) {
        if (StrUtil.isBlank(subscription.getSubscriptionCode())) {
            throw new BizException(400, "订阅编号不能为空");
        }
        if (subscription.getTenantId() == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        if (subscription.getUserId() == null) {
            throw new BizException(400, "用户ID不能为空");
        }
        if (subscription.getPackageId() == null) {
            throw new BizException(400, "套餐ID不能为空");
        }
        if (subscription.getStartTime() == null) {
            throw new BizException(400, "订阅开始时间不能为空");
        }
    }

    /**
     * 业务规则校验
     */
    private void validateBusinessRules(TenantSubscription subscription) {
        // 检查订阅状态是否合法
        if (subscription.getSubscriptionStatus() != null) {
            if (!StrUtil.equalsAny(subscription.getSubscriptionStatus(),
                    "ACTIVE", "SUSPENDED", "CANCELLED", "EXPIRED", "PENDING")) {
                throw new BizException(400, "订阅状态不合法");
            }
        }

        // 检查结束时间是否大于开始时间
        if (subscription.getEndTime() != null && subscription.getStartTime() != null) {
            if (subscription.getEndTime().isBefore(subscription.getStartTime())) {
                throw new BizException(400, "订阅结束时间不能早于开始时间");
            }
        }
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
}
