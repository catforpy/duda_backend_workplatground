package com.duda.file.provider.helper;

import com.duda.file.adapter.StorageService;
import com.duda.file.adapter.TemporaryAdapter;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import com.duda.file.enums.StorageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 简化的适配器工厂
 * TODO: 实现完整的适配器工厂
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
@Component
public class SimpleAdapterFactory {

    /**
     * 创建存储适配器（临时实现）
     */
    public StorageService createAdapter(ApiKeyConfigDTO apiKeyConfig) {
        log.warn("使用临时适配器工厂，返回临时适配器");
        return new TemporaryAdapter();
    }

    /**
     * 创建存储适配器（临时实现）
     */
    public StorageService createAdapter(StorageType storageType, ApiKeyConfigDTO apiKeyConfig) {
        log.warn("使用临时适配器工厂，返回临时适配器");
        return new TemporaryAdapter();
    }
}
