package com.duda.common.interceptor;

import com.duda.common.context.TenantContext;
import com.duda.common.helper.TenantInterceptorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 租户拦截器
 *
 * <p>功能说明：
 * <ul>
 *   <li>从HTTP请求中提取tenant_id</li>
 *   <li>设置到TenantContext（ThreadLocal）</li>
 *   <li>请求结束后清除TenantContext（防止内存泄漏）</li>
 * </ul>
 *
 * <p>提取优先级：
 * <ol>
 *   <li>HTTP Header（开发环境）: X-Tenant-Id</li>
 *   <li>JWT Token（生产环境）: 从Token解析</li>
 *   <li>请求参数（测试环境）: ?tenant_id=1</li>
 *   <li>默认值（降级方案）: tenant_id = 1</li>
 * </ol>
 *
 * <p>使用示例：
 * <pre>
 * &#64;Configuration
 * public class WebMvcConfig implements WebMvcConfigurer {
 *     &#64;Autowired
 *     private TenantInterceptor tenantInterceptor;
 *
 *     &#64;Override
 *     public void addInterceptors(InterceptorRegistry registry) {
 *         registry.addInterceptor(tenantInterceptor)
 *                 .addPathPatterns("/**")
 *                 .excludePathPatterns("/public/**");
 *     }
 * }
 * </pre>
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    /**
     * 请求处理前：提取并设置tenant_id
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @return true-继续处理请求，false-中断请求
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {
        try {
            // 步骤1: 从请求中提取tenant_id
            Long tenantId = TenantInterceptorHelper.extractTenantId(request);

            // 步骤2: 验证并设置默认值
            if (tenantId == null) {
                log.debug("未提取到tenant_id，使用默认租户: tenant_id=1");
                tenantId = 1L; // 默认租户
            }

            // 步骤3: 设置到TenantContext
            TenantContext.setTenantId(tenantId);

            // 步骤4: 记录日志（debug级别）
            if (log.isDebugEnabled()) {
                log.debug("租户拦截器: 设置tenant_id={}, URI={}, Method={}, RemoteAddr={}",
                    tenantId,
                    request.getRequestURI(),
                    request.getMethod(),
                    request.getRemoteAddr()
                );
            }

            return true; // 继续处理请求

        } catch (Exception e) {
            // 异常处理：使用默认租户，记录错误日志
            log.error("租户拦截器异常，使用默认租户: tenant_id=1, URI={}, Error={}",
                request.getRequestURI(),
                e.getMessage(),
                e
            );
            TenantContext.setTenantId(1L); // 降级到默认租户
            return true;
        }
    }

    /**
     * 请求完成后：清除TenantContext（防止内存泄漏）
     *
     * <p>重要：必须清除ThreadLocal，否则会导致内存泄漏！
     * 原因：Tomcat使用线程池，线程会被复用，ThreadLocal数据会一直存在
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @param ex       异常（如果有）
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                              HttpServletResponse response,
                              Object handler,
                              Exception ex) {
        try {
            // 清除TenantContext（防止内存泄漏）
            if (TenantContext.hasTenantId()) {
                Long tenantId = TenantContext.getTenantId();
                TenantContext.clear();

                if (log.isDebugEnabled()) {
                    log.debug("租户拦截器: 清除tenant_id={}, URI={}",
                        tenantId,
                        request.getRequestURI()
                    );
                }
            }
        } catch (Exception e) {
            // 清除失败记录错误日志，但不影响请求处理
            log.error("清除TenantContext异常: URI={}, Error={}",
                request.getRequestURI(),
                e.getMessage(),
                e
            );
        }
    }
}
