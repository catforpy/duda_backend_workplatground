package com.duda.tenant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 租户用户数据服务
 * 负责将用户数据同步到各个租户schema中
 * 实现真正的租户级用户隔离
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
@Slf4j
@Service
public class TenantUserDataService {

    @Autowired
    private DataSource dataSource;

    /**
     * 同步用户到指定租户schema
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @param tenantCode 租户编码
     */
    public void syncUserToTenant(Long userId, Long tenantId, String tenantCode) {
        log.info("🔄 同步用户到租户: userId={}, tenantId={}, tenantCode={}",
                userId, tenantId, tenantCode);

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 1. 从duda_nexus.users_00查询用户数据
            jdbcTemplate.execute("USE `duda_nexus`");
            String querySql = "SELECT id, tenant_id, username, password, email, phone, " +
                "real_name, avatar, status, user_type, create_time, update_time " +
                "FROM users_00 WHERE id = ? AND tenant_id = ?";

            List<Map<String, Object>> users = jdbcTemplate.queryForList(querySql, userId, tenantId);

            if (users.isEmpty()) {
                log.warn("⚠️ 用户不属于该租户: userId={}, tenantId={}", userId, tenantId);
                return;
            }

            Map<String, Object> user = users.get(0);
            log.info("  找到用户: username={}", user.get("username"));

            // 2. 切换到租户schema
            String schemaName = "tenant_" + tenantCode;
            jdbcTemplate.execute("USE `" + schemaName + "`");

            // 3. 检查用户是否已存在
            Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?",
                Long.class, userId
            );

            if (count == 0) {
                // 插入用户到租户schema
                String insertSql = "INSERT INTO users " +
                    "(id, tenant_id, username, password, email, phone, real_name, " +
                    "avatar, status, user_type, last_login_at, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                jdbcTemplate.update(insertSql,
                    user.get("id"),
                    user.get("tenant_id"),
                    user.get("username"),
                    user.get("password"),
                    user.get("email"),
                    user.get("phone"),
                    user.get("real_name"),
                    user.get("avatar"),
                    user.get("status"),
                    user.get("user_type"),
                    null,  // last_login_at
                    user.get("create_time"),
                    user.get("update_time")
                );

                log.info("✅ 用户已同步到租户{}: username={}", tenantCode, user.get("username"));
            } else {
                // 更新用户数据
                String updateSql = "UPDATE users SET " +
                    "username = ?, email = ?, phone = ?, real_name = ?, " +
                    "avatar = ?, status = ?, updated_at = ? " +
                    "WHERE id = ? AND tenant_id = ?";

                jdbcTemplate.update(updateSql,
                    user.get("username"),
                    user.get("email"),
                    user.get("phone"),
                    user.get("real_name"),
                    user.get("avatar"),
                    user.get("status"),
                    LocalDateTime.now(),
                    userId,
                    tenantId
                );

                log.info("✅ 用户数据已在租户{}中更新: username={}", tenantCode, user.get("username"));
            }

        } catch (Exception e) {
            log.error("❌ 同步用户到租户{}失败", tenantCode, e);
            throw new RuntimeException("同步用户失败: " + e.getMessage(), e);
        }
    }

    /**
     * 为所有租户同步指定用户
     * @param userId 用户ID
     */
    public void syncUserToAllTenants(Long userId) {
        log.info("🔄 同步用户到所有租户: userId={}", userId);

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute("USE `duda_nexus`");

            // 查询用户所属的所有租户
            String querySql = "SELECT DISTINCT tenant_id FROM users_00 WHERE id = ?";
            List<Map<String, Object>> tenants = jdbcTemplate.queryForList(querySql, userId);

            log.info("  用户属于{}个租户", tenants.size());

            for (Map<String, Object> tenant : tenants) {
                Long tenantId = ((Number) tenant.get("tenant_id")).longValue();

                // 查询租户编码
                String tenantCodeSql = "SELECT tenant_code FROM tenants WHERE id = ?";
                String tenantCode = jdbcTemplate.queryForObject(
                    tenantCodeSql, String.class, tenantId
                );

                // 同步到该租户
                syncUserToTenant(userId, tenantId, tenantCode);
            }

            log.info("✅ 用户已同步到所有租户");

        } catch (Exception e) {
            log.error("❌ 同步用户到所有租户失败", e);
            throw new RuntimeException("同步失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询租户中的用户
     */
    public List<Map<String, Object>> getUsersFromTenant(Long tenantId, String tenantCode) {
        log.info("🔍 查询租户{}中的用户", tenantCode);

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 切换到租户schema
            String schemaName = "tenant_" + tenantCode;
            jdbcTemplate.execute("USE `" + schemaName + "`");

            // 查询用户
            String sql = "SELECT * FROM users WHERE tenant_id = ? ORDER BY created_at DESC";
            List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, tenantId);

            log.info("✅ 租户{}有{}个用户", tenantCode, users.size());
            return users;

        } catch (Exception e) {
            log.error("❌ 查询租户{}用户失败", tenantCode, e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 验证用户数据隔离
     */
    public Map<String, Object> validateUserDataIsolation(Long userId) {
        log.info("🔍 验证用户数据隔离: userId={}", userId);

        Map<String, Object> result = new java.util.HashMap<>();

        try {
            // 查询租户A中的用户
            List<Map<String, Object>> usersA = getUsersFromTenant(24L, "TESTOSS_A");
            result.put("tenantA_users", usersA);

            // 查询租户B中的用户
            List<Map<String, Object>> usersB = getUsersFromTenant(25L, "TESTOSS_B");
            result.put("tenantB_users", usersB);

            // 验证用户在租户中是否存在
            boolean userInA = usersA.stream().anyMatch(u ->
                ((Number) u.get("id")).longValue() == userId
            );

            boolean userInB = usersB.stream().anyMatch(u ->
                ((Number) u.get("id")).longValue() == userId
            );

            result.put("user_in_tenant_a", userInA);
            result.put("user_in_tenant_b", userInB);
            result.put("validation_passed", userInA && userInB);

            log.info("✅ 用户数据隔离验证完成: 租户A={}, 租户B={}",
                userInA ? "存在" : "不存在",
                userInB ? "存在" : "不存在"
            );

            return result;

        } catch (Exception e) {
            log.error("❌ 验证用户数据隔离失败", e);
            result.put("error", e.getMessage());
            return result;
        }
    }
}
