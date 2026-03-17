package com.duda.file.provider.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * OSS 适配器配置
 *
 * @author duda
 * @date 2026-03-14
 */
@Slf4j
@Configuration
public class OSSAdapterConfig {

    // ⚠️ 已废弃：配置文件密钥已迁移到数据库
    // OSS适配器Bean不再在这里创建，改由各个服务组件按需创建
    // 这样可以从数据库读取密钥并解密后动态初始化OSS适配器

    // Bean创建已禁用，由其他组件按需创建
    // 避免启动时因配置文件缺少密钥而失败
}
