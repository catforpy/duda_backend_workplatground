package com.duda.common.security.config;

import com.duda.common.security.properties.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * JWT自动配置类
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@ComponentScan(basePackages = "com.duda.common.security")
public class JwtAutoConfiguration {
    // 自动配置JWT相关组件
}
