package com.duda.tenant.config;

import com.duda.tenant.interceptor.DudaTenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Configuration
public class DudaWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private DudaTenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有API请求
                .excludePathPatterns(
                    "/api/tenant/public/**",  // 公开接口
                    "/swagger-ui/**",         // Swagger UI
                    "/v3/api-docs/**",        // API文档
                    "/actuator/**",           // 健康检查
                    "/error"                  // 错误页面
                );
    }
}
