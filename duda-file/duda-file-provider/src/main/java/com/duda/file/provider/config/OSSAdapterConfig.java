package com.duda.file.provider.config;

import com.duda.file.adapter.AliyunOSSAdapter;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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

    @Value("${aliyun.sts.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sts.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sts.endpoint:sts.cn-hangzhou.aliyuncs.com}")
    private String endpoint;

    @Value("${aliyun.sts.region:cn-hangzhou}")
    private String region;

    /**
     * 创建 AliyunOSSAdapter Bean
     */
    @Bean
    public AliyunOSSAdapter aliyunOSSAdapter() {
        log.info("初始化 AliyunOSSAdapter...");
        log.info("AccessKey ID: {}", accessKeyId);
        log.info("Endpoint: {}", endpoint);
        log.info("Region: {}", region);

        ApiKeyConfigDTO config = new ApiKeyConfigDTO();
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(accessKeySecret);
        config.setEndpoint(endpoint);
        config.setRegion(region);

        return new AliyunOSSAdapter(config);
    }
}
