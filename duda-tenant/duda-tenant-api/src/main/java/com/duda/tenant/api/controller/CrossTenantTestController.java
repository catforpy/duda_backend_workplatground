package com.duda.tenant.api.controller;

import com.duda.tenant.api.rpc.CrossTenantTestDataRpc;
import com.duda.tenant.api.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 跨租户业务场景测试Controller
 * 用于测试用户在多个租户间购买商品的完整流程
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/test/cross-tenant")
public class CrossTenantTestController {

    @DubboReference(
        version = "1.0.0",
        group = "DUDA_TENANT_GROUP",
        check = false
    )
    private CrossTenantTestDataRpc crossTenantTestDataRpc;

    /**
     * 1. 为租户A和租户B初始化测试商品
     */
    @PostMapping("/init-products")
    public ResultVO<Map<String, Object>> initProducts() {
        log.info("📦 开始初始化租户商品数据...");

        try {
            Map<String, Object> result = new java.util.HashMap<>();

            // 初始化租户A商品
            crossTenantTestDataRpc.initProductsForTenant(24L, "TESTOSS_A");
            List<Map<String, Object>> productsA = crossTenantTestDataRpc.getProductsFromTenant(24L, "TESTOSS_A");
            result.put("productsA", productsA);

            // 初始化租户B商品
            crossTenantTestDataRpc.initProductsForTenant(25L, "TESTOSS_B");
            List<Map<String, Object>> productsB = crossTenantTestDataRpc.getProductsFromTenant(25L, "TESTOSS_B");
            result.put("productsB", productsB);

            result.put("message", "商品初始化成功");
            log.info("✅ 商品初始化完成: A={}, B={}", productsA.size(), productsB.size());

            return ResultVO.success(result);

        } catch (Exception e) {
            log.error("❌ 商品初始化失败", e);
            return ResultVO.error("商品初始化失败: " + e.getMessage());
        }
    }

    /**
     * 2. 在租户A中创建订单
     */
    @PostMapping("/create-order/tenant-a")
    public ResultVO<String> createOrderInTenantA(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "99.00") Double amount) {
        log.info("📝 在租户A创建订单: userId={}, amount={}", userId, amount);

        try {
            crossTenantTestDataRpc.createOrderForTenant(
                24L, "TESTOSS_A", userId,
                new java.math.BigDecimal(amount),
                "PENDING"
            );
            return ResultVO.success("租户A订单创建成功");

        } catch (Exception e) {
            log.error("❌ 租户A订单创建失败", e);
            return ResultVO.error("订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 3. 在租户B中创建订单
     */
    @PostMapping("/create-order/tenant-b")
    public ResultVO<String> createOrderInTenantB(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "89.00") Double amount) {
        log.info("📝 在租户B创建订单: userId={}, amount={}", userId, amount);

        try {
            crossTenantTestDataRpc.createOrderForTenant(
                25L, "TESTOSS_B", userId,
                new java.math.BigDecimal(amount),
                "PENDING"
            );
            return ResultVO.success("租户B订单创建成功");

        } catch (Exception e) {
            log.error("❌ 租户B订单创建失败", e);
            return ResultVO.error("订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 4. 查询租户A的订单（验证数据隔离）
     */
    @GetMapping("/orders/tenant-a")
    public ResultVO<List<Map<String, Object>>> getOrdersFromTenantA() {
        log.info("🔍 查询租户A的订单...");

        try {
            List<Map<String, Object>> orders = crossTenantTestDataRpc.getOrdersFromTenant(24L, "TESTOSS_A");
            log.info("✅ 租户A订单查询成功: count={}", orders.size());
            return ResultVO.success(orders);

        } catch (Exception e) {
            log.error("❌ 租户A订单查询失败", e);
            return ResultVO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 5. 查询租户B的订单（验证数据隔离）
     */
    @GetMapping("/orders/tenant-b")
    public ResultVO<List<Map<String, Object>>> getOrdersFromTenantB() {
        log.info("🔍 查询租户B的订单...");

        try {
            List<Map<String, Object>> orders = crossTenantTestDataRpc.getOrdersFromTenant(25L, "TESTOSS_B");
            log.info("✅ 租户B订单查询成功: count={}", orders.size());
            return ResultVO.success(orders);

        } catch (Exception e) {
            log.error("❌ 租户B订单查询失败", e);
            return ResultVO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 6. 跨租户查询用户的所有订单（用户视图）
     */
    @GetMapping("/orders/user/{userId}")
    public ResultVO<Map<String, List<Map<String, Object>>>> getUserOrdersAcrossTenants(
            @PathVariable Long userId) {
        log.info("🔍 跨租户查询用户订单: userId={}", userId);

        try {
            Map<String, List<Map<String, Object>>> userOrders =
                crossTenantTestDataRpc.getUserOrdersAcrossTenants(userId);

            log.info("✅ 跨租户订单查询成功: tenantA={}, tenantB={}",
                userOrders.get("tenantA").size(),
                userOrders.get("tenantB").size());

            return ResultVO.success(userOrders);

        } catch (Exception e) {
            log.error("❌ 跨租户订单查询失败", e);
            return ResultVO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 7. 完整业务流程测试
     */
    @PostMapping("/full-test")
    public ResultVO<Map<String, Object>> runFullTest(
            @RequestParam(defaultValue = "5147348110413824") Long userId) {
        log.info("🚀 开始完整跨租户业务测试: userId={}", userId);

        try {
            Map<String, Object> result = crossTenantTestDataRpc.runFullCrossTenantTest(userId);
            return ResultVO.success(result);

        } catch (Exception e) {
            log.error("❌ 跨租户业务测试失败", e);
            return ResultVO.error("测试失败: " + e.getMessage());
        }
    }
}
