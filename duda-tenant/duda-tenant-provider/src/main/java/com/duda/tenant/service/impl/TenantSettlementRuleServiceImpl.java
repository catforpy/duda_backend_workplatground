package com.duda.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantSettlementRuleDTO;
import com.duda.tenant.entity.TenantSettlementRule;
import com.duda.tenant.mapper.TenantSettlementRuleMapper;
import com.duda.tenant.service.TenantSettlementRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算规则表服务实现
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Slf4j
@Service
public class TenantSettlementRuleServiceImpl
    extends ServiceImpl<TenantSettlementRuleMapper, TenantSettlementRule>
    implements TenantSettlementRuleService {

    @Autowired
    private TenantSettlementRuleMapper ruleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSettlementRule createRule(TenantSettlementRuleDTO dto) {
        log.info("开始创建结算规则: tenantId={}, merchantId={}", dto.getTenantId(), dto.getMerchantId());

        // 1. 参数校验
        validateCreateParams(dto);

        // 2. 业务规则校验
        validateBusinessRules(dto);

        // 3. 检查是否已存在规则(幂等性)
        LambdaQueryWrapper<TenantSettlementRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSettlementRule::getTenantId, dto.getTenantId());
        wrapper.eq(TenantSettlementRule::getMerchantId, dto.getMerchantId());
        wrapper.eq(TenantSettlementRule::getStatus, "active");
        if (count(wrapper) > 0) {
            log.warn("结算规则已存在: tenantId={}, merchantId={}", dto.getTenantId(), dto.getMerchantId());
            throw new BizException(409, "结算规则已存在");
        }

        // 4. 创建实体
        TenantSettlementRule rule = new TenantSettlementRule();
        BeanUtils.copyProperties(dto, rule);

        // 设置默认值
        if (rule.getSettlementTime() == null) {
            rule.setSettlementTime("02:00:00");
        }
        if (rule.getPlatformFeeRate() == null) {
            rule.setPlatformFeeRate(new BigDecimal("0.0500"));
        }
        if (rule.getTenantFeeRate() == null) {
            rule.setTenantFeeRate(new BigDecimal("0.1000"));
        }
        if (rule.getCommissionFeeRate() == null) {
            rule.setCommissionFeeRate(new BigDecimal("0.1000"));
        }
        if (rule.getReserveFeeRate() == null) {
            rule.setReserveFeeRate(BigDecimal.ZERO);
        }
        if (rule.getOtherFeeRate() == null) {
            rule.setOtherFeeRate(BigDecimal.ZERO);
        }
        if (rule.getMinSettlementAmount() == null) {
            rule.setMinSettlementAmount(new BigDecimal("100.00"));
        }
        if (rule.getAutoSettle() == null) {
            rule.setAutoSettle(false);
        }
        if (rule.getAutoSettleThreshold() == null) {
            rule.setAutoSettleThreshold(new BigDecimal("1000.00"));
        }
        if (rule.getEffectiveDate() == null) {
            rule.setEffectiveDate(LocalDateTime.now());
        }
        if (rule.getStatus() == null) {
            rule.setStatus("active");
        }
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());

        // 5. 保存到数据库
        save(rule);

        log.info("创建结算规则成功: ruleId={}, tenantId={}, merchantId={}",
            rule.getId(), rule.getTenantId(), rule.getMerchantId());

        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSettlementRule updateRule(TenantSettlementRuleDTO dto) {
        log.info("开始更新结算规则: ruleId={}", dto.getId());

        // 1. 参数校验
        if (dto.getId() == null) {
            throw new BizException(400, "规则ID不能为空");
        }

        // 2. 查询规则
        TenantSettlementRule rule = getById(dto.getId());
        if (rule == null) {
            log.warn("结算规则不存在: ruleId={}", dto.getId());
            throw new BizException(40001, "结算规则不存在");
        }

        // 3. 业务规则校验
        if (dto.getPlatformFeeRate() != null && dto.getPlatformFeeRate().compareTo(new BigDecimal("0.5")) > 0) {
            throw new BizException(400, "平台抽成比例不能超过50%");
        }
        if (dto.getTenantFeeRate() != null && dto.getTenantFeeRate().compareTo(new BigDecimal("0.5")) > 0) {
            throw new BizException(400, "小程序抽成比例不能超过50%");
        }

        // 4. 更新字段
        if (dto.getSettlementCycle() != null) {
            rule.setSettlementCycle(dto.getSettlementCycle());
        }
        if (dto.getSettlementDay() != null) {
            rule.setSettlementDay(dto.getSettlementDay());
        }
        if (dto.getSettlementTime() != null) {
            rule.setSettlementTime(dto.getSettlementTime());
        }
        if (dto.getPlatformFeeRate() != null) {
            rule.setPlatformFeeRate(dto.getPlatformFeeRate());
        }
        if (dto.getTenantFeeRate() != null) {
            rule.setTenantFeeRate(dto.getTenantFeeRate());
        }
        if (dto.getCommissionFeeRate() != null) {
            rule.setCommissionFeeRate(dto.getCommissionFeeRate());
        }
        if (dto.getMinSettlementAmount() != null) {
            rule.setMinSettlementAmount(dto.getMinSettlementAmount());
        }
        if (dto.getAutoSettle() != null) {
            rule.setAutoSettle(dto.getAutoSettle());
        }
        if (dto.getAutoSettleThreshold() != null) {
            rule.setAutoSettleThreshold(dto.getAutoSettleThreshold());
        }

        rule.setUpdatedAt(LocalDateTime.now());
        updateById(rule);

        log.info("更新结算规则成功: ruleId={}", rule.getId());

        return rule;
    }

    @Override
    public TenantSettlementRuleDTO getRuleDTO(Long id) {
        log.debug("查询结算规则: ruleId={}", id);

        if (id == null) {
            throw new BizException(400, "规则ID不能为空");
        }

        TenantSettlementRule rule = getById(id);
        if (rule == null) {
            log.warn("结算规则不存在: ruleId={}", id);
            throw new BizException(40001, "结算规则不存在");
        }

        TenantSettlementRuleDTO dto = new TenantSettlementRuleDTO();
        BeanUtils.copyProperties(rule, dto);

        return dto;
    }

    @Override
    public TenantSettlementRuleDTO getTenantRule(Long tenantId, Long merchantId) {
        log.debug("查询租户结算规则: tenantId={}, merchantId={}", tenantId, merchantId);

        if (tenantId == null) {
            throw new BizException(400, "租户ID不能为空");
        }

        LambdaQueryWrapper<TenantSettlementRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSettlementRule::getTenantId, tenantId);
        if (merchantId != null) {
            wrapper.eq(TenantSettlementRule::getMerchantId, merchantId);
        }
        wrapper.eq(TenantSettlementRule::getStatus, "active");
        wrapper.orderByDesc(TenantSettlementRule::getEffectiveDate);
        wrapper.last("LIMIT 1");

        TenantSettlementRule rule = getOne(wrapper);
        if (rule == null) {
            log.warn("未找到激活的结算规则: tenantId={}, merchantId={}", tenantId, merchantId);
            return null;
        }

        TenantSettlementRuleDTO dto = new TenantSettlementRuleDTO();
        BeanUtils.copyProperties(rule, dto);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean suspend(Long id) {
        log.info("开始暂停结算规则: ruleId={}", id);

        if (id == null) {
            throw new BizException(400, "规则ID不能为空");
        }

        TenantSettlementRule rule = getById(id);
        if (rule == null) {
            throw new BizException(40001, "结算规则不存在");
        }

        if ("suspended".equals(rule.getStatus())) {
            log.warn("结算规则已暂停: ruleId={}", id);
            return true;
        }

        rule.setStatus("suspended");
        rule.setUpdatedAt(LocalDateTime.now());
        updateById(rule);

        log.info("暂停结算规则成功: ruleId={}", id);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean activate(Long id) {
        log.info("开始激活结算规则: ruleId={}", id);

        if (id == null) {
            throw new BizException(400, "规则ID不能为空");
        }

        TenantSettlementRule rule = getById(id);
        if (rule == null) {
            throw new BizException(40001, "结算规则不存在");
        }

        if ("active".equals(rule.getStatus())) {
            log.warn("结算规则已激活: ruleId={}", id);
            return true;
        }

        rule.setStatus("active");
        rule.setUpdatedAt(LocalDateTime.now());
        updateById(rule);

        log.info("激活结算规则成功: ruleId={}", id);

        return true;
    }

    /**
     * 校验创建参数
     */
    private void validateCreateParams(TenantSettlementRuleDTO dto) {
        if (dto == null) {
            throw new BizException(400, "结算规则信息不能为空");
        }
        if (dto.getTenantId() == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        if (dto.getMerchantId() == null) {
            throw new BizException(400, "商户ID不能为空");
        }
        if (StrUtil.isBlank(dto.getSettlementCycle())) {
            throw new BizException(400, "结算周期不能为空");
        }
    }

    /**
     * 校验业务规则
     */
    private void validateBusinessRules(TenantSettlementRuleDTO dto) {
        // 结算周期校验
        if (!"daily".equals(dto.getSettlementCycle())
            && !"weekly".equals(dto.getSettlementCycle())
            && !"monthly".equals(dto.getSettlementCycle())) {
            throw new BizException(400, "结算周期必须是daily、weekly或monthly");
        }

        // 结算日校验
        if ("monthly".equals(dto.getSettlementCycle())) {
            if (dto.getSettlementDay() == null || dto.getSettlementDay() < 1 || dto.getSettlementDay() > 31) {
                throw new BizException(400, "月结的结算日必须在1-31之间");
            }
        } else if ("weekly".equals(dto.getSettlementCycle())) {
            if (dto.getSettlementDay() == null || dto.getSettlementDay() < 1 || dto.getSettlementDay() > 7) {
                throw new BizException(400, "周结的结算日必须在1-7之间(1代表周一)");
            }
        }

        // 费率校验
        if (dto.getPlatformFeeRate() != null) {
            if (dto.getPlatformFeeRate().compareTo(BigDecimal.ZERO) < 0
                || dto.getPlatformFeeRate().compareTo(new BigDecimal("0.5")) > 0) {
                throw new BizException(400, "平台抽成比例必须在0-50%之间");
            }
        }
        if (dto.getTenantFeeRate() != null) {
            if (dto.getTenantFeeRate().compareTo(BigDecimal.ZERO) < 0
                || dto.getTenantFeeRate().compareTo(new BigDecimal("0.5")) > 0) {
                throw new BizException(400, "小程序抽成比例必须在0-50%之间");
            }
        }
        if (dto.getCommissionFeeRate() != null) {
            if (dto.getCommissionFeeRate().compareTo(BigDecimal.ZERO) < 0
                || dto.getCommissionFeeRate().compareTo(new BigDecimal("0.5")) > 0) {
                throw new BizException(400, "销售商提成比例必须在0-50%之间");
            }
        }

        // 最低结算金额校验
        if (dto.getMinSettlementAmount() != null && dto.getMinSettlementAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(400, "最低结算金额必须大于0");
        }

        // 自动结算阈值校验
        if (dto.getAutoSettleThreshold() != null && dto.getAutoSettleThreshold().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(400, "自动结算阈值必须大于0");
        }
    }
}
