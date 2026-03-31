package com.duda.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantSalesAgentDTO;
import com.duda.tenant.entity.TenantSalesAgent;
import com.duda.tenant.mapper.TenantSalesAgentMapper;
import com.duda.tenant.service.TenantSalesAgentService;
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
 * 销售商表服务实现
 */
@Slf4j
@Service
public class TenantSalesAgentServiceImpl
    extends ServiceImpl<TenantSalesAgentMapper, TenantSalesAgent>
    implements TenantSalesAgentService {

    @Autowired
    private TenantSalesAgentMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSalesAgent createAgent(TenantSalesAgentDTO dto) {
        log.info("创建销售商: agentName={}", dto.getAgentName());

        if (StrUtil.isBlank(dto.getAgentCode())) {
            throw new BizException(400, "销售商编码不能为空");
        }

        LambdaQueryWrapper<TenantSalesAgent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSalesAgent::getAgentCode, dto.getAgentCode());
        if (count(wrapper) > 0) {
            throw new BizException(409, "销售商编码已存在");
        }

        TenantSalesAgent entity = new TenantSalesAgent();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(LocalDateTime.now());
        if (entity.getCommissionRate() == null) {
            entity.setCommissionRate(new BigDecimal("0.1000"));
        }
        if (entity.getStatus() == null) {
            entity.setStatus("active");
        }
        save(entity);
        log.info("创建销售商成功: id={}", entity.getId());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSalesAgent updateAgent(TenantSalesAgentDTO dto) {
        log.info("更新销售商: id={}", dto.getId());
        TenantSalesAgent entity = getById(dto.getId());
        BeanUtils.copyProperties(dto, entity);
        updateById(entity);
        return entity;
    }

    @Override
    public TenantSalesAgentDTO getAgentDTO(Long id) {
        if (id == null) {
            throw new BizException(400, "ID不能为空");
        }
        TenantSalesAgent entity = getById(id);
        if (entity == null) {
            throw new BizException(43001, "销售商不存在");
        }
        TenantSalesAgentDTO dto = new TenantSalesAgentDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public TenantSalesAgentDTO getByReferralCode(String referralCode) {
        if (StrUtil.isBlank(referralCode)) {
            throw new BizException(400, "推荐码不能为空");
        }
        LambdaQueryWrapper<TenantSalesAgent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSalesAgent::getReferralCode, referralCode);
        TenantSalesAgent entity = getOne(wrapper);
        if (entity == null) return null;
        TenantSalesAgentDTO dto = new TenantSalesAgentDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<TenantSalesAgentDTO> listAllAgents() {
        return list().stream()
            .map(entity -> {
                TenantSalesAgentDTO dto = new TenantSalesAgentDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }
}
