package com.duda.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantCooperationDTO;
import com.duda.tenant.entity.TenantCooperation;
import com.duda.tenant.mapper.TenantCooperationMapper;
import com.duda.tenant.service.TenantCooperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 合作管理表服务实现
 */
@Slf4j
@Service
public class TenantCooperationServiceImpl
    extends ServiceImpl<TenantCooperationMapper, TenantCooperation>
    implements TenantCooperationService {

    @Autowired
    private TenantCooperationMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantCooperation createCooperation(TenantCooperationDTO dto) {
        log.info("创建合作: tenantId={}, operatorTenantId={}", dto.getTenantId(), dto.getOperatorTenantId());

        if (StrUtil.isBlank(dto.getCooperationCode())) {
            throw new BizException(400, "合作编码不能为空");
        }

        LambdaQueryWrapper<TenantCooperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantCooperation::getCooperationCode, dto.getCooperationCode());
        if (count(wrapper) > 0) {
            throw new BizException(409, "合作编码已存在");
        }

        TenantCooperation entity = new TenantCooperation();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(LocalDateTime.now());
        if (entity.getStatus() == null) {
            entity.setStatus("pending");
        }
        save(entity);
        log.info("创建合作成功: id={}", entity.getId());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantCooperation updateCooperation(TenantCooperationDTO dto) {
        log.info("更新合作: id={}", dto.getId());
        TenantCooperation entity = getById(dto.getId());
        BeanUtils.copyProperties(dto, entity);
        updateById(entity);
        return entity;
    }

    @Override
    public TenantCooperationDTO getCooperationDTO(Long id) {
        if (id == null) {
            throw new BizException(400, "ID不能为空");
        }
        TenantCooperation entity = getById(id);
        if (entity == null) {
            throw new BizException(45001, "合作不存在");
        }
        TenantCooperationDTO dto = new TenantCooperationDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public TenantCooperationDTO getByCooperationCode(String cooperationCode) {
        if (StrUtil.isBlank(cooperationCode)) {
            throw new BizException(400, "合作编码不能为空");
        }
        LambdaQueryWrapper<TenantCooperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantCooperation::getCooperationCode, cooperationCode);
        TenantCooperation entity = getOne(wrapper);
        if (entity == null) return null;
        TenantCooperationDTO dto = new TenantCooperationDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<TenantCooperationDTO> listByTenant(Long tenantId) {
        if (tenantId == null) {
            throw new BizException(400, "租户ID不能为空");
        }
        return lambdaQuery()
            .eq(TenantCooperation::getTenantId, tenantId)
            .list()
            .stream()
            .map(entity -> {
                TenantCooperationDTO dto = new TenantCooperationDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean suspend(Long id) {
        log.info("暂停合作: id={}", id);
        TenantCooperation entity = getById(id);
        if (entity == null) {
            throw new BizException(45001, "合作不存在");
        }
        entity.setStatus("suspended");
        updateById(entity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean activate(Long id) {
        log.info("激活合作: id={}", id);
        TenantCooperation entity = getById(id);
        if (entity == null) {
            throw new BizException(45001, "合作不存在");
        }
        entity.setStatus("active");
        updateById(entity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean terminate(Long id) {
        log.info("终止合作: id={}", id);
        TenantCooperation entity = getById(id);
        if (entity == null) {
            throw new BizException(45001, "合作不存在");
        }
        entity.setStatus("terminated");
        updateById(entity);
        return true;
    }
}
