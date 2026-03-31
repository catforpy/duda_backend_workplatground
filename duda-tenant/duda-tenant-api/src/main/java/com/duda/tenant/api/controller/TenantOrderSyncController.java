package com.duda.tenant.api.controller;

import com.duda.tenant.api.rpc.TenantOrderSyncRpc;
import com.duda.tenant.api.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 租户订单同步Controller
 * 提供订单同步到duda_nexus的功能
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/sync")
public class TenantOrderSyncController {

    @DubboReference(
        version = "1.0.0",
        group = "DUDA_TENANT_GROUP",
        check = false
    )
    private TenantOrderSyncRpc tenantOrderSyncRpc;

    /**
     * 同步所有租户订单到duda_nexus
     */
    @PostMapping("/orders/all")
    public ResultVO<String> syncAllOrders() {
        log.info("🔄 手动触发订单同步...");

        try {
            tenantOrderSyncRpc.syncAllTenantOrders();
            return ResultVO.success("订单同步成功，请在duda_nexus.tenant_orders表中查看");

        } catch (Exception e) {
            log.error("❌ 订单同步失败", e);
            return ResultVO.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 创建跨租户订单视图
     */
    @PostMapping("/orders/create-view")
    public ResultVO<String> createCrossTenantView() {
        log.info("🔨 创建跨租户订单视图...");

        try {
            tenantOrderSyncRpc.createCrossTenantView();
            return ResultVO.success("视图创建成功，请使用 SELECT * FROM v_cross_tenant_orders 查看");

        } catch (Exception e) {
            log.error("❌ 创建视图失败", e);
            return ResultVO.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 查询同步后的订单（从duda_nexus）
     */
    @GetMapping("/orders/tenant-orders")
    public ResultVO<java.util.List<Map<String, Object>>> getSyncedOrders() {
        log.info("🔍 查询duda_nexus中的同步订单...");

        try {
            java.util.List<Map<String, Object>> orders = tenantOrderSyncRpc.getSyncedOrders();
            return ResultVO.success(orders);

        } catch (Exception e) {
            log.error("❌ 查询失败", e);
            return ResultVO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询跨租户视图
     */
    @GetMapping("/orders/cross-tenant-view")
    public ResultVO<java.util.List<Map<String, Object>>> getCrossTenantView() {
        log.info("🔍 查询跨租户订单视图...");

        try {
            java.util.List<Map<String, Object>> orders = tenantOrderSyncRpc.getCrossTenantView();
            return ResultVO.success(orders);

        } catch (Exception e) {
            log.error("❌ 查询失败", e);
            return ResultVO.error("查询失败: " + e.getMessage());
        }
    }
}
