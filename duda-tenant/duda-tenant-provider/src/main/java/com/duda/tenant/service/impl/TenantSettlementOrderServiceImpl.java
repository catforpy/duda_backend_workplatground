package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.tenant.api.dto.TenantSettlementOrderDTO;
import com.duda.tenant.entity.TenantSettlementOrder;
import com.duda.tenant.mapper.TenantSettlementOrderMapper;
import com.duda.tenant.service.TenantSettlementOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 对账订单明细表服务实现
 */
@Slf4j
@Service
public class TenantSettlementOrderServiceImpl
    extends ServiceImpl<TenantSettlementOrderMapper, TenantSettlementOrder>
    implements TenantSettlementOrderService {

    @Autowired
    private TenantSettlementOrderMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantSettlementOrder addOrder(TenantSettlementOrderDTO dto) {
        log.info("添加订单到对账周期: periodId={}, orderId={}", dto.getSettlementPeriodId(), dto.getOrderId());
        TenantSettlementOrder entity = new TenantSettlementOrder();
        BeanUtils.copyProperties(dto, entity);
        save(entity);
        log.info("添加订单成功: id={}", entity.getId());
        return entity;
    }

    @Override
    public TenantSettlementOrderDTO getOrderDTO(Long id) {
        TenantSettlementOrder entity = getById(id);
        if (entity == null) return null;
        TenantSettlementOrderDTO dto = new TenantSettlementOrderDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<TenantSettlementOrderDTO> listByPeriodId(Long settlementPeriodId) {
        return lambdaQuery()
            .eq(TenantSettlementOrder::getSettlementPeriodId, settlementPeriodId)
            .list()
            .stream()
            .map(entity -> {
                TenantSettlementOrderDTO dto = new TenantSettlementOrderDTO();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Boolean isOrderCollected(Long orderId) {
        return lambdaQuery()
            .eq(TenantSettlementOrder::getOrderId, orderId)
            .count() > 0;
    }
}
