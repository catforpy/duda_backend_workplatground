package com.duda.tenant;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 租户服务启动类
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@SpringBootApplication(scanBasePackages = {
    "com.duda.tenant",
    "com.duda.common"
})
@EnableDubbo
public class TenantApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenantApplication.class, args);
    }
}
