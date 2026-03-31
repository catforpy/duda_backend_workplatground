package com.duda.tenant.context;

import lombok.extern.slf4j.Slf4j;

/**
 * 租户上下文
 * 使用ThreadLocal存储当前线程的租户信息
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
public class TenantContext {

    /**
     * 租户ID持有者
     */
    private static final ThreadLocal<Long> TENANT_ID_HOLDER = new ThreadLocal<>();

    /**
     * 租户编码持有者
     */
    private static final ThreadLocal<String> TENANT_CODE_HOLDER = new ThreadLocal<>();

    /**
     * 数据库Schema名称持有者
     */
    private static final ThreadLocal<String> SCHEMA_NAME_HOLDER = new ThreadLocal<>();

    /**
     * 设置租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID_HOLDER.set(tenantId);
        log.debug("设置租户ID: tenantId={}", tenantId);
    }

    /**
     * 获取租户ID
     *
     * @return 租户ID
     */
    public static Long getTenantId() {
        return TENANT_ID_HOLDER.get();
    }

    /**
     * 设置租户编码
     *
     * @param tenantCode 租户编码
     */
    public static void setTenantCode(String tenantCode) {
        TENANT_CODE_HOLDER.set(tenantCode);
        log.debug("设置租户编码: tenantCode={}", tenantCode);
    }

    /**
     * 获取租户编码
     *
     * @return 租户编码
     */
    public static String getTenantCode() {
        return TENANT_CODE_HOLDER.get();
    }

    /**
     * 设置Schema名称
     *
     * @param schemaName Schema名称
     */
    public static void setSchemaName(String schemaName) {
        SCHEMA_NAME_HOLDER.set(schemaName);
        log.debug("设置Schema名称: schemaName={}", schemaName);
    }

    /**
     * 获取Schema名称
     *
     * @return Schema名称
     */
    public static String getSchemaName() {
        return SCHEMA_NAME_HOLDER.get();
    }

    /**
     * 清除当前租户上下文
     */
    public static void clear() {
        TENANT_ID_HOLDER.remove();
        TENANT_CODE_HOLDER.remove();
        SCHEMA_NAME_HOLDER.remove();
        log.debug("清除租户上下文");
    }

    /**
     * 判断当前是否有租户上下文
     *
     * @return true-有上下文，false-无上下文
     */
    public static boolean hasContext() {
        return TENANT_ID_HOLDER.get() != null;
    }
}
