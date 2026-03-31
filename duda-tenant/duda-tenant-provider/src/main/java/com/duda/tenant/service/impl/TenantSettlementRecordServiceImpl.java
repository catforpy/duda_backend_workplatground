package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantSettlementRecordDTO;
import com.duda.tenant.entity.TenantSettlementRecord;
import com.duda.tenant.mapper.TenantSettlementRecordMapper;
import com.duda.tenant.service.TenantSettlementRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 结算流水表服务实现
 */
@Slf4j
@Service
public class TenantSettlementRecordServiceImpl
    extends ServiceImpl<TenantSettlementRecordMapper, TenantSettlementRecord>
    implements TenantSettlementRecordService {

    @Autowired
    private TenantSettlementRecordMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSettlementRecord createRecord(TenantSettlementRecordDTO dto) {
        log.info("创建结算流水: periodId={}, transferNo={}", dto.getSettlementPeriodId(), dto.getTransferNo());

        if (dto.getTransferNo() == null) {
            throw new BizException(400, "转账流水号不能为空");
        }

        LambdaQueryWrapper<TenantSettlementRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSettlementRecord::getTransferNo, dto.getTransferNo());
        if (count(wrapper) > 0) {
            throw new BizException(409, "转账流水号已存在");
        }

        TenantSettlementRecord entity = new TenantSettlementRecord();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        if (entity.getStatus() == null) {
            entity.setStatus("pending");
        }
        if (entity.getRetryCount() == null) {
            entity.setRetryCount(0);
        }
        save(entity);
        log.info("创建结算流水成功: id={}", entity.getId());
        return entity;
    }

    @Override
    public TenantSettlementRecordDTO getRecordDTO(Long id) {
        if (id == null) {
            throw new BizException(400, "ID不能为空");
        }
        TenantSettlementRecord entity = getById(id);
        if (entity == null) {
            throw new BizException(40002, "结算流水不存在");
        }
        TenantSettlementRecordDTO dto = new TenantSettlementRecordDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public TenantSettlementRecordDTO getByTransferNo(String transferNo) {
        LambdaQueryWrapper<TenantSettlementRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSettlementRecord::getTransferNo, transferNo);
        TenantSettlementRecord entity = getOne(wrapper);
        if (entity == null) return null;
        TenantSettlementRecordDTO dto = new TenantSettlementRecordDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<TenantSettlementRecordDTO> listByPeriodId(Long settlementPeriodId) {
        return lambdaQuery()
            .eq(TenantSettlementRecord::getSettlementPeriodId, settlementPeriodId)
            .list()
            .stream()
            .map(entity -> {
                TenantSettlementRecordDTO dto = new TenantSettlementRecordDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean initiateTransfer(Long id) {
        log.info("发起转账: recordId={}", id);
        TenantSettlementRecord entity = getById(id);
        if (entity == null) {
            throw new BizException(40002, "结算流水不存在");
        }
        entity.setStatus("processing");
        entity.setTransferInitiatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        updateById(entity);
        log.info("发起转账成功: id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean retryTransfer(Long id) {
        log.info("重试转账: recordId={}", id);
        TenantSettlementRecord entity = getById(id);
        if (entity == null) {
            throw new BizException(40002, "结算流水不存在");
        }
        entity.setRetryCount(entity.getRetryCount() + 1);
        entity.setStatus("processing");
        entity.setUpdatedAt(LocalDateTime.now());
        updateById(entity);
        log.info("重试转账成功: id={}, retryCount={}", id, entity.getRetryCount());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmSuccess(Long id) {
        log.info("确认转账成功: recordId={}", id);
        TenantSettlementRecord entity = getById(id);
        if (entity == null) {
            throw new BizException(40002, "结算流水不存在");
        }
        entity.setStatus("success");
        entity.setTransferCompletedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        updateById(entity);
        log.info("确认转账成功: id={}", id);
        return true;
    }
}
