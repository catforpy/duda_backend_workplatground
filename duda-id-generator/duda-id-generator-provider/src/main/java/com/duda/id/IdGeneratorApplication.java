package com.duda.id;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 雪花ID生成服务启动类
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@SpringBootApplication
@EnableDubbo
public class IdGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdGeneratorApplication.class, args);
    }
}
