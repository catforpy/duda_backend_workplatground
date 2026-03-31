package com.duda.tenant.api;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 租户API服务启动类
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@SpringBootApplication(scanBasePackages = {"com.duda.tenant.api", "com.duda.common"})
@EnableDiscoveryClient
@EnableDubbo
public class TenantApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenantApiApplication.class, args);
    }
}
