package com.duda.file.provider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * duda-file-provider 启动类
 * 文件服务提供者 - Dubbo服务
 *
 * @author duda
 * @date 2025-03-13
 */
@SpringBootApplication(scanBasePackages = "com.duda.file")
@EnableDiscoveryClient
@EnableDubbo(scanBasePackages = "com.duda.file.provider.impl")
@MapperScan("com.duda.file.provider.mapper")
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
