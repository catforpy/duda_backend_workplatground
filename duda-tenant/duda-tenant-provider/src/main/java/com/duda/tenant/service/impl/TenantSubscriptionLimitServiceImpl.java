package com.duda.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantSubscriptionLimitDTO;
import com.duda.tenant.entity.TenantSubscriptionLimit;
import com.duda.tenant.mapper.TenantSubscriptionLimitMapper;
import com.duda.tenant.service.TenantSubscriptionLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户订阅限制条件服务实现
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@Service
public class TenantSubscriptionLimitServiceImpl
        extends ServiceImpl<TenantSubscriptionLimitMapper, TenantSubscriptionLimit>
        implements TenantSubscriptionLimitService {

    @Autowired
    private TenantSubscriptionLimitMapper limitMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSubscriptionLimit createLimit(TenantSubscriptionLimit limit) {
        log.info("开始创建限制条件: subscriptionId={}, limitKey={}",
                limit.getSubscriptionId(), limit.getLimitKey());

        // 1. 参数校验
        if (limit.getSubscriptionId() == null) {
            throw new BizException(400, "订阅ID不能为空");
        }
        if (StrUtil.isBlank(limit.getLimitKey())) {
            throw new BizException(400, "限制类型不能为空");
        }
        if (StrUtil.isBlank(limit.getLimitValue())) {
            throw new BizException(400, "限制值不能为空");
        }

        // 2. 检查是否已存在(幂等性)
        LambdaQueryWrapper<TenantSubscriptionLimit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscriptionLimit::getSubscriptionId, limit.getSubscriptionId());
        wrapper.eq(TenantSubscriptionLimit::getLimitKey, limit.getLimitKey());
        if (count(wrapper) > 0) {
            log.warn("限制条件已存在: subscriptionId={}, limitKey={}",
                    limit.getSubscriptionId(), limit.getLimitKey());
            throw new BizException(409, "限制条件已存在");
        }

        // 3. 设置默认值
        limit.setCurrentUsed(limit.getCurrentUsed() != null ? limit.getCurrentUsed() : 0L);
        limit.setCurrentUsedPercent(limit.getCurrentUsedPercent() != null
                ? limit.getCurrentUsedPercent() : BigDecimal.ZERO);
        limit.setAlertSent(limit.getAlertSent() != null ? limit.getAlertSent() : 0);
        limit.setCreateTime(LocalDateTime.now());
        limit.setUpdateTime(LocalDateTime.now());

        // 4. 保存到数据库
        save(limit);

        log.info("创建限制条件成功: id={}", limit.getId());

        return limit;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSubscriptionLimit updateLimit(TenantSubscriptionLimit limit) {
        log.info("开始更新限制条件: id={}", limit.getId());

        // 1. 参数校验
        if (limit.getId() == null) {
            throw new BizException(400, "限制条件ID不能为空");
        }

        // 2. 更新时间
        limit.setUpdateTime(LocalDateTime.now());

        // 3. 保存更新
        updateById(limit);

        log.info("更新限制条件成功: id={}", limit.getId());

        return limit;
    }

    @Override
    public TenantSubscriptionLimitDTO getLimitDTO(Long id) {
        log.debug("查询限制条件: id={}", id);
        TenantSubscriptionLimit limit = getById(id);
        return entityToDto(limit);
    }

    @Override
    public List<TenantSubscriptionLimitDTO> listBySubscriptionId(Long subscriptionId) {
        log.debug("查询订阅的所有限制条件: subscriptionId={}", subscriptionId);
        LambdaQueryWrapper<TenantSubscriptionLimit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscriptionLimit::getSubscriptionId, subscriptionId);
        wrapper.orderByAsc(TenantSubscriptionLimit::getId);
        List<TenantSubscriptionLimit> limits = list(wrapper);
        return limits.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TenantSubscriptionLimitDTO getBySubscriptionAndKey(Long subscriptionId, String limitKey) {
        log.debug("根据订阅ID和限制类型查询: subscriptionId={}, limitKey={}", subscriptionId, limitKey);
        LambdaQueryWrapper<TenantSubscriptionLimit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscriptionLimit::getSubscriptionId, subscriptionId);
        wrapper.eq(TenantSubscriptionLimit::getLimitKey, limitKey);
        TenantSubscriptionLimit limit = getOne(wrapper);
        return entityToDto(limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean increaseUsage(Long id, Long increment) {
        log.info("增加使用量: id={}, increment={}", id, increment);

        TenantSubscriptionLimit limit = getById(id);
        if (limit == null) {
            log.warn("限制条件不存在: id={}", id);
            throw new BizException(10001, "限制条件不存在");
        }

        // 增加使用量
        long newUsed = limit.getCurrentUsed() + increment;
        limit.setCurrentUsed(newUsed);

        // 计算使用百分比
        if (StrUtil.isNotBlank(limit.getLimitValue()) && StrUtil.isNotEmpty(limit.getLimitUnit())) {
            if (!"boolean".equals(limit.getLimitUnit()) && !"json".equals(limit.getLimitUnit())) {
                try {
                    long maxValue = Long.parseLong(limit.getLimitValue());
                    if (maxValue > 0) {
                        BigDecimal percent = new BigDecimal(newUsed)
                                .multiply(new BigDecimal("100"))
                                .divide(new BigDecimal(maxValue), 2, BigDecimal.ROUND_HALF_UP);
                        limit.setCurrentUsedPercent(percent);
                    }
                } catch (NumberFormatException e) {
                    log.warn("限制值不是数字: limitValue={}", limit.getLimitValue());
                }
            }
        }

        limit.setUpdateTime(LocalDateTime.now());
        updateById(limit);

        log.info("增加使用量成功: id={}, currentUsed={}", id, newUsed);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetUsage(Long id) {
        log.info("重置使用量: id={}", id);

        TenantSubscriptionLimit limit = getById(id);
        if (limit == null) {
            log.warn("限制条件不存在: id={}", id);
            throw new BizException(10001, "限制条件不存在");
        }

        limit.setCurrentUsed(0L);
        limit.setCurrentUsedPercent(BigDecimal.ZERO);
        limit.setLastResetTime(LocalDateTime.now());
        limit.setAlertSent(0);
        limit.setUpdateTime(LocalDateTime.now());

        updateById(limit);

        log.info("重置使用量成功: id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer resetPeriodicLimits() {
        log.info("批量重置周期性限制的使用量");

        LocalDateTime now = LocalDateTime.now();

        // 重置每日限制
        LambdaQueryWrapper<TenantSubscriptionLimit> dailyWrapper = new LambdaQueryWrapper<>();
        dailyWrapper.eq(TenantSubscriptionLimit::getPeriodType, "daily");
        dailyWrapper.apply("DATE(last_reset_time) < CURDATE()");
        List<TenantSubscriptionLimit> dailyLimits = list(dailyWrapper);
        for (TenantSubscriptionLimit limit : dailyLimits) {
            resetUsage(limit.getId());
        }

        // 重置每月限制
        LambdaQueryWrapper<TenantSubscriptionLimit> monthlyWrapper = new LambdaQueryWrapper<>();
        monthlyWrapper.eq(TenantSubscriptionLimit::getPeriodType, "monthly");
        monthlyWrapper.apply("DATE_FORMAT(last_reset_time, '%Y-%m') < DATE_FORMAT(NOW(), '%Y-%m')");
        List<TenantSubscriptionLimit> monthlyLimits = list(monthlyWrapper);
        for (TenantSubscriptionLimit limit : monthlyLimits) {
            resetUsage(limit.getId());
        }

        // 重置每年限制
        LambdaQueryWrapper<TenantSubscriptionLimit> yearlyWrapper = new LambdaQueryWrapper<>();
        yearlyWrapper.eq(TenantSubscriptionLimit::getPeriodType, "yearly");
        yearlyWrapper.apply("YEAR(last_reset_time) < YEAR(NOW())");
        List<TenantSubscriptionLimit> yearlyLimits = list(yearlyWrapper);
        for (TenantSubscriptionLimit limit : yearlyLimits) {
            resetUsage(limit.getId());
        }

        int totalCount = dailyLimits.size() + monthlyLimits.size() + yearlyLimits.size();

        log.info("批量重置周期性限制成功: count={}", totalCount);
        return totalCount;
    }

    @Override
    public Boolean checkLimitExceeded(Long subscriptionId, String limitKey, Long requiredValue) {
        log.debug("检查限制是否超出: subscriptionId={}, limitKey={}, requiredValue={}",
                subscriptionId, limitKey, requiredValue);

        TenantSubscriptionLimitDTO limit = getBySubscriptionAndKey(subscriptionId, limitKey);
        if (limit == null) {
            log.warn("限制条件不存在: subscriptionId={}, limitKey={}", subscriptionId, limitKey);
            return false;
        }

        try {
            long maxValue = Long.parseLong(limit.getLimitValue());
            long currentUsed = limit.getCurrentUsed() != null ? limit.getCurrentUsed() : 0;
            return (currentUsed + requiredValue) > maxValue;
        } catch (NumberFormatException e) {
            log.warn("限制值不是数字，无法比较: limitValue={}", limit.getLimitValue());
            return false;
        }
    }

    @Override
    public List<TenantSubscriptionLimitDTO> listAlertLimits() {
        log.debug("查询需要告警的限制条件");

        LambdaQueryWrapper<TenantSubscriptionLimit> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(TenantSubscriptionLimit::getAlertThreshold, 0);
        wrapper.lt(TenantSubscriptionLimit::getAlertSent, 1);

        List<TenantSubscriptionLimit> limits = list(wrapper);
        List<TenantSubscriptionLimitDTO> result = limits.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());

        // 筛选出使用量达到告警阈值的
        return result.stream()
                .filter(limit -> {
                    if (limit.getAlertThreshold() == null) {
                        return false;
                    }
                    BigDecimal threshold = new BigDecimal(limit.getAlertThreshold());
                    return limit.getCurrentUsedPercent().compareTo(threshold) >= 0;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchCreateLimits(Long subscriptionId, List<TenantSubscriptionLimit> limitList) {
        log.info("批量创建限制条件: subscriptionId={}, count={}", subscriptionId, limitList.size());

        int count = 0;
        for (TenantSubscriptionLimit limit : limitList) {
            limit.setSubscriptionId(subscriptionId);
            try {
                createLimit(limit);
                count++;
            } catch (Exception e) {
                log.error("创建限制条件失败: limitKey={}", limit.getLimitKey(), e);
            }
        }

        log.info("批量创建限制条件成功: count={}", count);
        return count;
    }

    /**
     * Entity转DTO
     */
    private TenantSubscriptionLimitDTO entityToDto(TenantSubscriptionLimit entity) {
        if (entity == null) {
            return null;
        }
        TenantSubscriptionLimitDTO dto = new TenantSubscriptionLimitDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
