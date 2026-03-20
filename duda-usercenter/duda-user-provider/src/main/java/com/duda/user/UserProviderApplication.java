package com.duda.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户Provider启动类
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@SpringBootApplication(scanBasePackages = {"com.duda.user", "com.duda.common"})
@EnableDiscoveryClient
@EnableDubbo(scanBasePackages = "com.duda.user.rpc")
@MapperScan("com.duda.user.mapper")
public class UserProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserProviderApplication.class, args);
        System.out.println("✅ 用户Provider服务启动成功！");
    }
}
