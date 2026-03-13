package com.duda.file.adapter;

import com.duda.file.common.exception.StorageException;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import com.duda.file.enums.StorageType;

/**
 * 存储服务适配器工厂
 * 负责根据存储类型创建对应的适配器实例
 *
 * @author duda
 * @date 2025-03-13
 */
public interface StorageAdapterFactory {

    /**
     * 根据存储类型创建适配器
     *
     * @param storageType 存储类型
     * @param apiKeyConfig API密钥配置
     * @return 存储服务适配器
     * @throws StorageException 存储异常
     */
    StorageService createAdapter(StorageType storageType, ApiKeyConfigDTO apiKeyConfig) throws StorageException;

    /**
     * 创建阿里云OSS适配器
     *
     * @param apiKeyConfig API密钥配置
     * @return 存储服务适配器
     * @throws StorageException 存储异常
     */
    StorageService createAliyunOSSAdapter(ApiKeyConfigDTO apiKeyConfig) throws StorageException;

    /**
     * 创建腾讯云COS适配器
     *
     * @param apiKeyConfig API密钥配置
     * @return 存储服务适配器
     * @throws StorageException 存储异常
     */
    StorageService createTencentCOSAdapter(ApiKeyConfigDTO apiKeyConfig) throws StorageException;

    /**
     * 创建七牛云Kodo适配器
     *
     * @param apiKeyConfig API密钥配置
     * @return 存储服务适配器
     * @throws StorageException 存储异常
     */
    StorageService createQiniuKodoAdapter(ApiKeyConfigDTO apiKeyConfig) throws StorageException;

    /**
     * 创建MinIO适配器
     *
     * @param apiKeyConfig API密钥配置
     * @return 存储服务适配器
     * @throws StorageException 存储异常
     */
    StorageService createMinIOAdapter(ApiKeyConfigDTO apiKeyConfig) throws StorageException;
}
