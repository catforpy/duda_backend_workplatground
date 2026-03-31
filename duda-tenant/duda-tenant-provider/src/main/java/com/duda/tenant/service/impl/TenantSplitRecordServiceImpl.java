package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.tenant.api.dto.TenantSplitRecordDTO;
import com.duda.tenant.entity.TenantSplitRecord;
import com.duda.tenant.mapper.TenantSplitRecordMapper;
import com.duda.tenant.service.TenantSplitRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分账记录表服务实现
 */
@Slf4j
@Service
public class TenantSplitRecordServiceImpl
    extends ServiceImpl<TenantSplitRecordMapper, TenantSplitRecord>
    implements TenantSplitRecordService {

    @Autowired
    private TenantSplitRecordMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSplitRecord createRecord(TenantSplitRecordDTO dto) {
        log.info("创建分账记录: orderId={}", dto.getOrderId());
        TenantSplitRecord entity = new TenantSplitRecord();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(LocalDateTime.now());
        if (entity.getStatus() == null) {
            entity.setStatus("pending");
        }
        save(entity);
        log.info("创建分账记录成功: id={}", entity.getId());
        return entity;
    }

    @Override
    public TenantSplitRecordDTO getRecordDTO(Long id) {
        TenantSplitRecord entity = getById(id);
        if (entity == null) return null;
        TenantSplitRecordDTO dto = new TenantSplitRecordDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<TenantSplitRecordDTO> listByOrderId(Long orderId) {
        return lambdaQuery()
            .eq(TenantSplitRecord::getOrderId, orderId)
            .list()
            .stream()
            .map(entity -> {
                TenantSplitRecordDTO dto = new TenantSplitRecordDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<TenantSplitRecordDTO> listByCooperation(Long cooperationId) {
        return lambdaQuery()
            .eq(TenantSplitRecord::getCooperationId, cooperationId)
            .list()
            .stream()
            .map(entity -> {
                TenantSplitRecordDTO dto = new TenantSplitRecordDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }
}
