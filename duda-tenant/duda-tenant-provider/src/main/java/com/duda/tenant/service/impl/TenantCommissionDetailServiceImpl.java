package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.web.exception.BizException;
import com.duda.tenant.api.dto.TenantCommissionDetailDTO;
import com.duda.tenant.entity.TenantCommissionDetail;
import com.duda.tenant.mapper.TenantCommissionDetailMapper;
import com.duda.tenant.service.TenantCommissionDetailService;
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
 * 分佣明细表服务实现
 */
@Slf4j
@Service
public class TenantCommissionDetailServiceImpl
    extends ServiceImpl<TenantCommissionDetailMapper, TenantCommissionDetail>
    implements TenantCommissionDetailService {

    @Autowired
    private TenantCommissionDetailMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantCommissionDetail createDetail(TenantCommissionDetailDTO dto) {
        log.info("创建分佣明细: orderId={}, beneficiaryId={}", dto.getOrderId(), dto.getBeneficiaryId());
        TenantCommissionDetail entity = new TenantCommissionDetail();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        if (entity.getStatus() == null) {
            entity.setStatus("pending");
        }
        save(entity);
        log.info("创建分佣明细成功: id={}", entity.getId());
        return entity;
    }

    @Override
    public TenantCommissionDetailDTO getDetailDTO(Long id) {
        if (id == null) {
            throw new BizException(400, "ID不能为空");
        }
        TenantCommissionDetail entity = getById(id);
        if (entity == null) {
            throw new BizException(41002, "分佣明细不存在");
        }
        TenantCommissionDetailDTO dto = new TenantCommissionDetailDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<TenantCommissionDetailDTO> listByOrderId(Long orderId) {
        return lambdaQuery()
            .eq(TenantCommissionDetail::getOrderId, orderId)
            .list()
            .stream()
            .map(entity -> {
                TenantCommissionDetailDTO dto = new TenantCommissionDetailDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<TenantCommissionDetailDTO> listByBeneficiary(Long beneficiaryId, String status) {
        return lambdaQuery()
            .eq(TenantCommissionDetail::getBeneficiaryId, beneficiaryId)
            .eq(status != null, TenantCommissionDetail::getStatus, status)
            .list()
            .stream()
            .map(entity -> {
                TenantCommissionDetailDTO dto = new TenantCommissionDetailDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<TenantCommissionDetailDTO> listByPeriodId(Long settlementPeriodId) {
        return lambdaQuery()
            .eq(TenantCommissionDetail::getSettlementPeriodId, settlementPeriodId)
            .list()
            .stream()
            .map(entity -> {
                TenantCommissionDetailDTO dto = new TenantCommissionDetailDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean settle(Long id, String settledMethod) {
        log.info("结算分佣: detailId={}, settledMethod={}", id, settledMethod);
        TenantCommissionDetail entity = getById(id);
        if (entity == null) {
            throw new BizException(41002, "分佣明细不存在");
        }
        entity.setStatus("settled");
        entity.setSettledAt(LocalDateTime.now());
        entity.setSettledMethod(settledMethod);
        entity.setUpdatedAt(LocalDateTime.now());
        updateById(entity);
        log.info("结算分佣成功: id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchSettle(Long settlementPeriodId, String settledMethod) {
        log.info("批量结算分佣: settlementPeriodId={}, settledMethod={}", settlementPeriodId, settledMethod);
        List<TenantCommissionDetail> list = lambdaQuery()
            .eq(TenantCommissionDetail::getSettlementPeriodId, settlementPeriodId)
            .eq(TenantCommissionDetail::getStatus, "pending")
            .list();

        for (TenantCommissionDetail entity : list) {
            entity.setStatus("settled");
            entity.setSettledAt(LocalDateTime.now());
            entity.setSettledMethod(settledMethod);
            entity.setUpdatedAt(LocalDateTime.now());
        }

        updateBatchById(list);
        log.info("批量结算分佣成功: count={}", list.size());
        return true;
    }

    @Override
    public BigDecimal calculateCommission(Long orderId, BigDecimal orderAmount) {
        // TODO: 实现分佣计算逻辑
        log.info("计算分佣: orderId={}, orderAmount={}", orderId, orderAmount);
        return BigDecimal.ZERO;
    }
}
