package com.duda.common.rocketmq.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * RocketMQ 自动配置类
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.duda.common.rocketmq")
public class RocketMQConfig {
}
