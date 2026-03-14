package com.duda.file.provider.helper;

import com.duda.file.adapter.AliyunOSSAdapter;
import com.duda.file.adapter.StorageService;
import com.duda.file.adapter.TemporaryAdapter;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import com.duda.file.enums.StorageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 简化的适配器工厂
 * 根据存储类型创建对应的存储适配器
 *
 * @author duda
 * @date 2025-03-14
 */
@Slf4j
@Component
public class SimpleAdapterFactory {

    /**
     * 创建存储适配器（根据配置）
     */
    public StorageService createAdapter(ApiKeyConfigDTO apiKeyConfig) {
        if (apiKeyConfig == null || apiKeyConfig.getStorageType() == null) {
            log.warn("StorageType not specified, using temporary adapter");
            return new TemporaryAdapter();
        }

        return createAdapter(apiKeyConfig.getStorageType(), apiKeyConfig);
    }

    /**
     * 创建存储适配器（根据存储类型）
     */
    public StorageService createAdapter(StorageType storageType, ApiKeyConfigDTO apiKeyConfig) {
        if (storageType == null) {
            log.warn("StorageType is null, using temporary adapter");
            return new TemporaryAdapter();
        }

        switch (storageType) {
            case ALIYUN_OSS:
                log.info("Creating AliyunOSSAdapter with endpoint: {}, region: {}",
                    apiKeyConfig.getEndpoint(), apiKeyConfig.getRegion());
                return new AliyunOSSAdapter(apiKeyConfig);

            case TENCENT_COS:
                log.warn("TencentCOSAdapter not implemented yet, using temporary adapter");
                return new TemporaryAdapter();

            case QINIU_KODO:
                log.warn("QiniuKodoAdapter not implemented yet, using temporary adapter");
                return new TemporaryAdapter();

            case MINIO:
                log.warn("MinIOAdapter not implemented yet, using temporary adapter");
                return new TemporaryAdapter();

            default:
                log.warn("Unknown storage type: {}, using temporary adapter", storageType);
                return new TemporaryAdapter();
        }
    }
}
