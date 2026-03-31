package com.duda.tenant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 租户订单同步服务
 * 将各个租户schema中的订单数据同步到duda_nexus的tenant_orders表
 * 用于在主数据库中统一查看和管理所有租户的订单
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
@Slf4j
@Service
public class TenantOrderSyncService {

    @Autowired
    private DataSource dataSource;

    /**
     * 同步所有租户的订单到duda_nexus.tenant_orders表
     */
    public void syncAllTenantOrders() {
        log.info("🔄 开始同步所有租户订单...");

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 1. 同步租户A的订单
            syncTenantOrders(24L, "TESTOSS_A", jdbcTemplate);

            // 2. 同步租户B的订单
            syncTenantOrders(25L, "TESTOSS_B", jdbcTemplate);

            log.info("✅ 所有租户订单同步完成");

        } catch (Exception e) {
            log.error("❌ 同步租户订单失败", e);
        }
    }

    /**
     * 同步指定租户的订单
     */
    private void syncTenantOrders(Long tenantId, String tenantCode, JdbcTemplate jdbcTemplate) {
        log.info("🔄 同步租户{}的订单: tenantId={}", tenantCode, tenantId);

        try {
            // 1. 切换到租户schema查询订单
            String schemaName = "tenant_" + tenantCode;
            String querySql = String.format(
                "SELECT id, order_no, customer_id, total_amount, status, created_at, updated_at " +
                "FROM `%s`.`orders` WHERE tenant_id = %d",
                schemaName, tenantId
            );

            List<Map<String, Object>> orders = jdbcTemplate.queryForList(querySql);
            log.info("  租户{}有{}条订单", tenantCode, orders.size());

            // 2. 切换回duda_nexus
            jdbcTemplate.execute("USE `duda_nexus`");

            // 3. 对每条订单，同步到tenant_orders表
            for (Map<String, Object> order : orders) {
                syncSingleOrder(tenantId, tenantCode, order, jdbcTemplate);
            }

            log.info("✅ 租户{}订单同步完成", tenantCode);

        } catch (Exception e) {
            log.error("❌ 同步租户{}订单失败", tenantCode, e);
        }
    }

    /**
     * 同步单条订单
     */
    private void syncSingleOrder(Long tenantId, String tenantCode,
                                 Map<String, Object> order, JdbcTemplate jdbcTemplate) {
        try {
            Long orderId = ((Number) order.get("id")).longValue();
            String orderNo = (String) order.get("order_no");
            Long customerId = ((Number) order.get("customer_id")).longValue();
            BigDecimal totalAmount = (BigDecimal) order.get("total_amount");
            String status = (String) order.get("status");

            // 检查订单是否已存在
            Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tenant_orders WHERE tenant_id = ? AND order_no = ?",
                Long.class, tenantId, orderNo
            );

            if (count == 0) {
                // 插入新订单
                String insertSql = "INSERT INTO tenant_orders " +
                    "(tenant_id, tenant_code, order_no, customer_id, total_amount, status, " +
                    "original_order_id, created_time, updated_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

                jdbcTemplate.update(insertSql,
                    tenantId, tenantCode, orderNo, customerId, totalAmount, status, orderId
                );

                log.debug("  插入新订单: tenant={}, orderNo={}", tenantCode, orderNo);
            } else {
                // 更新订单状态
                String updateSql = "UPDATE tenant_orders SET " +
                    "status = ?, updated_time = NOW() " +
                    "WHERE tenant_id = ? AND order_no = ?";

                jdbcTemplate.update(updateSql, status, tenantId, orderNo);

                log.debug("  更新订单: tenant={}, orderNo={}, status={}", tenantCode, orderNo, status);
            }

        } catch (Exception e) {
            log.error("❌ 同步订单失败: tenant={}, orderNo={}", tenantCode, order.get("order_no"), e);
        }
    }

    /**
     * 创建跨租户订单视图
     */
    public void createCrossTenantView() {
        log.info("🔨 创建跨租户订单视图...");

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute("USE `duda_nexus`");

            // 删除旧视图（如果存在）
            try {
                jdbcTemplate.execute("DROP VIEW IF EXISTS v_cross_tenant_orders");
                log.info("  删除旧视图");
            } catch (Exception e) {
                // 视图不存在，忽略
            }

            // 创建新视图
            String createViewSql =
                "CREATE VIEW v_cross_tenant_orders AS " +
                "SELECT " +
                "  24 AS tenant_id, " +
                "  'TESTOSS_A' AS tenant_code, " +
                "  id, " +
                "  order_no, " +
                "  customer_id, " +
                "  total_amount, " +
                "  status, " +
                "  created_at, " +
                "  updated_at " +
                "FROM tenant_testossa_a.orders " +
                "UNION ALL " +
                "SELECT " +
                "  25 AS tenant_id, " +
                "  'TESTOSS_B' AS tenant_code, " +
                "  id, " +
                "  order_no, " +
                "  customer_id, " +
                "  total_amount, " +
                "  status, " +
                "  created_at, " +
                "  updated_at " +
                "FROM tenant_testoss_b.orders";

            jdbcTemplate.execute(createViewSql);
            log.info("✅ 跨租户订单视图创建成功");

        } catch (Exception e) {
            log.error("❌ 创建跨租户订单视图失败", e);
        }
    }

    /**
     * 定时同步（每5分钟执行一次）
     */
    @Scheduled(fixedRate = 300000)  // 5分钟
    public void scheduledSync() {
        log.info("⏰ 定时同步租户订单...");
        syncAllTenantOrders();
    }

    /**
     * 查询同步后的订单（从duda_nexus.tenant_orders）
     */
    public List<Map<String, Object>> getSyncedOrders() {
        log.info("🔍 查询duda_nexus中的同步订单...");

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute("USE `duda_nexus`");

            String sql = "SELECT * FROM tenant_orders ORDER BY created_time DESC";
            List<Map<String, Object>> orders = jdbcTemplate.queryForList(sql);

            log.info("✅ 查询到{}条同步订单", orders.size());
            return orders;

        } catch (Exception e) {
            log.error("❌ 查询同步订单失败", e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 查询跨租户视图
     */
    public List<Map<String, Object>> getCrossTenantView() {
        log.info("🔍 查询跨租户订单视图...");

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute("USE `duda_nexus`");

            String sql = "SELECT * FROM v_cross_tenant_orders ORDER BY created_at DESC";
            List<Map<String, Object>> orders = jdbcTemplate.queryForList(sql);

            log.info("✅ 查询到{}条视图订单", orders.size());
            return orders;

        } catch (Exception e) {
            log.error("❌ 查询视图订单失败", e);
            return new java.util.ArrayList<>();
        }
    }
}
