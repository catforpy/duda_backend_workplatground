package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.tenant.api.dto.TenantCommissionRecordDTO;
import com.duda.tenant.entity.TenantCommissionRecord;
import com.duda.tenant.mapper.TenantCommissionRecordMapper;
import com.duda.tenant.service.TenantCommissionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 提成记录表服务实现（已废弃）
 */
@Slf4j
@Service
@Deprecated
public class TenantCommissionRecordServiceImpl
    extends ServiceImpl<TenantCommissionRecordMapper, TenantCommissionRecord>
    implements TenantCommissionRecordService {

    @Autowired
    private TenantCommissionRecordMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantCommissionRecord create(TenantCommissionRecordDTO dto) {
        log.info("创建提成记录（已废弃）: salesAgentId={}", dto.getSalesAgentId());
        TenantCommissionRecord entity = new TenantCommissionRecord();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(java.time.LocalDateTime.now());
        save(entity);
        return entity;
    }

    // 其他方法可以使用MyBatis Plus提供的基础CRUD方法
}
