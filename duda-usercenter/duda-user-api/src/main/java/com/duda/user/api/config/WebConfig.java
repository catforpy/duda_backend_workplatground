package com.duda.user.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * @author DudaNexus
 * @since 2026-03-14
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域请求
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 对所有路径生效
                .allowedOriginPatterns("*")  // 允许所有来源
                .allowedMethods("*")  // 允许所有 HTTP 方法
                .allowedHeaders("*")  // 允许所有请求头
                .exposedHeaders("*")  // 暴露所有响应头
                .allowCredentials(true)  // 允许携带认证信息（Cookie等）
                .maxAge(3600);  // 预检请求缓存时间（秒）
    }
}
