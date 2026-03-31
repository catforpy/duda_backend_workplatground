package com.duda.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantCommissionRuleDTO;
import com.duda.tenant.entity.TenantCommissionRule;
import com.duda.tenant.mapper.TenantCommissionRuleMapper;
import com.duda.tenant.service.TenantCommissionRuleService;
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
 * 分佣规则表服务实现
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Slf4j
@Service
public class TenantCommissionRuleServiceImpl
    extends ServiceImpl<TenantCommissionRuleMapper, TenantCommissionRule>
    implements TenantCommissionRuleService {

    @Autowired
    private TenantCommissionRuleMapper ruleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantCommissionRule createRule(TenantCommissionRuleDTO dto) {
        log.info("开始创建分佣规则: tenantId={}, ruleCode={}", dto.getTenantId(), dto.getRuleCode());

        validateCreateParams(dto);
        validateBusinessRules(dto);

        // 检查规则编码是否已存在
        LambdaQueryWrapper<TenantCommissionRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantCommissionRule::getRuleCode, dto.getRuleCode());
        if (count(wrapper) > 0) {
            log.warn("规则编码已存在: ruleCode={}", dto.getRuleCode());
            throw new BizException(409, "规则编码已存在");
        }

        TenantCommissionRule rule = new TenantCommissionRule();
        BeanUtils.copyProperties(dto, rule);

        // 设置默认值
        if (rule.getPriority() == null) {
            rule.setPriority(100);
        }
        if (rule.getStatus() == null) {
            rule.setStatus("active");
        }
        if (rule.getEffectiveStart() == null) {
            rule.setEffectiveStart(LocalDateTime.now());
        }
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());

        save(rule);

        log.info("创建分佣规则成功: ruleId={}, ruleCode={}", rule.getId(), rule.getRuleCode());

        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantCommissionRule updateRule(TenantCommissionRuleDTO dto) {
        log.info("开始更新分佣规则: ruleId={}", dto.getId());

        if (dto.getId() == null) {
            throw new BizException(400, "规则ID不能为空");
        }

        TenantCommissionRule rule = getById(dto.getId());
        if (rule == null) {
            throw new BizException(41001, "分佣规则不存在");
        }

        if (StrUtil.isNotBlank(dto.getRuleName())) {
            rule.setRuleName(dto.getRuleName());
        }
        if (dto.getCommissionValue() != null) {
            rule.setCommissionValue(dto.getCommissionValue());
        }
        if (dto.getPriority() != null) {
            rule.setPriority(dto.getPriority());
        }
        if (dto.getMaxCommissionAmount() != null) {
            rule.setMaxCommissionAmount(dto.getMaxCommissionAmount());
        }

        rule.setUpdatedAt(LocalDateTime.now());
        updateById(rule);

        log.info("更新分佣规则成功: ruleId={}", rule.getId());

        return rule;
    }

    @Override
    public TenantCommissionRuleDTO getRuleDTO(Long id) {
        if (id == null) {
            throw new BizException(400, "规则ID不能为空");
        }

        TenantCommissionRule rule = getById(id);
        if (rule == null) {
            throw new BizException(41001, "分佣规则不存在");
        }

        TenantCommissionRuleDTO dto = new TenantCommissionRuleDTO();
        BeanUtils.copyProperties(rule, dto);

        return dto;
    }

    @Override
    public TenantCommissionRuleDTO getByRuleCode(String ruleCode) {
        if (StrUtil.isBlank(ruleCode)) {
            throw new BizException(400, "规则编码不能为空");
        }

        LambdaQueryWrapper<TenantCommissionRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantCommissionRule::getRuleCode, ruleCode);
        TenantCommissionRule rule = getOne(wrapper);

        if (rule == null) {
            return null;
        }

        TenantCommissionRuleDTO dto = new TenantCommissionRuleDTO();
        BeanUtils.copyProperties(rule, dto);

        return dto;
    }

    @Override
    public List<TenantCommissionRuleDTO> listActiveRules(Long tenantId) {
        if (tenantId == null) {
            throw new BizException(400, "租户ID不能为空");
        }

        LambdaQueryWrapper<TenantCommissionRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantCommissionRule::getTenantId, tenantId);
        wrapper.eq(TenantCommissionRule::getStatus, "active");
        wrapper.orderByAsc(TenantCommissionRule::getPriority);

        List<TenantCommissionRule> list = list(wrapper);

        return list.stream()
            .map(rule -> {
                TenantCommissionRuleDTO dto = new TenantCommissionRuleDTO();
                BeanUtils.copyProperties(rule, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean suspend(Long id) {
        log.info("开始暂停分佣规则: ruleId={}", id);

        TenantCommissionRule rule = getById(id);
        if (rule == null) {
            throw new BizException(41001, "分佣规则不存在");
        }

        rule.setStatus("inactive");
        rule.setUpdatedAt(LocalDateTime.now());
        updateById(rule);

        log.info("暂停分佣规则成功: ruleId={}", id);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean activate(Long id) {
        log.info("开始激活分佣规则: ruleId={}", id);

        TenantCommissionRule rule = getById(id);
        if (rule == null) {
            throw new BizException(41001, "分佣规则不存在");
        }

        rule.setStatus("active");
        rule.setUpdatedAt(LocalDateTime.now());
        updateById(rule);

        log.info("激活分佣规则成功: ruleId={}", id);

        return true;
    }

    private void validateCreateParams(TenantCommissionRuleDTO dto) {
        if (dto == null) {
            throw new BizException(400, "分佣规则信息不能为空");
        }
        if (dto.getTenantId() == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        if (StrUtil.isBlank(dto.getRuleCode())) {
            throw new BizException(400, "规则编码不能为空");
        }
        if (StrUtil.isBlank(dto.getRuleName())) {
            throw new BizException(400, "规则名称不能为空");
        }
        if (StrUtil.isBlank(dto.getRuleType())) {
            throw new BizException(400, "规则类型不能为空");
        }
        if (StrUtil.isBlank(dto.getCommissionType())) {
            throw new BizException(400, "分佣类型不能为空");
        }
        if (dto.getCommissionValue() == null) {
            throw new BizException(400, "分佣值不能为空");
        }
    }

    private void validateBusinessRules(TenantCommissionRuleDTO dto) {
        if (!"percentage".equals(dto.getRuleType())
            && !"fixed".equals(dto.getRuleType())
            && !"tiered".equals(dto.getRuleType())) {
            throw new BizException(400, "规则类型必须是percentage、fixed或tiered");
        }

        if (dto.getPriority() != null && (dto.getPriority() < 0 || dto.getPriority() > 100)) {
            throw new BizException(400, "优先级必须在0-100之间");
        }

        if (dto.getCommissionValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(400, "分佣值必须大于0");
        }
    }
}
