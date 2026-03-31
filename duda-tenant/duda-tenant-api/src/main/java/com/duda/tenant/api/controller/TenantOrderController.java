package com.duda.tenant.api.controller;

import com.duda.tenant.api.dto.TenantOrderDTO;
import com.duda.tenant.api.service.TenantOrderService;
import com.duda.tenant.api.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户订单Controller
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
public class TenantOrderController {

    @Autowired
    private TenantOrderService tenantOrderService;

    /**
     * 根据租户ID查询订单列表
     *
     * @param tenantId 租户ID
     * @return 订单列表
     */
    @GetMapping("/tenant/{tenantId}")
    public ResultVO<List<TenantOrderDTO>> listByTenantId(@PathVariable Long tenantId) {
        log.info("查询租户订单列表: tenantId={}", tenantId);
        List<TenantOrderDTO> orders = tenantOrderService.listByTenantId(tenantId);
        return ResultVO.success(orders);
    }

    /**
     * 根据ID查询订单
     *
     * @param id 订单ID
     * @return 订单DTO
     */
    @GetMapping("/{id}")
    public ResultVO<TenantOrderDTO> getById(@PathVariable Long id) {
        log.info("查询订单: id={}", id);
        TenantOrderDTO order = tenantOrderService.getById(id);
        return ResultVO.success(order);
    }
}
