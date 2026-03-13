package com.duda.common.security.config;

import com.duda.common.security.interceptor.TokenInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Token拦截器配置
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Configuration
public class TokenInterceptorConfig implements WebMvcConfigurer {

    @Resource
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有API请求
                .excludePathPatterns(
                        "/api/auth/login",           // 登录接口
                        "/api/auth/register",        // 注册接口
                        "/api/auth/refresh",         // 刷新Token接口
                        "/api/auth/validate",        // 验证Token接口
                        "/api/auth/sms/**",          // 短信相关接口
                        "/api/auth/test/**",         // 测试接口
                        "/api/auth/v2/**",           // V2 认证接口（新）
                        "/swagger-ui/**",            // Swagger UI
                        "/swagger-resources/**",     // Swagger资源
                        "/v3/api-docs/**",           // API文档
                        "/webjars/**",               // Swagger webjars
                        "/error"                     // 错误页面
                );
    }
}
