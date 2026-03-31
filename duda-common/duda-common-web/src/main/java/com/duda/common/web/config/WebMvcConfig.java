package com.duda.common.web.config;

import com.duda.common.interceptor.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Web MVC 配置
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TenantInterceptor tenantInterceptor;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 使用 extendMessageConverters 而不是 configureMessageConverters
        // 这样可以在现有转换器基础上添加，而不是替换

        // 找到 Jackson 转换器并确保它支持所有 JSON 类型
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jsonConverter) {
                // Spring Boot 默认已经配置好了，不需要修改
                break;
            }
        }

        // 添加字符串转换器（如果还没有）
        boolean hasStringConverter = false;
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof StringHttpMessageConverter) {
                hasStringConverter = true;
                break;
            }
        }

        if (!hasStringConverter) {
            StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
            stringConverter.setWriteAcceptCharset(false);
            converters.add(stringConverter);
        }
    }

    /**
     * 注册拦截器
     *
     * <p>拦截器配置：
     * <ul>
     *   <li>TenantInterceptor: 租户拦截器，提取并设置tenant_id</li>
     * </ul>
     *
     * <p>拦截路径：
     * <ul>
     *   <li>/**: 拦截所有请求</li>
     * </ul>
     *
     * <p>排除路径：
     * <ul>
     *   <li>/public/**: 公共接口，不需要租户隔离</li>
     *   <li>/error: 错误页面</li>
     *   <li>/swagger-ui/**: Swagger UI</li>
     *   <li>/v3/api-docs/**: API文档</li>
     *   <li>/swagger-resources/**: Swagger资源</li>
     *   <li>/webjars/**: WebJars资源</li>
     * </ul>
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")              // 拦截所有请求
                .excludePathPatterns(               // 排除公共接口
                    "/public/**",                   // 公共接口
                    "/error",                       // 错误页面
                    "/swagger-ui/**",               // Swagger UI
                    "/v3/api-docs/**",              // API文档（OpenAPI 3.0）
                    "/swagger-resources/**",        // Swagger资源
                    "/webjars/**",                  // WebJars资源
                    "/favicon.ico",                 // 网站图标
                    "/actuator/**"                  // Spring Boot Actuator（健康检查等）
                );
    }
}
