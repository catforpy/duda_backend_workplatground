package com.duda.file.provider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * duda-file-provider 启动类
 * 文件服务提供者 - Dubbo服务
 *
 * @author duda
 * @date 2025-03-13
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo(scanBasePackages = "com.duda.file.rpc")
@MapperScan("com.duda.file.provider.mapper")
@ComponentScan(basePackages = {"com.duda.file.provider", "com.duda.file.service", "com.duda.file.rpc", "com.duda.file.manager", "com.duda.file.common"})
@EnableScheduling
public class DudaFileProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DudaFileProviderApplication.class, args);
        System.out.println("""

            ======================================
              duda-file-provider 启动成功！
              Dubbo服务已注册到Nacos
            ======================================
            """);
    }
}
