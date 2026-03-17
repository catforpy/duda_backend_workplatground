package com.duda.file.provider.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES-GCM加密工具类
 * 用于加密/解密OSS访问密钥
 *
 * @author duda
 * @date 2025-03-17
 */
@Slf4j
@Component
public class AesEncryptUtil {

    /**
     * AES密钥（从配置文件读取，建议使用环境变量）
     * 密钥长度必须是16/24/32字节（AES-128/192/256）
     */
    @Value("${encryption.aes.key:ChangeThisKeyTo32BytesLong!!}")
    private String aesKeyStr;

    /**
     * GCM认证标签长度（位）
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * GCM初始化向量长度（字节）
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加密模式和填充
     */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * 加密字符串
     *
     * @param plaintext 明文
     * @return Base64编码的密文
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            // 1. 获取密钥
            byte[] keyBytes = getKeyBytes();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            // 2. 生成随机IV（初始化向量）
            byte[] iv = generateIV();

            // 3. 创建加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            // 4. 加密
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // 5. 组合：IV + 密文
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            byte[] combined = byteBuffer.array();

            // 6. Base64编码
            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            log.error("AES加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * 解密字符串
     *
     * @param ciphertext Base64编码的密文
     * @return 明文
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }

        try {
            // 1. Base64解码
            byte[] combined = Base64.getDecoder().decode(ciphertext);

            // 2. 提取IV和密文
            ByteBuffer byteBuffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);

            // 3. 获取密钥
            byte[] keyBytes = getKeyBytes();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            // 4. 创建解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            // 5. 解密
            byte[] plaintext = cipher.doFinal(encrypted);

            // 6. 转换为字符串
            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("AES解密失败: {}", e.getMessage(), e);
            throw new RuntimeException("AES解密失败", e);
        }
    }

    /**
     * 获取密钥字节数组
     * 确保密钥长度为32字节（AES-256）
     *
     * 如果配置的密钥是Base64编码的，先解码
     * 否则直接使用原始字节
     */
    private byte[] getKeyBytes() {
        byte[] keyBytes;

        // 尝试Base64解码（如果密钥包含Base64字符特征）
        if (aesKeyStr.matches("^[A-Za-z0-9+/]+=*$")) {
            try {
                keyBytes = Base64.getDecoder().decode(aesKeyStr);
                log.debug("AES密钥已Base64解码，长度: {} 字节", keyBytes.length);
            } catch (IllegalArgumentException e) {
                // Base64解码失败，使用原始字节
                log.warn("AES密钥Base64解码失败，使用原始字节");
                keyBytes = aesKeyStr.getBytes(StandardCharsets.UTF_8);
            }
        } else {
            keyBytes = aesKeyStr.getBytes(StandardCharsets.UTF_8);
        }

        // 确保密钥长度为32字节（AES-256）
        byte[] result = new byte[32];

        // 如果密钥长度不足32字节，用0填充；如果超过32字节，截断
        System.arraycopy(keyBytes, 0, result, 0, Math.min(keyBytes.length, 32));

        return result;
    }

    /**
     * 生成随机IV（初始化向量）
     */
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        // 使用安全随机数生成器
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * 批量加密
     *
     * @param plaintexts 明文数组
     * @return 密文数组
     */
    public String[] encryptBatch(String[] plaintexts) {
        if (plaintexts == null) {
            return null;
        }
        String[] result = new String[plaintexts.length];
        for (int i = 0; i < plaintexts.length; i++) {
            result[i] = encrypt(plaintexts[i]);
        }
        return result;
    }

    /**
     * 批量解密
     *
     * @param ciphertexts 密文数组
     * @return 明文数组
     */
    public String[] decryptBatch(String[] ciphertexts) {
        if (ciphertexts == null) {
            return null;
        }
        String[] result = new String[ciphertexts.length];
        for (int i = 0; i < ciphertexts.length; i++) {
            result[i] = decrypt(ciphertexts[i]);
        }
        return result;
    }
}
