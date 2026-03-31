package com.duda.tenant.interceptor;

import com.duda.tenant.context.TenantContext;
import com.duda.tenant.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 租户拦截器
 * 从HTTP请求中提取租户信息，并设置到TenantContext
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Component
public class DudaTenantInterceptor implements HandlerInterceptor {

    @Autowired
    private TenantService tenantService;

    /**
     * 请求头中的租户编码字段名
     */
    private static final String TENANT_CODE_HEADER = "X-Tenant-Code";

    /**
     * 请求参数中的租户编码字段名
     */
    private static final String TENANT_CODE_PARAM = "tenantCode";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 1. 从请求头获取租户编码
            String tenantCode = request.getHeader(TENANT_CODE_HEADER);

            // 2. 如果请求头没有，从请求参数获取
            if (tenantCode == null || tenantCode.isEmpty()) {
                tenantCode = request.getParameter(TENANT_CODE_PARAM);
            }

            // 3. 如果还是没有，从URI路径提取（例如：/api/tenant/company001/users）
            if (tenantCode == null || tenantCode.isEmpty()) {
                String uri = request.getRequestURI();
                String[] paths = uri.split("/");
                if (paths.length > 2) {
                    // 尝试从路径中提取租户编码
                    for (String path : paths) {
                        if (path.startsWith("tenant_") || path.matches("^[a-zA-Z0-9_-]+$")) {
                            // 验证是否是有效的租户编码
                            if (tenantService.getByTenantCode(path) != null) {
                                tenantCode = path;
                                break;
                            }
                        }
                    }
                }
            }

            // 4. 设置租户上下文
            if (tenantCode != null && !tenantCode.isEmpty()) {
                // 验证租户是否存在
                var tenant = tenantService.getByTenantCode(tenantCode);
                if (tenant != null && tenantService.isValidTenant(tenant.getId())) {
                    TenantContext.setTenantId(tenant.getId());
                    TenantContext.setTenantCode(tenant.getTenantCode());
                    TenantContext.setSchemaName("tenant_" + tenant.getTenantCode());

                    log.debug("设置租户上下文成功: tenantId={}, tenantCode={}, schema={}",
                        tenant.getId(), tenant.getTenantCode(), "tenant_" + tenant.getTenantCode());
                } else {
                    log.warn("租户不存在或已失效: tenantCode={}", tenantCode);
                }
            } else {
                log.debug("请求中未找到租户信息: uri={}", request.getRequestURI());
            }

            return true;

        } catch (Exception e) {
            log.error("租户拦截器处理失败", e);
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 清除租户上下文，避免内存泄漏
        TenantContext.clear();
    }
}
