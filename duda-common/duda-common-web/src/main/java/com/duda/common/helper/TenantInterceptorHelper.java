package com.duda.common.helper;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 租户拦截器辅助类
 *
 * <p>功能说明：提供多种方式提取tenant_id
 * <ul>
 *   <li>从HTTP Header提取（开发环境）</li>
 *   <li>从JWT Token提取（生产环境）</li>
 *   <li>从请求参数提取（测试环境）</li>
 * </ul>
 *
 * <p>提取优先级：
 * <ol>
 *   <li>HTTP Header: X-Tenant-Id（优先级最高，用于开发）</li>
 *   <li>JWT Token: Authorization: Bearer <token>（生产环境）</li>
 *   <li>请求参数: ?tenant_id=1（测试环境）</li>
 *   <li>返回null（由拦截器决定默认值）</li>
 * </ol>
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
public class TenantInterceptorHelper {

    /**
     * HTTP Header中tenant_id的键名
     */
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    /**
     * HTTP Header中Authorization的键名
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Bearer Token前缀
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * 请求参数中tenant_id的键名
     */
    public static final String PARAM_TENANT_ID = "tenant_id";

    /**
     * 从HTTP请求中提取tenant_id
     *
     * <p>提取策略（按优先级）：
     * <ol>
     *   <li>从HTTP Header提取: X-Tenant-Id</li>
     *   <li>从JWT Token提取: Authorization: Bearer <token></li>
     *   <li>从请求参数提取: ?tenant_id=1</li>
     * </ol>
     *
     * @param request HTTP请求
     * @return tenant_id，如果未提取到返回null
     */
    public static Long extractTenantId(HttpServletRequest request) {
        // 策略1: 从HTTP Header提取（开发环境）
        Long tenantId = extractFromHeader(request);
        if (tenantId != null) {
            log.debug("从Header提取到tenant_id: {}", tenantId);
            return tenantId;
        }

        // 策略2: 从JWT Token提取（生产环境）
        tenantId = extractFromToken(request);
        if (tenantId != null) {
            log.debug("从Token提取到tenant_id: {}", tenantId);
            return tenantId;
        }

        // 策略3: 从请求参数提取（测试环境）
        tenantId = extractFromParameter(request);
        if (tenantId != null) {
            log.debug("从参数提取到tenant_id: {}", tenantId);
            return tenantId;
        }

        // 所有策略都失败，返回null（由调用方决定默认值）
        log.debug("未能从请求中提取到tenant_id");
        return null;
    }

    /**
     * 从HTTP Header提取tenant_id
     *
     * <p>Header格式: X-Tenant-Id: 1
     *
     * @param request HTTP请求
     * @return tenant_id，如果Header不存在或格式错误返回null
     */
    private static Long extractFromHeader(HttpServletRequest request) {
        String tenantIdStr = request.getHeader(HEADER_TENANT_ID);

        if (tenantIdStr == null || tenantIdStr.trim().isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(tenantIdStr.trim());
        } catch (NumberFormatException e) {
            log.warn("Header中的tenant_id格式错误: X-Tenant-Id={}", tenantIdStr);
            return null;
        }
    }

    /**
     * 从JWT Token提取tenant_id
     *
     * <p>Header格式: Authorization: Bearer <token>
     *
     * <p>注意：此方法需要JWT工具类支持，当前为简化实现
     * 完整实现需要：
     * <ol>
     *   <li>引入JWT依赖（io.jsonwebtoken:jjwt）</li>
     *   <li>实现JwtUtils.getTenantIdFromToken()方法</li>
     *   <li>验证Token签名和过期时间</li>
     * </ol>
     *
     * @param request HTTP请求
     * @return tenant_id，如果Token不存在或解析失败返回null
     */
    private static Long extractFromToken(HttpServletRequest request) {
        String authorization = request.getHeader(HEADER_AUTHORIZATION);

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        try {
            String token = authorization.substring(BEARER_PREFIX.length());

            // TODO: 完整实现JWT解析
            // 当前为简化实现，实际应该调用JwtUtils.getTenantIdFromToken(token)
            // 示例代码：
            // return JwtUtils.getTenantIdFromToken(token);

            log.warn("JWT Token解析功能待实现，当前为简化版本");
            return null;

        } catch (Exception e) {
            log.warn("从Token提取tenant_id失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从请求参数提取tenant_id
     *
     * <p>参数格式: ?tenant_id=1
     *
     * @param request HTTP请求
     * @return tenant_id，如果参数不存在或格式错误返回null
     */
    private static Long extractFromParameter(HttpServletRequest request) {
        String tenantIdStr = request.getParameter(PARAM_TENANT_ID);

        if (tenantIdStr == null || tenantIdStr.trim().isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(tenantIdStr.trim());
        } catch (NumberFormatException e) {
            log.warn("参数中的tenant_id格式错误: tenant_id={}", tenantIdStr);
            return null;
        }
    }

    /**
     * 验证tenant_id是否有效
     *
     * <p>验证规则：
     * <ul>
     *   <li>不能为null</li>
     *   <li>必须大于0</li>
     * </ul>
     *
     * @param tenant_id 待验证的租户ID
     * @return true-有效，false-无效
     */
    public static boolean isValidTenantId(Long tenant_id) {
        return tenant_id != null && tenant_id > 0;
    }
}
