package com.duda.tenant.service;

import com.duda.tenant.entity.TenantOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 跨租户测试数据服务
 * 用于在多个租户schema中创建测试数据
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
@Slf4j
@Service
public class CrossTenantTestDataService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TenantUserDataService tenantUserDataService;

    /**
     * 在指定租户的schema中初始化商品数据
     */
    public void initProductsForTenant(Long tenantId, String tenantCode) {
        log.info("📦 为租户初始化商品数据: tenantId={}, tenantCode={}", tenantId, tenantCode);

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 切换到租户schema
            String schemaName = "tenant_" + tenantCode;
            jdbcTemplate.execute("USE `" + schemaName + "`");

            // 清空现有商品
            jdbcTemplate.execute("DELETE FROM products WHERE tenant_id = " + tenantId);
            log.info("  清空租户{}现有商品", tenantCode);

            // 插入测试商品
            String sql = "INSERT INTO products (tenant_id, product_name, price, description, stock, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";

            if (tenantId == 24L) {
                // 租户A的商品
                jdbcTemplate.update(sql, tenantId, "阿里云OSS存储A套餐", 99.00, "租户A的云存储产品", 100, "ON_SALE");
                jdbcTemplate.update(sql, tenantId, "阿里云CDN加速A套餐", 199.00, "租户A的CDN产品", 50, "ON_SALE");
                jdbcTemplate.update(sql, tenantId, "阿里云云服务器A套餐", 299.00, "租户A的ECS产品", 20, "ON_SALE");
            } else if (tenantId == 25L) {
                // 租户B的商品
                jdbcTemplate.update(sql, tenantId, "阿里云OSS存储B套餐", 89.00, "租户B的云存储产品", 100, "ON_SALE");
                jdbcTemplate.update(sql, tenantId, "阿里云CDN加速B套餐", 179.00, "租户B的CDN产品", 50, "ON_SALE");
                jdbcTemplate.update(sql, tenantId, "阿里云云服务器B套餐", 269.00, "租户B的ECS产品", 20, "ON_SALE");
            }

            log.info("✅ 租户{}商品初始化完成", tenantCode);

        } catch (Exception e) {
            log.error("❌ 初始化租户{}商品失败", tenantCode, e);
            throw new RuntimeException("初始化商品失败: " + e.getMessage(), e);
        }
    }

    /**
     * 在指定租户的schema中为用户创建订单
     */
    public void createOrderForTenant(Long tenantId, String tenantCode, Long userId, BigDecimal amount, String status) {
        log.info("📝 在租户{}创建订单: userId={}, amount={}", tenantCode, userId, amount);

        try {
            // 先同步用户数据到租户schema
            log.info("🔄 先同步用户数据到租户{}", tenantCode);
            tenantUserDataService.syncUserToTenant(userId, tenantId, tenantCode);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 切换到租户schema
            String schemaName = "tenant_" + tenantCode;
            jdbcTemplate.execute("USE `" + schemaName + "`");

            // 生成订单号
            String orderNo = "ORD-" + tenantCode.toUpperCase().charAt(tenantCode.length() - 1) + "-" + System.currentTimeMillis();

            // 插入订单
            String sql = "INSERT INTO orders (tenant_id, order_no, customer_id, total_amount, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
            jdbcTemplate.update(sql, tenantId, orderNo, userId, amount, status);

            log.info("✅ 租户{}订单创建成功: orderNo={}", tenantCode, orderNo);

        } catch (Exception e) {
            log.error("❌ 在租户{}创建订单失败", tenantCode, e);
            throw new RuntimeException("创建订单失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询指定租户的订单
     */
    public List<Map<String, Object>> getOrdersFromTenant(Long tenantId, String tenantCode) {
        log.info("🔍 查询租户{}的订单", tenantCode);

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 切换到租户schema
            String schemaName = "tenant_" + tenantCode;
            jdbcTemplate.execute("USE `" + schemaName + "`");

            // 查询订单
            String sql = "SELECT * FROM orders WHERE tenant_id = ? ORDER BY created_at DESC";
            List<Map<String, Object>> orders = jdbcTemplate.queryForList(sql, tenantId);

            log.info("✅ 租户{}查询到{}条订单", tenantCode, orders.size());
            return orders;

        } catch (Exception e) {
            log.error("❌ 查询租户{}订单失败", tenantCode, e);
            throw new RuntimeException("查询订单失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询指定租户的商品
     */
    public List<Map<String, Object>> getProductsFromTenant(Long tenantId, String tenantCode) {
        log.info("🔍 查询租户{}的商品", tenantCode);

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 切换到租户schema
            String schemaName = "tenant_" + tenantCode;
            jdbcTemplate.execute("USE `" + schemaName + "`");

            // 查询商品
            String sql = "SELECT * FROM products WHERE tenant_id = ? ORDER BY id";
            List<Map<String, Object>> products = jdbcTemplate.queryForList(sql, tenantId);

            log.info("✅ 租户{}查询到{}个商品", tenantCode, products.size());
            return products;

        } catch (Exception e) {
            log.error("❌ 查询租户{}商品失败", tenantCode, e);
            throw new RuntimeException("查询商品失败: " + e.getMessage(), e);
        }
    }

    /**
     * 跨租户查询用户的所有订单
     */
    public Map<String, List<Map<String, Object>>> getUserOrdersAcrossTenants(Long userId) {
        log.info("🔍 跨租户查询用户订单: userId={}", userId);

        Map<String, List<Map<String, Object>>> result = new HashMap<>();

        try {
            // 查询租户A的订单
            List<Map<String, Object>> ordersA = getUserOrdersFromSpecificTenant(24L, "TESTOSS_A", userId);
            result.put("tenantA", ordersA);

            // 查询租户B的订单
            List<Map<String, Object>> ordersB = getUserOrdersFromSpecificTenant(25L, "TESTOSS_B", userId);
            result.put("tenantB", ordersB);

            log.info("✅ 跨租户订单查询完成: 租户A={}, 租户B={}", ordersA.size(), ordersB.size());

            return result;

        } catch (Exception e) {
            log.error("❌ 跨租户查询用户订单失败", e);
            throw new RuntimeException("跨租户查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从指定租户查询用户的订单
     */
    private List<Map<String, Object>> getUserOrdersFromSpecificTenant(Long tenantId, String tenantCode, Long userId) {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 切换到租户schema
            String schemaName = "tenant_" + tenantCode;
            jdbcTemplate.execute("USE `" + schemaName + "`");

            // 查询用户在该租户的订单
            String sql = "SELECT * FROM orders WHERE tenant_id = ? AND customer_id = ? ORDER BY created_at DESC";
            List<Map<String, Object>> orders = jdbcTemplate.queryForList(sql, tenantId, userId);

            return orders;

        } catch (Exception e) {
            log.error("❌ 查询用户在租户{}的订单失败", tenantCode, e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 完整的跨租户业务测试
     */
    public Map<String, Object> runFullCrossTenantTest(Long userId) {
        log.info("🚀 开始完整跨租户业务测试: userId={}", userId);

        Map<String, Object> result = new HashMap<>();

        try {
            // 步骤1: 初始化租户A的商品
            log.info("📦 步骤1: 初始化租户A商品");
            initProductsForTenant(24L, "TESTOSS_A");
            List<Map<String, Object>> productsA = getProductsFromTenant(24L, "TESTOSS_A");
            result.put("step1_products_tenant_a", productsA);

            // 步骤2: 初始化租户B的商品
            log.info("📦 步骤2: 初始化租户B商品");
            initProductsForTenant(25L, "TESTOSS_B");
            List<Map<String, Object>> productsB = getProductsFromTenant(25L, "TESTOSS_B");
            result.put("step2_products_tenant_b", productsB);

            // 步骤3: 在租户A创建订单
            log.info("🛒 步骤3: 在租户A购买");
            createOrderForTenant(24L, "TESTOSS_A", userId, new BigDecimal("99.00"), "PENDING");
            createOrderForTenant(24L, "TESTOSS_A", userId, new BigDecimal("299.00"), "COMPLETED");

            // 步骤4: 在租户B创建订单
            log.info("🛒 步骤4: 在租户B购买");
            createOrderForTenant(25L, "TESTOSS_B", userId, new BigDecimal("89.00"), "PENDING");
            createOrderForTenant(25L, "TESTOSS_B", userId, new BigDecimal("269.00"), "COMPLETED");

            // 步骤5: 验证租户A的订单（数据隔离）
            log.info("🔍 步骤5: 验证租户A数据隔离");
            List<Map<String, Object>> ordersA = getOrdersFromTenant(24L, "TESTOSS_A");
            result.put("step5_orders_tenant_a", ordersA);
            log.info("  租户A订单数: {}", ordersA.size());

            // 步骤6: 验证租户B的订单（数据隔离）
            log.info("🔍 步骤6: 验证租户B数据隔离");
            List<Map<String, Object>> ordersB = getOrdersFromTenant(25L, "TESTOSS_B");
            result.put("step6_orders_tenant_b", ordersB);
            log.info("  租户B订单数: {}", ordersB.size());

            // 步骤7: 用户跨租户订单汇总
            log.info("🔍 步骤7: 用户跨租户订单视图");
            Map<String, List<Map<String, Object>>> userOrders = getUserOrdersAcrossTenants(userId);
            result.put("step7_user_cross_tenant_view", userOrders);

            // 步骤8: 验证数据隔离
            log.info("✅ 步骤8: 验证数据隔离");
            boolean isolationValid = validateDataIsolation(ordersA, ordersB);
            result.put("step8_data_isolation_valid", isolationValid);

            log.info("🎉 完整跨租户业务测试完成!");
            result.put("test_status", "SUCCESS");
            result.put("test_message", "跨租户业务测试完成");

            return result;

        } catch (Exception e) {
            log.error("❌ 跨租户业务测试失败", e);
            result.put("test_status", "FAILED");
            result.put("test_error", e.getMessage());
            return result;
        }
    }

    /**
     * 验证数据隔离
     */
    private boolean validateDataIsolation(List<Map<String, Object>> ordersA, List<Map<String, Object>> ordersB) {
        log.info("🔒 验证数据隔离...");

        // 验证租户A的订单都包含tenant_id=24
        boolean allOrdersAHaveCorrectTenantId = ordersA.stream()
                .allMatch(order -> {
                    Object tenantId = order.get("tenant_id");
                    // tenant_id可能是Long或Integer
                    Long tid = (tenantId instanceof Long) ? (Long) tenantId : Long.valueOf(tenantId.toString());
                    return tid == 24L;
                });

        // 验证租户B的订单都包含tenant_id=25
        boolean allOrdersBHaveCorrectTenantId = ordersB.stream()
                .allMatch(order -> {
                    Object tenantId = order.get("tenant_id");
                    Long tid = (tenantId instanceof Long) ? (Long) tenantId : Long.valueOf(tenantId.toString());
                    return tid == 25L;
                });

        boolean isValid = allOrdersAHaveCorrectTenantId && allOrdersBHaveCorrectTenantId;

        if (isValid) {
            log.info("✅ 数据隔离验证通过");
            log.info("  租户A的所有订单都包含tenant_id=24");
            log.info("  租户B的所有订单都包含tenant_id=25");
        } else {
            log.error("❌ 数据隔离验证失败");
        }

        return isValid;
    }
}
