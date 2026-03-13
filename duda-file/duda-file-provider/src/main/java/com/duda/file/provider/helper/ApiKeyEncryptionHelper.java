package com.duda.file.provider.helper;

import com.duda.file.common.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * API密钥加密助手
 * 用于在保存API密钥到数据库前进行加密
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
@Component
public class ApiKeyEncryptionHelper {

    /**
     * API密钥加密密钥(从Nacos配置中心读取)
     */
    @Value("${duda.file.encryption.key:duda-file-encryption-key}")
    private String encryptionKey;

    /**
     * 加密API密钥Access Key ID
     *
     * @param plainKeyId 明文Access Key ID
     * @return Base64编码的密文
     */
    public String encryptAccessKeyId(String plainKeyId) {
        if (!StringUtils.hasText(plainKeyId)) {
            return "";
        }
        try {
            return AesUtil.encrypt(plainKeyId, encryptionKey);
        } catch (Exception e) {
            log.error("加密Access Key ID失败", e);
            throw new RuntimeException("Failed to encrypt Access Key ID: " + e.getMessage(), e);
        }
    }

    /**
     * 加密API密钥Access Key Secret
     *
     * @param plainKeySecret 明文Access Key Secret
     * @return Base64编码的密文
     */
    public String encryptAccessKeySecret(String plainKeySecret) {
        if (!StringUtils.hasText(plainKeySecret)) {
            return "";
        }
        try {
            return AesUtil.encrypt(plainKeySecret, encryptionKey);
        } catch (Exception e) {
            log.error("加密Access Key Secret失败", e);
            throw new RuntimeException("Failed to encrypt Access Key Secret: " + e.getMessage(), e);
        }
    }

    /**
     * 解密API密钥
     *
     * @param encryptedKey 加密的密钥
     * @return 明文密钥
     */
    public String decryptApiKey(String encryptedKey) {
        if (!StringUtils.hasText(encryptedKey)) {
            return "";
        }
        try {
            return AesUtil.decrypt(encryptedKey, encryptionKey);
        } catch (Exception e) {
            log.error("解密API密钥失败", e);
            throw new RuntimeException("Failed to decrypt API key: " + e.getMessage(), e);
        }
    }

    /**
     * 批量加密API密钥对
     *
     * @param accessKeyId 明文Access Key ID
     * @param accessKeySecret 明文Access Key Secret
     * @return 加密后的密钥对 [加密的AccessKeyId, 加密的AccessKeySecret]
     */
    public String[] encryptApiKeyPair(String accessKeyId, String accessKeySecret) {
        String encryptedKeyId = encryptAccessKeyId(accessKeyId);
        String encryptedKeySecret = encryptAccessKeySecret(accessKeySecret);
        return new String[]{encryptedKeyId, encryptedKeySecret};
    }
}
