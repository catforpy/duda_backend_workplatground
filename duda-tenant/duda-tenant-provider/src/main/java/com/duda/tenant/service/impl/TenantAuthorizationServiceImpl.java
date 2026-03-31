package com.duda.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantAuthorizationDTO;
import com.duda.tenant.entity.TenantAuthorization;
import com.duda.tenant.mapper.TenantAuthorizationMapper;
import com.duda.tenant.service.TenantAuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 授权管理表服务实现
 */
@Slf4j
@Service
public class TenantAuthorizationServiceImpl
    extends ServiceImpl<TenantAuthorizationMapper, TenantAuthorization>
    implements TenantAuthorizationService {

    @Autowired
    private TenantAuthorizationMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantAuthorization createAuthorization(TenantAuthorizationDTO dto) {
        log.info("创建授权: tenantId={}, merchantId={}", dto.getTenantId(), dto.getMerchantId());

        if (StrUtil.isBlank(dto.getAuthorizationCode())) {
            throw new BizException(400, "授权编码不能为空");
        }

        LambdaQueryWrapper<TenantAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantAuthorization::getAuthorizationCode, dto.getAuthorizationCode());
        if (count(wrapper) > 0) {
            throw new BizException(409, "授权编码已存在");
        }

        TenantAuthorization entity = new TenantAuthorization();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(LocalDateTime.now());
        if (entity.getStatus() == null) {
            entity.setStatus("active");
        }
        save(entity);
        log.info("创建授权成功: id={}", entity.getId());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantAuthorization updateAuthorization(TenantAuthorizationDTO dto) {
        log.info("更新授权: id={}", dto.getId());
        TenantAuthorization entity = getById(dto.getId());
        BeanUtils.copyProperties(dto, entity);
        updateById(entity);
        return entity;
    }

    @Override
    public TenantAuthorizationDTO getAuthorizationDTO(Long id) {
        if (id == null) {
            throw new BizException(400, "ID不能为空");
        }
        TenantAuthorization entity = getById(id);
        if (entity == null) {
            throw new BizException(44001, "授权不存在");
        }
        TenantAuthorizationDTO dto = new TenantAuthorizationDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public TenantAuthorizationDTO getByAuthorizationCode(String authorizationCode) {
        if (StrUtil.isBlank(authorizationCode)) {
            throw new BizException(400, "授权编码不能为空");
        }
        LambdaQueryWrapper<TenantAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantAuthorization::getAuthorizationCode, authorizationCode);
        TenantAuthorization entity = getOne(wrapper);
        if (entity == null) return null;
        TenantAuthorizationDTO dto = new TenantAuthorizationDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<TenantAuthorizationDTO> listByTenant(Long tenantId) {
        if (tenantId == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        return lambdaQuery()
            .eq(TenantAuthorization::getTenantId, tenantId)
            .list()
            .stream()
            .map(entity -> {
                TenantAuthorizationDTO dto = new TenantAuthorizationDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean suspend(Long id) {
        log.info("暂停授权: id={}", id);
        TenantAuthorization entity = getById(id);
        if (entity == null) {
            throw new BizException(44001, "授权不存在");
        }
        entity.setStatus("suspended");
        updateById(entity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean activate(Long id) {
        log.info("激活授权: id={}", id);
        TenantAuthorization entity = getById(id);
        if (entity == null) {
            throw new BizException(44001, "授权不存在");
        }
        entity.setStatus("active");
        updateById(entity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean terminate(Long id) {
        log.info("终止授权: id={}", id);
        TenantAuthorization entity = getById(id);
        if (entity == null) {
            throw new BizException(44001, "授权不存在");
        }
        entity.setStatus("terminated");
        updateById(entity);
        return true;
    }
}
