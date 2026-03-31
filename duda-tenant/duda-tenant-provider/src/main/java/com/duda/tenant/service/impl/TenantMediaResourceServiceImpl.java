package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantMediaResourceDTO;
import com.duda.tenant.entity.TenantMediaResource;
import com.duda.tenant.mapper.TenantMediaResourceMapper;
import com.duda.tenant.service.TenantMediaResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 媒体资源表服务实现
 */
@Slf4j
@Service
public class TenantMediaResourceServiceImpl
    extends ServiceImpl<TenantMediaResourceMapper, TenantMediaResource>
    implements TenantMediaResourceService {

    @Autowired
    private TenantMediaResourceMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantMediaResource createResource(TenantMediaResourceDTO dto) {
        log.info("创建媒体资源: tenantId={}, fileName={}", dto.getTenantId(), dto.getFileName());
        if (dto.getTenantId() == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        TenantMediaResource entity = new TenantMediaResource();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(LocalDateTime.now());
        if (entity.getStatus() == null) {
            entity.setStatus("active");
        }
        save(entity);
        log.info("创建媒体资源成功: id={}", entity.getId());
        return entity;
    }

    @Override
    public TenantMediaResourceDTO getResourceDTO(Long id) {
        if (id == null) {
            throw new BizException(400, "ID不能为空");
        }
        TenantMediaResource entity = getById(id);
        if (entity == null) {
            throw new BizException(47001, "媒体资源不存在");
        }
        TenantMediaResourceDTO dto = new TenantMediaResourceDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<TenantMediaResourceDTO> listByTenant(Long tenantId) {
        if (tenantId == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        return lambdaQuery()
            .eq(TenantMediaResource::getTenantId, tenantId)
            .list()
            .stream()
            .map(entity -> {
                TenantMediaResourceDTO dto = new TenantMediaResourceDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<TenantMediaResourceDTO> listByType(Long tenantId, String resourceType) {
        if (tenantId == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        return lambdaQuery()
            .eq(TenantMediaResource::getTenantId, tenantId)
            .eq(TenantMediaResource::getResourceType, resourceType)
            .list()
            .stream()
            .map(entity -> {
                TenantMediaResourceDTO dto = new TenantMediaResourceDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }
}
