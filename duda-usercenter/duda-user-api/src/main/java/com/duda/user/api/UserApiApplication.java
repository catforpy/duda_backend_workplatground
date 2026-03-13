package com.duda.user.api;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户API层启动类
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@EnableDubbo
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {
    "com.duda.user.api",
    "com.duda.common.security",
    "com.duda.common.domain",
    "com.duda.common.dto",
    "com.duda.common.enums",
    "com.duda.common.utils",
    "com.duda.common.exception",
    "com.duda.common.config"
})
public class UserApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApiApplication.class, args);
        System.out.println("✅ 用户API服务启动成功！");
    }
}
