package com.duda.msg.provider.config;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Dubbo 配置类
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Configuration
public class DubboConfig {

    /**
     * 配置 Dubbo 应用名称
     */
    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("duda-msg-provider");
        return applicationConfig;
    }

    /**
     * 配置 Dubbo 注册中心
     */
    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("nacos://120.26.170.213:8848");
        registryConfig.setProtocol("nacos");
        registryConfig.setGroup("MSG_GROUP");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("namespace", "duda-dev");
        parameters.put("username", "nacos");
        parameters.put("password", "nacos");
        registryConfig.setParameters(parameters);

        return registryConfig;
    }
}
