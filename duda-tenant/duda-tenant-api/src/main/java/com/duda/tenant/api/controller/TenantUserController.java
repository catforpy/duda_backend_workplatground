package com.duda.tenant.api.controller;

import com.duda.tenant.api.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 租户用户数据Controller
 * 提供用户数据同步到租户schema的功能
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant-user")
public class TenantUserController {

    @DubboReference(
        version = "1.0.0",
        group = "DUDA_TENANT_GROUP",
        check = false
    )
    private com.duda.tenant.api.rpc.TenantUserRpc tenantUserRpc;

    /**
     * 同步用户到指定租户
     */
    @PostMapping("/sync/{userId}")
    public ResultVO<String> syncUserToTenant(
            @PathVariable Long userId,
            @RequestParam Long tenantId,
            @RequestParam String tenantCode) {
        log.info("🔄 同步用户到租户: userId={}, tenantId={}, tenantCode={}",
                userId, tenantId, tenantCode);

        try {
            tenantUserRpc.syncUserToTenant(userId, tenantId, tenantCode);
            return ResultVO.success("用户已同步到租户" + tenantCode);

        } catch (Exception e) {
            log.error("❌ 同步失败", e);
            return ResultVO.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步用户到所有租户
     */
    @PostMapping("/sync-all/{userId}")
    public ResultVO<String> syncUserToAllTenants(@PathVariable Long userId) {
        log.info("🔄 同步用户到所有租户: userId={}", userId);

        try {
            tenantUserRpc.syncUserToAllTenants(userId);
            return ResultVO.success("用户已同步到所有租户");

        } catch (Exception e) {
            log.error("❌ 同步失败", e);
            return ResultVO.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 查询租户中的用户
     */
    @GetMapping("/list/{tenantId}/{tenantCode}")
    public ResultVO<List<Map<String, Object>>> getUsersFromTenant(
            @PathVariable Long tenantId,
            @PathVariable String tenantCode) {
        log.info("🔍 查询租户用户: tenantId={}, tenantCode={}", tenantId, tenantCode);

        try {
            List<Map<String, Object>> users = tenantUserRpc.getUsersFromTenant(tenantId, tenantCode);
            return ResultVO.success(users);

        } catch (Exception e) {
            log.error("❌ 查询失败", e);
            return ResultVO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 验证用户数据隔离
     */
    @GetMapping("/validate/{userId}")
    public ResultVO<Map<String, Object>> validateUserDataIsolation(@PathVariable Long userId) {
        log.info("🔍 验证用户数据隔离: userId={}", userId);

        try {
            Map<String, Object> result = tenantUserRpc.validateUserDataIsolation(userId);
            return ResultVO.success(result);

        } catch (Exception e) {
            log.error("❌ 验证失败", e);
            return ResultVO.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 完整的用户数据初始化
     * 同步用户到租户A和租户B
     */
    @PostMapping("/init/{userId}")
    public ResultVO<Map<String, Object>> initUserDataInTenants(@PathVariable Long userId) {
        log.info("🚀 初始化用户数据到租户: userId={}", userId);

        try {
            Map<String, Object> result = new java.util.HashMap<>();

            // 1. 同步到租户A
            log.info("📦 步骤1: 同步用户到租户A");
            tenantUserRpc.syncUserToTenant(userId, 24L, "TESTOSS_A");
            List<Map<String, Object>> usersA = tenantUserRpc.getUsersFromTenant(24L, "TESTOSS_A");
            result.put("tenantA", usersA);

            // 2. 同步到租户B
            log.info("📦 步骤2: 同步用户到租户B");
            tenantUserRpc.syncUserToTenant(userId, 25L, "TESTOSS_B");
            List<Map<String, Object>> usersB = tenantUserRpc.getUsersFromTenant(25L, "TESTOSS_B");
            result.put("tenantB", usersB);

            // 3. 验证数据隔离
            log.info("📦 步骤3: 验证用户数据隔离");
            Map<String, Object> validation = tenantUserRpc.validateUserDataIsolation(userId);
            result.put("validation", validation);

            result.put("status", "SUCCESS");
            result.put("message", "用户数据已初始化到租户");

            log.info("✅ 用户数据初始化完成");
            return ResultVO.success(result);

        } catch (Exception e) {
            log.error("❌ 初始化失败", e);
            return ResultVO.error("初始化失败: " + e.getMessage());
        }
    }
}
