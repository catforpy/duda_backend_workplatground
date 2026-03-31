package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.tenant.api.dto.TenantSplitRuleDTO;
import com.duda.tenant.entity.TenantSplitRule;
import com.duda.tenant.mapper.TenantSplitRuleMapper;
import com.duda.tenant.service.TenantSplitRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分账规则表服务实现
 */
@Slf4j
@Service
public class TenantSplitRuleServiceImpl
    extends ServiceImpl<TenantSplitRuleMapper, TenantSplitRule>
    implements TenantSplitRuleService {

    @Autowired
    private TenantSplitRuleMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSplitRule createRule(TenantSplitRuleDTO dto) {
        log.info("创建分账规则: cooperationId={}", dto.getCooperationId());
        TenantSplitRule entity = new TenantSplitRule();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(LocalDateTime.now());
        if (entity.getStatus() == null) {
            entity.setStatus("active");
        }
        save(entity);
        log.info("创建分账规则成功: id={}", entity.getId());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSplitRule updateRule(TenantSplitRuleDTO dto) {
        log.info("更新分账规则: id={}", dto.getId());
        TenantSplitRule entity = getById(dto.getId());
        BeanUtils.copyProperties(dto, entity);
        updateById(entity);
        return entity;
    }

    @Override
    public TenantSplitRuleDTO getRuleDTO(Long id) {
        TenantSplitRule entity = getById(id);
        if (entity == null) return null;
        TenantSplitRuleDTO dto = new TenantSplitRuleDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<TenantSplitRuleDTO> listByCooperation(Long cooperationId) {
        return lambdaQuery()
            .eq(TenantSplitRule::getCooperationId, cooperationId)
            .list()
            .stream()
            .map(entity -> {
                TenantSplitRuleDTO dto = new TenantSplitRuleDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }
}
