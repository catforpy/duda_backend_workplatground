package com.duda.common.context;

/**
 * 租户上下文
 *
 * 使用ThreadLocal存储当前请求的租户ID
 * 用于多租户SAAS系统中的数据隔离
 *
 * 使用方法：
 * <pre>
 * // 1. 在拦截器或过滤器中设置租户ID
 * TenantContext.setTenantId(12345L);
 *
 * // 2. 在Service层获取租户ID
 * Long tenantId = TenantContext.getTenantId();
 *
 * // 3. 请求结束后清理租户ID（防止内存泄漏）
 * TenantContext.clear();
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public class TenantContext {

    /**
     * 使用ThreadLocal存储租户ID，实现线程隔离
     */
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    /**
     * 设置当前线程的租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * 获取当前线程的租户ID
     *
     * @return 租户ID，如果未设置则返回null
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 清除当前线程的租户ID
     *
     * 注意：请求结束后必须调用此方法，防止ThreadLocal内存泄漏
     */
    public static void clear() {
        TENANT_ID.remove();
    }

    /**
     * 检查当前线程是否已设置租户ID
     *
     * @return 如果已设置返回true，否则返回false
     */
    public static boolean hasTenantId() {
        return TENANT_ID.get() != null;
    }
}
