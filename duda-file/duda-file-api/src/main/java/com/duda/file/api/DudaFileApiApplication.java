package com.duda.file.api;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * duda-file-api 启动类
 * 文件API服务 - REST API网关
 *
 * @author DudaNexus
 * @date 2026-03-14
 */
@SpringBootApplication(scanBasePackages = "com.duda.file")
@EnableDiscoveryClient
@EnableDubbo(scanBasePackages = "com.duda.file.rpc")
public class DudaFileApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DudaFileApiApplication.class, args);
        System.out.println("""

            ======================================
              duda-file-api 启动成功！
              REST API: http://localhost:8085
              Swagger: http://localhost:8085/swagger-ui.html
              已注册到 Nacos (FILE_GROUP)
            ======================================
            """);
    }
}
