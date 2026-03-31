package com.duda.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.tenant.entity.TenantSettlementOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对账订单明细表Mapper
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Mapper
public interface TenantSettlementOrderMapper extends BaseMapper<TenantSettlementOrder> {
}
