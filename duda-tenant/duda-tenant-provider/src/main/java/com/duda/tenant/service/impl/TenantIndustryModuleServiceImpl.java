package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantIndustryModuleDTO;
import com.duda.tenant.entity.TenantIndustryModule;
import com.duda.tenant.mapper.TenantIndustryModuleMapper;
import com.duda.tenant.service.TenantIndustryModuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 行业模块表服务实现
 */
@Slf4j
@Service
public class TenantIndustryModuleServiceImpl
    extends ServiceImpl<TenantIndustryModuleMapper, TenantIndustryModule>
    implements TenantIndustryModuleService {

    @Autowired
    private TenantIndustryModuleMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantIndustryModule createModule(TenantIndustryModuleDTO dto) {
        log.info("创建行业模块: tenantId={}, moduleName={}", dto.getTenantId(), dto.getModuleName());
        if (dto.getTenantId() == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        TenantIndustryModule entity = new TenantIndustryModule();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(LocalDateTime.now());
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(true);
        }
        save(entity);
        log.info("创建行业模块成功: id={}", entity.getId());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantIndustryModule updateModule(TenantIndustryModuleDTO dto) {
        log.info("更新行业模块: id={}", dto.getId());
        TenantIndustryModule entity = getById(dto.getId());
        BeanUtils.copyProperties(dto, entity);
        updateById(entity);
        return entity;
    }

    @Override
    public TenantIndustryModuleDTO getModuleDTO(Long id) {
        if (id == null) {
            throw new BizException(400, "ID不能为空");
        }
        TenantIndustryModule entity = getById(id);
        if (entity == null) {
            throw new BizException(48001, "行业模块不存在");
        }
        TenantIndustryModuleDTO dto = new TenantIndustryModuleDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<TenantIndustryModuleDTO> listByTenant(Long tenantId) {
        if (tenantId == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        return lambdaQuery()
            .eq(TenantIndustryModule::getTenantId, tenantId)
            .list()
            .stream()
            .map(entity -> {
                TenantIndustryModuleDTO dto = new TenantIndustryModuleDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean enable(Long id) {
        log.info("启用行业模块: id={}", id);
        TenantIndustryModule entity = getById(id);
        if (entity == null) {
            throw new BizException(48001, "行业模块不存在");
        }
        entity.setIsEnabled(true);
        updateById(entity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disable(Long id) {
        log.info("禁用行业模块: id={}", id);
        TenantIndustryModule entity = getById(id);
        if (entity == null) {
            throw new BizException(48001, "行业模块不存在");
        }
        entity.setIsEnabled(false);
        updateById(entity);
        return true;
    }
}
