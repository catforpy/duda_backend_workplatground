package com.duda.msg.provider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 短信服务提供者启动类
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.duda.msg.provider.rpc")
public class MsgProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsgProviderApplication.class, args);
        System.out.println("✅ 短信服务启动成功！");
    }
}
